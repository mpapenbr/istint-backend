package de.mp.istint.server.service;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.mp.istint.server.model.racelog.LapData;
import de.mp.istint.server.model.racelog.LapDataMetaData;
import de.mp.istint.server.service.racelog.StintProcessor;
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
        proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

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
        proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

    }

    @Test
    public void testNoEndingInLap2() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(2).lapTime(20.0f).build(),
                LapData.builder().lapNo(3).lapTime(25.0f).build());
        StintProcessor proc = new StintProcessor();
        proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

    }

    @Test
    public void testOneStintLap() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(2).lapTime(20.0f).build(),
                LapData.builder().lapNo(3).lapTime(25.0f).inLap(true).build());
        StintProcessor proc = new StintProcessor();
        proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

    }

    @Test
    public void testOutInLap() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(3).lapTime(25.0f).inLap(true).build());
        StintProcessor proc = new StintProcessor();
        proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

    }

    @Test
    public void testOutOutLap() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(30.0f).outLap(true).build(),
                LapData.builder().lapNo(3).lapTime(25.0f).outLap(true).build());
        StintProcessor proc = new StintProcessor();
        proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

    }

    @Test
    public void testOutLap() {
        List<LapData> data = List.of(
                LapData.builder().lapNo(1).lapTime(25.0f).outLap(true).build());
        StintProcessor proc = new StintProcessor();
        proc.analyze(data.stream().map(d -> LapDataMetaData.builder().data(d).raceEventId("1").sessionTime(1).build()).collect(Collectors.toList()));

    }

}