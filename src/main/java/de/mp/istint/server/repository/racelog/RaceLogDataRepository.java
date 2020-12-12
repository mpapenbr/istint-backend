package de.mp.istint.server.repository.racelog;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.mp.istint.server.model.racelog.RaceLogData;

public interface RaceLogDataRepository extends MongoRepository<RaceLogData,UUID>{
    

}
