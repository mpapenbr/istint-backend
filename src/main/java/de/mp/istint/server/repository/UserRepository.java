package de.mp.istint.server.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import de.mp.istint.server.model.User;

@RepositoryRestResource(exported = false)
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findOptionalById(@Param("id") String name);

    Optional<User> findOptionalByName(@Param("name") String name);

}