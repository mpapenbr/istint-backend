package de.mp.istint.server.repository.racelog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.mp.istint.server.model.racelog.RaceEvent;
import de.mp.istint.server.model.racelog.RaceLogMetaData;
import de.mp.istint.server.model.racelog.SessionSummary;

@DataMongoTest
@ExtendWith(SpringExtension.class)

public class TestRaceLogDataRepository {

    @Autowired
    private RaceEventRepository raceEventRepository;
    @Autowired
    private RaceLogDataRepository raceLogDataRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setupEach() {
        mongoTemplate.dropCollection(RaceLogMetaData.class);
    }

    @Test
    public void shouldNotStoreWithoutRaceEvent() {
        assertThrows(NullPointerException.class, () -> raceLogDataRepository.save(RaceLogMetaData.builder().build()));
    }

    @Test
    public void shouldStoreWithRaceEvent() {
        RaceEvent event = raceEventRepository.save(RaceEvent.builder().id(UUID.randomUUID().toString()).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId(event.getId()).build());
    }

    @Test
    public void summaryTest() {
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(12.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(24.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(36.0f).build());

        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(1).sessionTime(45.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(1).sessionTime(87.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(1).sessionTime(99.0f).build());

        List<SessionSummary> res = raceLogDataRepository.getSummaryBySession("1");
        assertEquals(List.of(
                SessionSummary.builder().sessionNum(0).minTime(12f).maxTime(36f).count(3).build(),
                SessionSummary.builder().sessionNum(1).minTime(45f).maxTime(99f).count(3).build())

                , res);

    }

    @Test
    public void racedataTimeRange() {
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(12.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(24.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(36.0f).build());

        Object res = raceLogDataRepository.findByRaceEventIdAndSessionNumAndSessionTimeBetweenOrderBySessionTimeAsc("1", 0, 11, 25);
        System.out.println(res);
    }

}
