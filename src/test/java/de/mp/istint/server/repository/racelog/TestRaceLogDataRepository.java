package de.mp.istint.server.repository.racelog;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.mp.istint.server.model.racelog.RaceEvent;
import de.mp.istint.server.model.racelog.RaceLogData;

@DataMongoTest
@ExtendWith(SpringExtension.class)

public class TestRaceLogDataRepository {

    @Autowired
    private RaceEventRepository raceEventRepository;
    @Autowired
    private RaceLogDataRepository raceLogDataRepository;

    @Test
    public void shouldNotStoreWithoutRaceEvent() {
        assertThrows(NullPointerException.class, () -> raceLogDataRepository.save(RaceLogData.builder().build()));
    }

    @Test
    public void shouldStoreWithRaceEvent() {
        RaceEvent event = raceEventRepository.save(RaceEvent.builder().id(UUID.randomUUID()).build());
        raceLogDataRepository.save(RaceLogData.builder().id(UUID.randomUUID()).raceEventId(event.getId()).build());
    }

}
