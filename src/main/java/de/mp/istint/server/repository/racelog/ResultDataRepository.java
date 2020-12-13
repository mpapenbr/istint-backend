package de.mp.istint.server.repository.racelog;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.mp.istint.server.model.racelog.ResultMetaData;

public interface ResultDataRepository extends MongoRepository<ResultMetaData, UUID> {
    long deleteByRaceEventId(String raceEventId);
}
