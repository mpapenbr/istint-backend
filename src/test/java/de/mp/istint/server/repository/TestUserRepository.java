package de.mp.istint.server.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import de.mp.istint.server.model.User;
import lombok.extern.slf4j.Slf4j;

@DataMongoTest
@Slf4j
public class TestUserRepository extends TCBaseMongoDb {

    @Test
    public void testAdd(@Autowired MongoTemplate mongoTemplate) {
        var u = User.builder().name("test").build();

        var ret = mongoTemplate.save(u);
        log.info("{}", ret);

    }
}