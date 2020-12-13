package de.mp.istint.server.repository.racelog;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.mp.istint.server.model.racelog.PitStopMetaData;


public interface PitStopDataRepository extends MongoRepository<PitStopMetaData, UUID> {
    long deleteByRaceEventId(String raceEventId);
}
