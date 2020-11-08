package de.mp.istint.server.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import de.mp.istint.server.model.Event;
import de.mp.istint.server.model.User;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByName(@Param("name") String name);

    List<Event> findByNameRegex(@Param("name") String name);

    List<Event> findByOwner(/* @Param("owner") */ User owner);

    List<Event> findByOwnerId(@Param("ownerId") String ownerId);

}