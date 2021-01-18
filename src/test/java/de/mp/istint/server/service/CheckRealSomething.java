package de.mp.istint.server.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.mp.istint.server.model.racelog.LapDataMetaData;
import de.mp.istint.server.model.racelog.RaceLogMetaData;
import de.mp.istint.server.model.racelog.ResultMetaData;
import de.mp.istint.server.model.racelog.response.StintData;
import de.mp.istint.server.repository.racelog.LapDataRepository;
import de.mp.istint.server.repository.racelog.RaceLogDataRepository;
import de.mp.istint.server.repository.racelog.ResultDataRepository;
import de.mp.istint.server.service.racelog.MergeLaptimes;
import de.mp.istint.server.service.racelog.StintProcessor;

@ActiveProfiles({ "realdb" })
// exclude is required because we don't want the embedded in-memory flapdoodle to be instantiated when checking things against real data
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
public class CheckRealSomething {
    @Autowired
    private RaceLogDataRepository raceLogDataRepository;
    @Autowired
    private LapDataRepository lapDataRepository;
    @Autowired
    private ResultDataRepository resultDataRepository;

    @Test
    public void testSomething() {
        String raceEventId = "14998128-7246-4d73-a063-f1ae20398eef";
        int sessionNum = 2;
        int carIdx = 34;
        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        List<Integer> sessionTicks = laps.stream().map(item -> item.getSessionTick()).collect(Collectors.toList());

        // List<RaceLogMetaData> bigRacelogMeta = raceLogDataRepository.findByRaceEventIdAndSessionNum(raceEventId, sessionNum);
        List<RaceLogMetaData> racelogMeta = raceLogDataRepository.findByRaceEventIdAndSessionNumAndSessionTickInOrderBySessionTimeAsc(raceEventId, sessionNum, sessionTicks);
        Map<Integer, RaceLogMetaData> lookup = racelogMeta.stream().collect(Collectors.toMap(RaceLogMetaData::getSessionTick, Function.identity()));

        System.out.printf("CheckRealSomething.testSomething() laps: %d raceLog: %d %n", laps.size(), racelogMeta.size());

        laps.stream().limit(50).forEach(l -> {
            RaceLogMetaData raceLog = lookup.get(l.getSessionTick());
            List<RaceLogMetaData> surround = raceLogDataRepository.findByRaceEventIdAndSessionNumAndSessionTimeBetweenOrderBySessionTimeAsc(raceEventId, sessionNum, l.getSessionTime() - 2, l.getSessionTime() + 2);
            String info = surround.stream().map(rl -> String.format("ST%.0f: LC: %2d LT: %.3f", rl.getSessionTime(), raceLog.getData().getCarIdxLapCompleted()[l.getData().getCarIdx()], raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()])).collect(Collectors.joining(", "));
            System.out.printf("My: Lap %2d:%.3f iR: LC: %2d L: %2d:%.3f info: %s%n",
                    l.getData().getLapNo(), l.getData().getLapTime(),
                    raceLog.getData().getCarIdxLapCompleted()[l.getData().getCarIdx()],
                    raceLog.getData().getCarIdxLap()[l.getData().getCarIdx()],
                    raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()],
                    info);
        });
    }

    @Test
    public void testOneOff() {
        String raceEventId = "14998128-7246-4d73-a063-f1ae20398eef";
        int sessionNum = 2;
        int carIdx = 34;
        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        List<Integer> sessionTicks = laps.stream().map(item -> item.getSessionTick()).collect(Collectors.toList());

        // List<RaceLogMetaData> bigRacelogMeta = raceLogDataRepository.findByRaceEventIdAndSessionNum(raceEventId, sessionNum);
        List<RaceLogMetaData> racelogMeta = raceLogDataRepository.findByRaceEventIdAndSessionNumAndSessionTickInOrderBySessionTimeAsc(raceEventId, sessionNum, sessionTicks);
        Map<Integer, RaceLogMetaData> lookup = racelogMeta.stream().collect(Collectors.toMap(RaceLogMetaData::getSessionTick, Function.identity()));

        System.out.printf("CheckRealSomething.testSomething() laps: %d raceLog: %d %n", laps.size(), racelogMeta.size());

        showLapsSideBySide(laps, racelogMeta, lookup);
    }

    private void showLapsSideBySide(List<LapDataMetaData> laps, List<RaceLogMetaData> racelogMeta, Map<Integer, RaceLogMetaData> lookup) {
        laps.stream().limit(500).forEach(l -> {
            RaceLogMetaData counterPart = lookup.get(l.getSessionTick());

            RaceLogMetaData raceLog = racelogMeta.get(racelogMeta.indexOf(counterPart) + 1);
            float delta = raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()] != -1 ? raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()] - l.getData().getLapTime() : -1;
            System.out.printf("My: Lap %2d:%.3f iR: %2d %.3f Delta: %6.3f%n",
                    l.getData().getLapNo(), l.getData().getLapTime(),
                    raceLog.getData().getCarIdxLapCompleted()[l.getData().getCarIdx()],
                    raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()],
                    delta);
        });
    }

    @Test
    public void testWithResultPositions() {
        String raceEventId = "14998128-7246-4d73-a063-f1ae20398eef";
        int sessionNum = 2;
        int carIdx = 34;
        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);

        List<ResultMetaData> resultData = resultDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        Map<Integer, List<ResultMetaData>> byLC = resultData.stream().collect(Collectors.groupingBy(d -> d.getData().getLapsComplete()));
        System.out.println("CheckRealSomething.testWithResultPositions() " + resultData.size());
        List<Integer> sessionTicks = byLC.keySet().stream().sorted().map(l -> byLC.get(l).get(0).getSessionTick()).collect(Collectors.toList());
        List<RaceLogMetaData> racelogMeta = raceLogDataRepository.findByRaceEventIdAndSessionNumAndSessionTickInOrderBySessionTimeAsc(raceEventId, sessionNum, sessionTicks);
        AtomicInteger idx = new AtomicInteger(0);
        laps.stream().limit(500).forEach(l -> {
            RaceLogMetaData raceLog = racelogMeta.get(idx.getAndIncrement());
            float delta = raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()] != -1 ? raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()] - l.getData().getLapTime() : -1;
            System.out.printf("My: Lap %2d:%.3f iR: %2d %.3f Delta: %6.3f%n",
                    l.getData().getLapNo(), l.getData().getLapTime(),
                    raceLog.getData().getCarIdxLapCompleted()[l.getData().getCarIdx()],
                    raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()],
                    delta);
        });

    }

    // own double lap 13
    @Test
    public void testCarIdx41() {
        String raceEventId = "14998128-7246-4d73-a063-f1ae20398eef";
        int sessionNum = 2;
        int carIdx = 41;
        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        List<LapDataMetaData> sub = laps.subList(30, 35);
        sub.forEach(item -> System.out.println(item));
        List<ResultMetaData> resultData = resultDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        Map<Integer, List<ResultMetaData>> byLC = resultData.stream().collect(Collectors.groupingBy(d -> d.getData().getLapsComplete()));
        System.out.println("CheckRealSomething.testWithResultPositions() " + resultData.size());
        List<Integer> sessionTicks = byLC.keySet().stream().sorted().map(l -> byLC.get(l).get(0).getSessionTick()).collect(Collectors.toList());
        List<RaceLogMetaData> racelogMeta = raceLogDataRepository.findByRaceEventIdAndSessionNumAndSessionTickInOrderBySessionTimeAsc(raceEventId, sessionNum, sessionTicks);
        AtomicInteger idx = new AtomicInteger(0);
        laps.stream().limit(50).forEach(l -> {
            RaceLogMetaData raceLog = racelogMeta.get(idx.getAndIncrement());
            float delta = raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()] != -1 ? raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()] - l.getData().getLapTime() : -1;
            System.out.printf("My: Lap %2d:%.3f iR: %2d %.3f Delta: %6.3f%n",
                    l.getData().getLapNo(), l.getData().getLapTime(),
                    raceLog.getData().getCarIdxLapCompleted()[l.getData().getCarIdx()],
                    raceLog.getData().getCarIdxLastLapTime()[l.getData().getCarIdx()],
                    delta);
        });

    }

    // empty stint?
    @Test
    public void testCarIdx22() {
        String raceEventId = "14998128-7246-4d73-a063-f1ae20398eef";
        int sessionNum = 2;
        int carIdx = 22;
        MergeLaptimes mergeLaptimes = new MergeLaptimes(lapDataRepository, resultDataRepository, raceLogDataRepository);
        List<LapDataMetaData> laps = mergeLaptimes.mergeLaptimes(raceEventId, sessionNum, carIdx);
        laps.stream().skip(0).limit(50).forEach(l -> {
            System.out.printf("Lap %3d:%7.3f in:%6b out:%6b inc:%6b%n",
                    l.getData().getLapNo(), l.getData().getLapTime(),
                    l.getData().isInLap(), l.getData().isOutLap(), l.getData().isIncomplete());
        });
        System.out.println("CheckRealSomething.testCarIdx22() RAW LAPS");
        List<LapDataMetaData> rawLaps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        rawLaps.stream().skip(0).limit(50).forEach(l -> {
            System.out.printf("Lap %3d:%7.3f in:%6b out:%6b inc:%6b%n",
                    l.getData().getLapNo(), l.getData().getLapTime(),
                    l.getData().isInLap(), l.getData().isOutLap(), l.getData().isIncomplete());
        });
        StintProcessor proc = new StintProcessor();
        List<StintData> stints = proc.analyze(laps);
    }

    // did not detect end of stint
    @Test
    public void testCarIdx46() {
        String raceEventId = "14998128-7246-4d73-a063-f1ae20398eef";
        int sessionNum = 2;
        int carIdx = 46;
        MergeLaptimes mergeLaptimes = new MergeLaptimes(lapDataRepository, resultDataRepository, raceLogDataRepository);
        List<LapDataMetaData> laps = mergeLaptimes.mergeLaptimes(raceEventId, sessionNum, carIdx);
        laps.stream().skip(75).limit(25).forEach(l -> {
            System.out.printf("Lap %3d:%7.3f in:%6b out:%6b inc:%6b%n",
                    l.getData().getLapNo(), l.getData().getLapTime(),
                    l.getData().isInLap(), l.getData().isOutLap(), l.getData().isIncomplete());
        });
        // System.out.println("CheckRealSomething.testCarIdx22() RAW LAPS");
        // List<LapDataMetaData> rawLaps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        // rawLaps.stream().skip(0).limit(50).forEach(l -> {
        //     System.out.printf("Lap %3d:%7.3f in:%6b out:%6b inc:%6b%n",
        //             l.getData().getLapNo(), l.getData().getLapTime(),
        //             l.getData().isInLap(), l.getData().isOutLap(), l.getData().isIncomplete());
        // });
        StintProcessor proc = new StintProcessor();
        List<StintData> stints = proc.analyze(laps);
    }

}
