package de.mp.istint.server.repository.racelog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import de.mp.istint.server.model.racelog.RaceEvent;

@RepositoryRestResource(path = "raceevents")
@CrossOrigin
public interface RaceEventRepository extends MongoRepository<RaceEvent, String> {

    List<RaceEvent> findBySessionId(@Param("sessionId") Long sessionId);

    Optional<RaceEvent> findBySessionIdAndOwnerId(@Param("sessionId") Long sessionId, @Param("ownerId") String ownerId);

    List<RaceEvent> findByOwnerId(@Param("ownerId") String ownerId);
}
