package de.mp.istint.server;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByName(@Param("name") String name);

    List<Event> findByNameRegex(@Param("name") String name);

    List<Event> findByOwner(/* @Param("owner") */ User owner);

}