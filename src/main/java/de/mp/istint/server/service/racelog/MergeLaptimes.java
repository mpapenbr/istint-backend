package de.mp.istint.server.service.racelog;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.mp.istint.server.model.racelog.LapDataMetaData;
import de.mp.istint.server.model.racelog.RaceLogMetaData;
import de.mp.istint.server.model.racelog.ResultMetaData;
import de.mp.istint.server.repository.racelog.LapDataRepository;
import de.mp.istint.server.repository.racelog.RaceLogDataRepository;
import de.mp.istint.server.repository.racelog.ResultDataRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class MergeLaptimes {

    @Autowired
    private LapDataRepository lapDataRepository;
    @Autowired
    private ResultDataRepository resultDataRepository;
    @Autowired
    private RaceLogDataRepository raceLogDataRepository;

    /**
     * Our laptimes are sometime a little bit off (in bad cases around 0.2-0.3s)
     * On the other hand: we still have laptimes on driver changes and when iRacing decides to
     * mark it as -1 when slowdown was issued.
     * The way to corresponding iR-Lap is via sessionTick und ResultData
     * ResultData contains a lot of entries. We need only the first entry for each lap.
     * The matching can then be done by LapDataMetaData.data.lapNo ==
     * ResultData.data.carIdxLapsCompleted[carIdx]
     * 
     * (Note: for now we just match it by index position. this "should" also be ok ;)
     * 
     * All non "-1"-iRacingData will override our own measured times.
     * 
     * @return
     */

    public List<LapDataMetaData> mergeLaptimes(String raceEventId, int sessionNum, int carIdx) {

        return mergeLaptimesPure(raceEventId, sessionNum, carIdx);

    }

    /**
     * merges all laptimes for a session
     * 
     * @param raceEventId
     * @param sessionNum
     * @return merged laptimes in a map. key is carIdx
     */
    public Map<Integer, List<LapDataMetaData>> mergeLaptimes(String raceEventId, int sessionNum) {

        return mergeLaptimesPure(raceEventId, sessionNum);

    }

    public List<LapDataMetaData> mergeLaptimesPure(String raceEventId, int sessionNum, int carIdx) {

        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);

        return laps;
    }

    public Map<Integer, List<LapDataMetaData>> mergeLaptimesPure(String raceEventId, int sessionNum) {

        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumOrderBySessionTimeAsc(raceEventId, sessionNum);
        return laps.stream().collect(Collectors.groupingBy(l -> l.getData().getCarIdx()));
    }

    public List<LapDataMetaData> mergeLaptimesOld(String raceEventId, int sessionNum, int carIdx) {

        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        // Map<Integer, LapDataMetaData> lapLookup = laps.stream().collect(Collectors.toMap(l -> l.getData().getLapNo(), Function.identity()));
        List<ResultMetaData> resultData = resultDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        Map<Integer, List<ResultMetaData>> byLC = resultData.stream().collect(Collectors.groupingBy(d -> d.getData().getLapsComplete()));

        List<Integer> sessionTicks = byLC.keySet().stream().sorted().map(l -> byLC.get(l).get(0).getSessionTick()).collect(Collectors.toList());
        List<RaceLogMetaData> racelogMeta = raceLogDataRepository.findByRaceEventIdAndSessionNumAndSessionTickInOrderBySessionTimeAsc(raceEventId, sessionNum, sessionTicks);

        for (int i = 0; i < Math.min(laps.size(), racelogMeta.size()); i++) {
            RaceLogMetaData rl = racelogMeta.get(i);
            if (rl.getData().getCarIdxLastLapTime()[carIdx] > 0) {
                LapDataMetaData work = laps.get(i);
                work.getData().setLapNo(rl.getData().getCarIdxLapCompleted()[carIdx]);
                work.getData().setLapTime(rl.getData().getCarIdxLastLapTime()[carIdx]);
            }
        }
        return laps;
    }

    public List<LapDataMetaData> mergeLaptimesTry(String raceEventId, int sessionNum, int carIdx) {

        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        // Map<Integer, LapDataMetaData> lapLookup = laps.stream().collect(Collectors.toMap(l -> l.getData().getLapNo(), Function.identity()));
        List<ResultMetaData> resultData = resultDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        Map<Integer, List<ResultMetaData>> byLC = resultData.stream().collect(Collectors.groupingBy(d -> d.getData().getLapsComplete()));

        List<Integer> sessionTicks = byLC.keySet().stream().sorted().map(l -> byLC.get(l).get(0).getSessionTick()).collect(Collectors.toList());
        List<RaceLogMetaData> racelogMeta = raceLogDataRepository.findByRaceEventIdAndSessionNumAndSessionTickInOrderBySessionTimeAsc(raceEventId, sessionNum, sessionTicks);

        for (int i = 0; i < racelogMeta.size(); i++) {
            RaceLogMetaData rl = racelogMeta.get(i);
            int lapNo = rl.getData().getCarIdxLapCompleted()[carIdx];
            float lapTime = rl.getData().getCarIdxLastLapTime()[carIdx];

            Optional<LapDataMetaData> ownCounterPart = laps.stream().filter(l -> l.getData().getLapNo() == lapNo).findFirst();
            if (ownCounterPart.isEmpty()) {
                log.debug("No counterpart in own data for {}", lapNo);
            } else {
                log.debug(String.format("i: %d lapNo: %d rl: %.2f own: %.2f", i, lapNo, lapTime, ownCounterPart.get().getData().getLapTime()));
            }
            if (rl.getData().getCarIdxLastLapTime()[carIdx] > 0) {
                LapDataMetaData work = laps.get(i);
                work.getData().setLapNo(rl.getData().getCarIdxLapCompleted()[carIdx]);
                work.getData().setLapTime(rl.getData().getCarIdxLastLapTime()[carIdx]);
            }
        }
        return laps;
    }
}
