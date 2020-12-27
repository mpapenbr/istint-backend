package de.mp.istint.server.repository.racelog;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.mp.istint.server.model.racelog.LapDataMetaData;

public interface LapDataRepository extends MongoRepository<LapDataMetaData, UUID> {
    long deleteByRaceEventId(String raceEventId);
}
