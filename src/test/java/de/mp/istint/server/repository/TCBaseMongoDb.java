package de.mp.istint.server.repository;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * The @DirtiesContext annotation is required here. If it is omitted, the tests will run into
 * timeout issues when waiting for the mongo cluster to be available.
 */
@Testcontainers
@DirtiesContext
public abstract class TCBaseMongoDb {
    @Container
    @ServiceConnection
    protected static MongoDBContainer mongo = new MongoDBContainer("mongo:5");

}
