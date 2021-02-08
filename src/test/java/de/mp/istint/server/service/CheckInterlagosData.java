package de.mp.istint.server.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

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
import de.mp.istint.server.service.racelog.StintProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * Checks/repairs around the SCO Interlagos race (ID: a209eb7a-4e2b-45a0-b02b-8062bbab71ec)
 */
@Slf4j
@ActiveProfiles({ "realdb" })
// exclude is required because we don't want the embedded in-memory flapdoodle to be instantiated when checking things against real data
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
public class CheckInterlagosData {

    @Autowired
    private RaceLogDataRepository raceLogDataRepository;
    @Autowired
    private LapDataRepository lapDataRepository;
    @Autowired
    private ResultDataRepository resultDataRepository;

    @Test
    public void repairBiela450_idx8() {
        String raceEventId = "a209eb7a-4e2b-45a0-b02b-8062bbab71ec";
        int sessionNum = 2;
        int carIdx = 8;

        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        List<RaceLogMetaData> raceLogs = raceLogDataRepository.findByRaceEventIdAndSessionNum(raceEventId, sessionNum);
        Set<LapDataMetaData> toFix = Sets.newHashSet();
        raceLogs.stream()
                .filter(item -> item.getData().getCarIdxOnPitRoad()[carIdx])
                .forEach(item -> {
                    log.debug(String.format("Check CarIdx: %02d ST: %.1f LC: %d CL: %d", carIdx, item.getSessionTime(), item.getData().getCarIdxLapCompleted()[carIdx], item.getData().getCarIdxLap()[carIdx]));
                    laps.stream().filter(l -> l.getData().getLapNo() == item.getData().getCarIdxLap()[carIdx]).findFirst().ifPresent(lap -> toFix.add(lap));
                });

        toFix.stream()
                .sorted(Comparator.comparing(ld -> ld.getData().getLapNo()))
                .forEach(ld -> {
                    log.debug(String.format("CarIdx: %02d Lap: %3d In: %b Out: %b", ld.getData().getCarIdx(), ld.getData().getLapNo(), ld.getData().isInLap(), ld.getData().isOutLap()));
                });
        StintProcessor proc = new StintProcessor();
        List<StintData> stints = proc.analyze(laps);

    }

    @Test
    public void checkStintBiela450() {
        String raceEventId = "a209eb7a-4e2b-45a0-b02b-8062bbab71ec";
        int sessionNum = 2;
        int carIdx = 8;

        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);
        List<RaceLogMetaData> raceLogs = raceLogDataRepository.findByRaceEventIdAndSessionNum(raceEventId, sessionNum);
        List<ResultMetaData> resultData = resultDataRepository.findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(raceEventId, sessionNum, carIdx);

