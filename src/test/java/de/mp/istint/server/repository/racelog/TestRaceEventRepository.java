package de.mp.istint.server.repository.racelog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.mp.istint.server.model.racelog.RaceEvent;

@DataMongoTest
@ExtendWith(SpringExtension.class)

public class TestRaceEventRepository {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RaceEventRepository raceEventRepository;

    @Test
    public void shouldCreateEvent() {
        raceEventRepository.save(RaceEvent.builder().id(UUID.randomUUID()).build());
    }

    @Test
    public void shouldFindExistingEvent() {
        UUID id = UUID.randomUUID();
        RaceEvent entry = raceEventRepository.save(RaceEvent.builder().id(id).build());
        Optional<RaceEvent> result = raceEventRepository.findById(id);
        assertEquals(Optional.of(entry), result);
    }

    @Test
    public void shouldDetectNonExistingEvent() {
        UUID id = UUID.randomUUID();
        Optional<RaceEvent> result = raceEventRepository.findById(id);
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldFindByEntries() {
        RaceEvent event1 = raceEventRepository.save(RaceEvent.builder().ownerId("test").sessionId(1L).id(UUID.fromString("00000000-0000-0000-0000-000000000001")).build());
        RaceEvent event2 = raceEventRepository.save(RaceEvent.builder().ownerId("test").sessionId(2L).id(UUID.fromString("00000000-0000-0000-0000-000000000002")).build());
        RaceEvent event3 = raceEventRepository.save(RaceEvent.builder().ownerId("othertest").sessionId(2L).id(UUID.fromString("00000000-0000-0000-0000-000000000003")).build());

        List<RaceEvent> result = raceEventRepository.findBySessionId(0L);
        assertTrue(result.isEmpty());

        result = raceEventRepository.findBySessionId(1L);
        assertEquals(Set.copyOf(result), Set.of(event1));

        result = raceEventRepository.findByOwnerId("test");
        assertEquals(Set.copyOf(result), Set.of(event1, event2));

        result = raceEventRepository.findBySessionId(2l);
        assertEquals(Set.copyOf(result), Set.of(event2, event3));

    }

}
