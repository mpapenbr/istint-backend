package de.mp.istint.server.service.racelog;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;

import org.springframework.stereotype.Component;

import de.mp.istint.server.model.racelog.LapData;
import de.mp.istint.server.model.racelog.LapDataMetaData;
import de.mp.istint.server.model.racelog.response.LapDataExtended;
import de.mp.istint.server.model.racelog.response.MinMaxAvg;
import de.mp.istint.server.model.racelog.response.StintData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StintProcessor {

    public static Predicate<LapData> validLap = l -> l.isInLap() == false && l.isOutLap() == false && l.isIncomplete() == false;

    public List<StintData> analyze(List<LapDataMetaData> laps) {
        return analyze(laps, 2);
    }

    /**
     * analyze a series of laps and extract stint data from it.
     * <p>
     * in general a stint lives from the outlap to the inlap. But there may be some exceptions.
     * <ul>
     * <li>lap is aborted due to undrivable car (car reset)</li>
     * <li>connection lost</li>
     * <li>other</li>
     * </ul>
     * Some of these circumstances may be identified easily, other may be more complicated and need
     * additional data.
     * 
     * @param laps
     *            laps to analyze
     * @param ignoreTolerance
     *            ignore laps which are above or below this percentage of the (overall) avg lap
     * @return
     */
    public List<StintData> analyze(List<LapDataMetaData> metaLaps, double ignoreTolerance) {
        List<StintData> ret = Lists.newArrayList();
        int i = 0;
        int stint = 1;
        List<LapData> laps = metaLaps.stream().map(LapDataMetaData::getData).collect(Collectors.toList());
        Double avg = laps.stream().filter(StintProcessor.validLap).collect(Collectors.averagingDouble(l -> Double.valueOf(l.getLapTime())));
        if (laps.get(i).getLapNo() < 1)
            i++;

        int startOfStint = i;
        int nextStartOfStint = i;
        while (i < laps.size()) {
            // // find start of stint
            // while (i < laps.size() && laps.get(i).isOutLap() == false)
            //     i++;
            log.debug("found start of stint at pos {}", i);

            i++;
            while (i < laps.size() && laps.get(i).isInLap() == false && laps.get(i).isOutLap() == false)
                i++;
            // TODO: check if last lap was outlap (probably a car reset)
            int endOfStint = i + 1;
            if (laps.get(i).isOutLap()) {
                endOfStint = Math.min(0, i - 1);
                nextStartOfStint = i;
            } else {
                log.debug("found end of stint at pos {}", i);
                i++; // increment for next run
                nextStartOfStint = i;
            }

            // log.debug("{}", laps.subList(startOfStint, endOfStint));

            StintData stintData = processLaps(stint, metaLaps.subList(startOfStint, endOfStint), ignoreTolerance, avg);
            ret.add(stintData);
            startOfStint = nextStartOfStint;
            stint++;

        }
        return ret;
    }

    StintData processLaps(int stintNo, List<LapDataMetaData> metaLaps, double pctRange, double avg) {
        List<LapData> laps = metaLaps.stream().map(LapDataMetaData::getData).collect(Collectors.toList());
        Comparator<LapData> comp = Comparator.comparing(l -> l.getLapTime());
        double lowerBound = avg * ((100 - pctRange) / 100);
        double upperBound = avg * ((100 + pctRange) / 100);
        Predicate<LapData> withinTolerance = l -> l.getLapTime() > lowerBound && l.getLapTime() < upperBound;
        List<LapData> filtered = laps.stream().filter(StintProcessor.validLap).filter(withinTolerance).collect(Collectors.toList());
        return StintData.builder()
                .stintNo(stintNo)
                .all(MinMaxAvg.builder()
                        .count(laps.size())
                        .avg(laps.stream().filter(StintProcessor.validLap).collect(Collectors.averagingDouble(l -> Double.valueOf(l.getLapTime()))))
                        .min(laps.stream().filter(StintProcessor.validLap).min(comp).map(l -> Double.valueOf(l.getLapTime())).orElse(0.0))
                        .max(laps.stream().filter(StintProcessor.validLap).max(comp).map(l -> Double.valueOf(l.getLapTime())).orElse(0.0))
                        .build())
                .ranged(MinMaxAvg.builder()
                        .count(filtered.size())
                        .avg(filtered.stream().filter(StintProcessor.validLap).collect(Collectors.averagingDouble(l -> Double.valueOf(l.getLapTime()))))
                        .min(filtered.stream().filter(StintProcessor.validLap).min(comp).map(l -> Double.valueOf(l.getLapTime())).orElse(0.0))
                        .max(filtered.stream().filter(StintProcessor.validLap).max(comp).map(l -> Double.valueOf(l.getLapTime())).orElse(0.0))
                        .build())
                .laps(dings(metaLaps, avg, withinTolerance))
                .build();

    }

    List<LapDataExtended> dings(List<LapDataMetaData> laps, double avg, Predicate<LapData> withinTolerance) {
        Predicate<LapData> isClean = l -> l.isInLap() == false && l.isOutLap() == false && l.isIncomplete() == false;

        AtomicDouble sum = new AtomicDouble(0.0);
        AtomicDouble sumFiltered = new AtomicDouble(0.0);
        AtomicInteger i = new AtomicInteger(0);
        return laps.stream().map(lm -> {
            LapData l = lm.getData();
            i.incrementAndGet();
            boolean filtered = false;
            if (isClean.test(l)) {
                sum.getAndAdd(l.getLapTime());
                if (withinTolerance.test(l)) {
                    sumFiltered.getAndAdd(l.getLapTime());
                } else {
                    filtered = true;
                    sumFiltered.getAndAdd(avg);
                }
            } else {
                // non clean laps will be counted as avg
                filtered = true;
                sum.getAndAdd(avg);
                sumFiltered.getAndAdd(avg);
            }
            return LapDataExtended.builder()
                    .lapData(l)
                    .sessionTime(lm.getSessionTime())
                    .rollAvg((float) sum.get() / i.get())
                    .rollAvgFiltered((float) sumFiltered.get() / i.get())
                    .filtered(filtered)
                    .build();
        })
                .collect(Collectors.toList());

    }
}
