package de.mp.istint.server.repository;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
    protected static MongoDBContainer mongo = new MongoDBContainer("mongo:5");

    @DynamicPropertySource
    protected static void registerMongoDbProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.data.mongodb.host", mongo::getHost);
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);

    }

}
