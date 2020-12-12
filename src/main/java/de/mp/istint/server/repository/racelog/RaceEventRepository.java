package de.mp.istint.server.repository.racelog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import de.mp.istint.server.model.racelog.RaceEvent;

public interface RaceEventRepository extends MongoRepository<RaceEvent, UUID> {

    List<RaceEvent> findBySessionId(@Param("sessionId") Long sessionId);

    Optional<RaceEvent> findBySessionIdAndOwnerId(@Param("sessionId") Long sessionId, @Param("ownerId") String ownerId);

    List<RaceEvent> findByOwnerId(@Param("ownerId") String ownerId);
}
