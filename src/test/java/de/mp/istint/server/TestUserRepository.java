package de.mp.istint.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class TestUserRepository {

    @Test
    public void testAdd(@Autowired MongoTemplate mongoTemplate) {
        var u = new User(null, "test");

        var ret = mongoTemplate.save(u);

    }
}