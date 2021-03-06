package de.mp.istint.server.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
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
import org.springframework.util.StopWatch;

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

    @Test
    public void testDaytona24MissingLaps() {
        String raceEventId = "403957dc-3570-481b-a288-a6877d6a120f";
        int sessionNum = 2;
        int carIdx = 30;
        MergeLaptimes mergeLaptimes = new MergeLaptimes(lapDataRepository, resultDataRepository, raceLogDataRepository);
        List<LapDataMetaData> laps = mergeLaptimes.mergeLaptimes(raceEventId, sessionNum, carIdx);
        laps.stream()
                .skip(180)
                .limit(25)
                .forEach(l -> {
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

    @Test
    public void collectDaytona24WrongLaps() throws FileNotFoundException {
        String raceEventId = "403957dc-3570-481b-a288-a6877d6a120f";
        int sessionNum = 2;
        int carIdx = 30;
        // List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataLapTimeGreaterThanOrderBySessionTimeAsc(raceEventId, sessionNum, 1000);
        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumOrderBySessionTimeAsc(raceEventId, sessionNum);
        long over = laps.stream().filter(l -> l.getData().getLapTime() > 1000).count();

        System.out.printf("total: %d over: %d%n", laps.size(), over);
        try (PrintWriter ps = new PrintWriter(new FileOutputStream("daytona-strange.csv", false))) {
            laps.stream().filter(l -> l.getData().getLapTime() > 1000).forEach(l -> ps.write(String.format("%s%n", l)));
        }
    }

    @Test
    public void collectDaytona24WrongLapsX() throws FileNotFoundException {
        String raceEventId = "403957dc-3570-481b-a288-a6877d6a120f";
        int sessionNum = 2;
        int carIdx = 30;
        // List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataLapTimeGreaterThanOrderBySessionTimeAsc(raceEventId, sessionNum, 1000);
        LapDataMetaData lap = lapDataRepository.findById(UUID.fromString("80445214-581e-06c4-70e0-977c67661c9f")).get();

    }

    @Test
    public void collectAllStints() throws FileNotFoundException {
        String raceEventId = "403957dc-3570-481b-a288-a6877d6a120f";
        int sessionNum = 2;
        StopWatch sw = new StopWatch("check stints");
        sw.start("read data");
        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumOrderBySessionTimeAsc(raceEventId, sessionNum);
        sw.stop();
        Map<Object, List<LapDataMetaData>> lookup = laps.stream().collect(Collectors.groupingBy(l -> l.getData().getCarIdx()));
        lookup.forEach((k, v) -> {
            sw.start("Car " + k);
            StintProcessor proc = new StintProcessor();
            List<StintData> stints = proc.analyze(v);
            sw.stop();
        });
        System.out.println(sw.prettyPrint());
    }

    @Test
    public void collectResultDataForCar() throws FileNotFoundException {
        String raceEventId = "403957dc-3570-481b-a288-a6877d6a120f";
        int sessionNum = 2;
        StopWatch sw = new StopWatch("collect data");
        sw.start("read data");
        List<ResultMetaData> data = resultDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, 34);
        System.out.println("CheckRealSomething.collectResultDataForCar() " + data.size());
        sw.stop();
        sw.start("Processing");
        Map<Integer, List<ResultMetaData>> byLap = data.stream().collect(Collectors.groupingBy(d -> d.getData().getLapsComplete()));
        //Comparator<Entry<Integer,?>> sortByLapNo = item -> Comparator.comparing(E)

        byLap.entrySet().stream().sorted(Entry.comparingByKey())
                .limit(50)
                .forEach(item -> System.out.printf("L %3d: Delta: %.2f%n", item.getKey(), item.getValue().get(0).getData().getDelta()));
        ;

        System.out.println(sw.prettyPrint());
    }

    @Test
    public void collectResultDataForTwoCars() throws FileNotFoundException {
        String raceEventId = "403957dc-3570-481b-a288-a6877d6a120f";
        int sessionNum = 2;
        StopWatch sw = new StopWatch("collect data");
        sw.start("read data");
        List<ResultMetaData> data = resultDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxInOrderBySessionTimeAsc(raceEventId, sessionNum, List.of(40, 37));

        System.out.println("CheckRealSomething.collectResultDataForCar() " + data.size());
        sw.stop();
        sw.start("Processing");
        Map<Integer, List<ResultMetaData>> car1ByLap = data.stream().filter(item -> item.getData().getCarIdx() == 40).collect(Collectors.groupingBy(d -> d.getData().getLapsComplete()));
        Map<Integer, List<ResultMetaData>> car2ByLap = data.stream().filter(item -> item.getData().getCarIdx() == 37).collect(Collectors.groupingBy(d -> d.getData().getLapsComplete()));
        //Comparator<Entry<Integer,?>> sortByLapNo = item -> Comparator.comparing(E)

        car1ByLap.entrySet().stream().sorted(Entry.comparingByKey())
                // .limit(50)
                .forEach(item -> {
                    List<ResultMetaData> other = Optional.ofNullable(car2ByLap.get(item.getKey())).orElse(List.of());
                    float myDelta = item.getValue().get(0).getData().getDelta();
                    float otherDelta = other.isEmpty() ? 0 : other.get(0).getData().getDelta();

                    System.out.printf("L %3d: Delta1: %.2f Delta2: %.2f Diff: %.2f%n", item.getKey(),
                            myDelta, otherDelta, myDelta - otherDelta

                );
                });

        System.out.println(sw.prettyPrint());
    }

}
