package de.mp.istint.server.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.mp.istint.server.model.User;

@DataMongoTest(properties = { "de.flapdoodle.mongodb.embedded.version=5.0.5" })
@ExtendWith(SpringExtension.class)
public class TestUserRepository {

    @Test
    public void testAdd(@Autowired MongoTemplate mongoTemplate) {
        var u = User.builder().name("test").build();

        var ret = mongoTemplate.save(u);

    }
}