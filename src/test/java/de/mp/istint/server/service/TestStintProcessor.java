package de.mp.istint.server.service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.mp.istint.server.model.racelog.LapData;
import de.mp.istint.server.model.racelog.LapDataMetaData;
import de.mp.istint.server.model.racelog.response.LapDataExtended;
import de.mp.istint.server.model.racelog.response.StintData;
import de.mp.istint.server.service.racelog.StintProcessor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/*
* 
* @author mpapenbr
*
*/
@Slf4j

public class TestStintProcessor {

    @Test
    public void testSummaryStandard() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(2).lapTime(20.0f).build(),
                LapData.builder().lapNo(3).lapTime(25.0f).build(),
                LapData.builder().lapNo(4).lapTime(22.0f).build(),
                LapData.builder().lapNo(5).lapTime(22.0f).inLap(true).build(),
                LapData.builder().lapNo(6).lapTime(35.0f).outLap(true).build(),
                LapData.builder().lapNo(7).lapTime(20.0f).build(),
                LapData.builder().lapNo(8).lapTime(25.0f).build(),
                LapData.builder().lapNo(9).lapTime(22.0f).inLap(true).build());
        StintProcessor proc = new StintProcessor();
        List<StintData> result = proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

        List<CheckStintData> expected = List.of(
                CheckStintData.builder().no(1).laps(List.of(1, 2, 3, 4, 5)).build(),
                CheckStintData.builder().no(2).laps(List.of(6, 7, 8, 9)).build());
        List<CheckStintData> check = createCheckStintData(result);

        Assertions.assertEquals(expected, check);

    }

    private List<CheckStintData> createCheckStintData(List<StintData> arg) {
        Function<List<LapDataExtended>, List<Integer>> extractLapNos = c -> c.stream().map(item -> item.getLapData().getLapNo()).collect(Collectors.toList());
        List<CheckStintData> ret = arg.stream().map(item -> CheckStintData.builder().no(item.getStintNo()).laps(extractLapNos.apply(item.getLaps())).build()).collect(Collectors.toList());
        return ret;
    }

    @Data
    @Builder
    static class CheckStintData {
        int no;
        List<Integer> laps;
    }

    @Test
    public void testNoEndingInLap1() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(2).lapTime(20.0f).build(),
                LapData.builder().lapNo(3).lapTime(25.0f).build(),
                LapData.builder().lapNo(4).lapTime(22.0f).build(),
                LapData.builder().lapNo(5).lapTime(22.0f).inLap(true).build(),
                LapData.builder().lapNo(6).lapTime(35.0f).outLap(true).build(),
                LapData.builder().lapNo(7).lapTime(20.0f).build(),
                LapData.builder().lapNo(8).lapTime(25.0f).build(),
                LapData.builder().lapNo(9).lapTime(22.0f).build());
        StintProcessor proc = new StintProcessor();
        List<StintData> result = proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

        List<CheckStintData> expected = List.of(
                CheckStintData.builder().no(1).laps(List.of(1, 2, 3, 4, 5)).build(),
                CheckStintData.builder().no(2).laps(List.of(6, 7, 8, 9)).build());
        List<CheckStintData> check = createCheckStintData(result);

        Assertions.assertEquals(expected, check);
    }

    @Test
    public void testNoEndingInLap2() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(2).lapTime(20.0f).build(),
                LapData.builder().lapNo(3).lapTime(25.0f).build());
        StintProcessor proc = new StintProcessor();
        List<StintData> result = proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

        List<CheckStintData> expected = List.of(
                CheckStintData.builder().no(1).laps(List.of(1, 2, 3)).build());
        List<CheckStintData> check = createCheckStintData(result);

        Assertions.assertEquals(expected, check);
    }

    @Test
    public void testOneStintLap() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(2).lapTime(20.0f).build(),
                LapData.builder().lapNo(3).lapTime(25.0f).inLap(true).build());
        StintProcessor proc = new StintProcessor();
        List<StintData> result = proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));
        List<CheckStintData> expected = List.of(
                CheckStintData.builder().no(1).laps(List.of(1, 2, 3)).build());
        List<CheckStintData> check = createCheckStintData(result);

        Assertions.assertEquals(expected, check);
    }

    @Test
    public void testOutInLap() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(2).lapTime(25.0f).inLap(true).build());
        StintProcessor proc = new StintProcessor();
        List<StintData> result = proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));
        List<CheckStintData> expected = List.of(
                CheckStintData.builder().no(1).laps(List.of(1, 2)).build());

        List<CheckStintData> check = createCheckStintData(result);

        Assertions.assertEquals(expected, check);

    }

    // seen in NEO Race Silverstone Race CarIdx 22, MSI Blue #44
    @Test
    public void testInInBothLap() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(2).lapTime(25.0f).inLap(true).build(),
                LapData.builder().lapNo(3).lapTime(25.0f).inLap(true).build());
        ;
        StintProcessor proc = new StintProcessor();
        List<StintData> result = proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));
        List<CheckStintData> expected = List.of(
                CheckStintData.builder().no(1).laps(List.of(1, 2)).build(),
                CheckStintData.builder().no(2).laps(List.of(3)).build());
        List<CheckStintData> check = createCheckStintData(result);

        Assertions.assertEquals(expected, check);

    }

    // seen in NEO Race Silverstone Race CarIdx 46, MSI Blue #44
    // could be Speeding-in-Pitlane-Penalty which is served in the next lap
    @Test
    public void testPenaltyLap() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(2).lapTime(25.0f).inLap(true).outLap(true).build(),
                LapData.builder().lapNo(3).lapTime(25.0f).outLap(true).build(),
                LapData.builder().lapNo(4).lapTime(25.0f).build(),
                LapData.builder().lapNo(5).lapTime(25.0f).build());
        ;
        StintProcessor proc = new StintProcessor();
        List<StintData> result = proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));
        List<CheckStintData> expected = List.of(
                CheckStintData.builder().no(1).laps(List.of(1)).build(),
                CheckStintData.builder().no(2).laps(List.of(2)).build(),
                CheckStintData.builder().no(3).laps(List.of(3, 4, 5)).build());
        List<CheckStintData> check = createCheckStintData(result);

        Assertions.assertEquals(expected, check);

    }

    @Test
    public void testOutOutLap() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(0).lapTime(30.0f).build(),
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(2).lapTime(25.0f).outLap(true).build());
        StintProcessor proc = new StintProcessor();
        List<StintData> result = proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));
        List<CheckStintData> expected = List.of(
                CheckStintData.builder().no(1).laps(List.of(1)).build(),
                CheckStintData.builder().no(2).laps(List.of(2)).build());
        List<CheckStintData> check = createCheckStintData(result);

        Assertions.assertEquals(expected, check);
    }

    @Test
    public void testOutLap() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(25.0f).outLap(true).build());
        StintProcessor proc = new StintProcessor();
        List<StintData> result = proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

        List<CheckStintData> expected = List.of(
                CheckStintData.builder().no(1).laps(List.of(1)).build());
        List<CheckStintData> check = createCheckStintData(result);

        Assertions.assertEquals(expected, check);
    }

}