        laps
                .stream().skip(118).limit(2)
                .forEach(l -> {
                    List<RaceLogMetaData> foundList = raceLogs.stream().filter(rl -> rl.getData().getCarIdxLapCompleted()[carIdx] == l.getData().getLapNo()).collect(Collectors.toList());

                    RaceLogMetaData found = raceLogs.stream().filter(rl -> rl.getData().getCarIdxLapCompleted()[carIdx] == l.getData().getLapNo()).findFirst().get();
                    Optional<RaceLogMetaData> firstChange = foundList.stream()
                            .filter(rl -> rl.getData().getCarIdxLapCompleted()[carIdx] == l.getData().getLapNo())
                            .filter(rl -> rl.getData().getCarIdxLastLapTime()[carIdx] != -1)
                            .filter(rl -> rl.getData().getCarIdxLastLapTime()[carIdx] != found.getData().getCarIdxLastLapTime()[carIdx])
                            .findFirst();

                    float newLapTime = firstChange.map(fc -> fc.getData().getCarIdxLastLapTime()[carIdx])
                            .orElseGet(() -> {
                                var x = found.getData().getCarIdxLastLapTime()[carIdx];
                                if (l.getData().isIncomplete()) {
                                    return x;
                                } else {
                                    if (foundList.stream().anyMatch(rl -> rl.getData().getCarIdxLastLapTime()[carIdx] != x)) {
                                        return l.getData().getLapTime();
                                    } else {
                                        return x;
                                    }
                                }
                            });
                    log.debug(String.format("L: %3d Own: %.3f iR: %s (%.3f) newLaptime: %.3f", l.getData().getLapNo(), l.getData().getLapTime(),
                            firstChange.map(fc -> String.format("%.3f (%d)", fc.getData().getCarIdxLastLapTime()[carIdx], foundList.indexOf(fc))).orElse("n.a."),
                            found.getData().getCarIdxLastLapTime()[carIdx],
                            newLapTime));

                });
    }

    // LAPTIMES neu einspielen!

    @Test
    public void checkStintBiela450_X() {
        String raceEventId = "a209eb7a-4e2b-45a0-b02b-8062bbab71ec";
        int sessionNum = 2;
        int carIdx = 8;

        List<RaceLogMetaData> raceLogs = raceLogDataRepository.findByRaceEventIdAndSessionNum(raceEventId, sessionNum);

        // for(int i =0; i < 10; i++) {
        //     RaceLogMetaData found = raceLogs.stream().filter(rl -> rl.getData().getCarIdxLapCompleted()[carIdx] == l.getData().getLapNo()).findFirst().get();
        //     log.debug(String.format("L: %3d Own: %.3f iR: %.3f", l.getData().getLapNo(), l.getData().getLapTime(), found.getData().getCarIdxLastLapTime()[carIdx]));
        // }
    }

    @Test
    public void repairPitLaps() {
        String raceEventId = "a209eb7a-4e2b-45a0-b02b-8062bbab71ec";
        int sessionNum = 2;

        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumOrderBySessionTimeAsc(raceEventId, sessionNum);
        Map<Integer, List<LapDataMetaData>> lookup = laps.stream().collect(Collectors.groupingBy(l -> l.getData().getCarIdx()));

        List<RaceLogMetaData> raceLogs = raceLogDataRepository.findByRaceEventIdAndSessionNum(raceEventId, sessionNum);
        Set<LapDataMetaData> toFix = Sets.newHashSet();
        lookup.forEach((carIdx, v) -> {
            raceLogs.stream()
                    .filter(item -> item.getData().getCarIdxOnPitRoad()[carIdx])
                    .forEach(item -> {
                        log.debug(String.format("Check CarIdx: %02d ST: %.1f LC: %d CL: %d", carIdx, item.getSessionTime(), item.getData().getCarIdxLapCompleted()[carIdx], item.getData().getCarIdxLap()[carIdx]));
                        v.stream()
                                .filter(l -> l.getData().getLapNo() == item.getData().getCarIdxLap()[carIdx])
                                .filter(l -> l.getData().isInLap() == false && l.getData().isOutLap() == false)
                                .findFirst().ifPresent(lap -> toFix.add(lap));
                    });
        });

        Comparator<LapDataMetaData> comp = Comparator.<LapDataMetaData>comparingInt(ld -> ld.getData().getCarIdx())
                .thenComparingInt(ld -> ld.getData().getLapNo());

        toFix.stream()
                .sorted(comp)
                .forEach(ld -> {
                    log.debug(String.format("CarIdx: %02d Lap: %3d In: %b Out: %b", ld.getData().getCarIdx(), ld.getData().getLapNo(), ld.getData().isInLap(), ld.getData().isOutLap()));
                    ld.getData().setOutLap(true);
                });
        lapDataRepository.saveAll(toFix);

        // StintProcessor proc = new StintProcessor();
        // List<StintData> stints = proc.analyze(laps);

    }

    @Test
    public void repairLaptimes() {
        String raceEventId = "a209eb7a-4e2b-45a0-b02b-8062bbab71ec";
        int sessionNum = 2;

        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumOrderBySessionTimeAsc(raceEventId, sessionNum);
        Map<Integer, List<LapDataMetaData>> lookup = laps.stream().collect(Collectors.groupingBy(l -> l.getData().getCarIdx()));
        List<RaceLogMetaData> raceLogs = raceLogDataRepository.findByRaceEventIdAndSessionNum(raceEventId, sessionNum);

        lookup.forEach((carIdx, v) -> {
            v.forEach(l -> {

                List<RaceLogMetaData> foundList = raceLogs.stream().filter(rl -> rl.getData().getCarIdxLapCompleted()[carIdx] == l.getData().getLapNo()).collect(Collectors.toList());

                RaceLogMetaData found = raceLogs.stream().filter(rl -> rl.getData().getCarIdxLapCompleted()[carIdx] == l.getData().getLapNo()).findFirst().get();
                Optional<RaceLogMetaData> firstChange = foundList.stream()
                        .filter(rl -> rl.getData().getCarIdxLapCompleted()[carIdx] == l.getData().getLapNo())
                        .filter(rl -> rl.getData().getCarIdxLastLapTime()[carIdx] != -1)
                        .filter(rl -> rl.getData().getCarIdxLastLapTime()[carIdx] != found.getData().getCarIdxLastLapTime()[carIdx])
                        .findFirst();

                /**
                 * Note: this doesn't work in all cases.
                 * At the moment, I have no real rule when to use which laptime. This variant may
                 * solve one or the other problem but causes another.
                 * Best way to solve may be to have both laptimes available at LapData and let some
                 * admin decide which one to use when data problems are encountered.
                 */
                float newLapTime = firstChange.map(fc -> fc.getData().getCarIdxLastLapTime()[carIdx])
                        .orElseGet(() -> {
                            var x = found.getData().getCarIdxLastLapTime()[carIdx];
                            if (l.getData().isIncomplete()) {
                                return x;
                            } else {
                                if (foundList.stream().anyMatch(rl -> rl.getData().getCarIdxLastLapTime()[carIdx] != x)) {
                                    return l.getData().getLapTime();
                                } else {
                                    return x;
                                }
                            }
                        });
                // log.debug(String.format("L: %3d Own: %.3f iR: %s (%.3f)", l.getData().getLapNo(), l.getData().getLapTime(),
                //         firstChange.map(fc -> String.format("%.3f (%d)", fc.getData().getCarIdxLastLapTime()[carIdx], foundList.indexOf(fc))).orElse("n.a."),
                //         found.getData().getCarIdxLastLapTime()[carIdx]));
                l.getData().setLapTime(newLapTime);
            });
        });
        lapDataRepository.saveAll(laps);
    }

}
