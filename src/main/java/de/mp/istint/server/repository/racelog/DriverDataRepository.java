package de.mp.istint.server.repository.racelog;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.mp.istint.server.model.racelog.DriverMetaData;

public interface DriverDataRepository extends MongoRepository<DriverMetaData, UUID> {
    long deleteByRaceEventId(String raceEventId);

    List<DriverMetaData> findByRaceEventId(String raceEventId);
}
