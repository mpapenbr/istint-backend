package de.mp.istint.server.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.mp.istint.server.model.racelog.LapDataMetaData;
import de.mp.istint.server.model.racelog.RaceEvent;
import de.mp.istint.server.model.racelog.RaceLogMetaData;
import de.mp.istint.server.repository.racelog.LapDataRepository;
import de.mp.istint.server.repository.racelog.RaceEventRepository;
import de.mp.istint.server.repository.racelog.RaceLogDataRepository;
import de.mp.istint.server.repository.racelog.ResultDataRepository;

@ActiveProfiles({ "realdb" })
// exclude is required because we don't want the embedded in-memory flapdoodle to be instantiated when checking things against real data
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ExtendWith(SpringExtension.class)

public class CheckInfluxExport {
    @Autowired
    private RaceLogDataRepository raceLogDataRepository;
    @Autowired
    private LapDataRepository lapDataRepository;
    @Autowired
    private ResultDataRepository resultDataRepository;
    @Autowired
    private RaceEventRepository raceEventRepository;

    @Test
    public void testExportLaps() throws FileNotFoundException {
        String raceEventId = "14998128-7246-4d73-a063-f1ae20398eef";
        RaceEvent raceEvent = raceEventRepository.findById(raceEventId).get();

        List<LapDataMetaData> laps = lapDataRepository.findByRaceEventIdAndSessionNumOrderBySessionTimeAsc(raceEventId, 2);
        try (PrintStream ps = new PrintStream(new FileOutputStream("data.influx"))) {

            laps.stream()
                    // .skip(100)
                    // .limit(10)
                    .forEach(l -> {
                        LocalDateTime current = raceEvent.getEventStart().plus(Float.valueOf(l.getSessionTime() * 1000).intValue(), ChronoUnit.MILLIS);
                        String v = String.format("lap,no=%d,carIdx=%d laptime=%.3f %d", l.getData().getLapNo(), l.getData().getCarIdx(), l.getData().getLapTime(), current.toInstant(ZoneOffset.UTC).toEpochMilli());
                        ps.println(v);
                    });
        }
    }

    @Test
    public void testExportTemps() throws FileNotFoundException {
        String raceEventId = "14998128-7246-4d73-a063-f1ae20398eef";
        RaceEvent raceEvent = raceEventRepository.findById(raceEventId).get();

        List<RaceLogMetaData> data = raceLogDataRepository.findByRaceEventIdAndSessionNum(raceEventId, 2);
        try (PrintStream ps = new PrintStream(new FileOutputStream("temps.influx", false))) {

            data.stream()
                    // .skip(100)
                    // .limit(10)
                    .forEach(d -> {
                        LocalDateTime stamp = raceEvent.getEventStart().plus(Float.valueOf(d.getSessionTime() * 1000).intValue(), ChronoUnit.MILLIS);
                        String v = String.format("temp,session=%d air=%.2f,track=%.2f,crew=%.2f,airDens=%.2f,airPres=%.2f %d",
                                d.getSessionNum(),
                                d.getData().getAirTemp(),
                                d.getData().getTrackTemp(),
                                d.getData().getTrackTempCrew(),
                                d.getData().getAirDensity(),
                                d.getData().getAirPressure(),
                                stamp.toInstant(ZoneOffset.UTC).toEpochMilli());
                        ps.println(v);
                    });
        }
    }

}
