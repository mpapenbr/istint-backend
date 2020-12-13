package de.mp.istint.server.repository.racelog;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import de.mp.istint.server.model.racelog.RaceLogMetaData;

@RestResource(exported = false)
public interface RaceLogDataRepository extends MongoRepository<RaceLogMetaData, UUID> {
    long deleteByRaceEventId(String raceEventId);

}
