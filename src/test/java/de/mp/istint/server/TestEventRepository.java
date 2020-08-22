package de.mp.istint.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class TestEventRepository {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private EventRepository eventRepository;

    private User demoUser;

    @BeforeEach
    public void ensureDemoUser() {
        mongoTemplate.dropCollection(Event.class);
        demoUser = mongoTemplate.save(User.builder().name("DemoUser").build());
    }

    @Test
    public void testAdd() {

        var event = Event.builder().carName("Car").trackName("track").name("DemoEvent").owner(demoUser).build();
        mongoTemplate.save(event);
        var check = mongoTemplate.findById(event.id, Event.class);
        assertEquals(event, check);

    }

    @Test
    public void testExplicitQueryByName() {

        var event = Event.builder().carName("Car").trackName("track").name("DemoEvent").owner(demoUser).build();
        mongoTemplate.save(event);

        var check = mongoTemplate.query(Event.class).matching(query(where("name").regex("^Demo"))).firstValue();

        assertEquals(event, check);
    }

    @Test
    public void testQueryByName() {

        var event = Event.builder().carName("Car").trackName("track").name("DemoEvent").owner(demoUser).build();
        mongoTemplate.save(event);

        var check = eventRepository.findByName("DemoEvent");
        assertEquals(List.of(event), check);

    }

    @Test
    public void failByName() {

        var event = Event.builder().carName("Car").trackName("track").name("DemoEvent").owner(demoUser).build();
        mongoTemplate.save(event);

        var check = eventRepository.findByName("DoesNotExist");
        assertEquals(List.of(), check);

    }

    @Test
    public void testQueryByNameRegex() {
        var demoEvent = Event.builder().carName("Car").trackName("track").name("DemoEvent").owner(demoUser).build();
        var realEvent = Event.builder().carName("Car").trackName("track").name("RealEvent").owner(demoUser).build();
        mongoTemplate.save(demoEvent);
        mongoTemplate.save(realEvent);

        var check = eventRepository.findByNameRegex("Event");
        assertEquals(Set.of(demoEvent, realEvent), Set.copyOf(check)); // we don't care about the order
    }

    @Test
    public void testQueryByOwner() {
        var otherUser = mongoTemplate.save(User.builder().name("OtherUser").build());
        var demoEvent = Event.builder().carName("Car").trackName("track").name("DemoEvent").owner(demoUser).build();
        var realEvent = Event.builder().carName("Car").trackName("track").name("RealEvent").owner(demoUser).build();
        var otherEvent = Event.builder().carName("Car").trackName("track").name("OtherEvent").owner(otherUser).build();
        mongoTemplate.save(demoEvent);
        mongoTemplate.save(realEvent);
        mongoTemplate.save(otherEvent);

        // check for demoUser
        var check = eventRepository.findByOwner(demoUser);
        assertEquals(Set.of(demoEvent, realEvent), Set.copyOf(check)); // we don't care about the order

        // check for otherUser
        check = eventRepository.findByOwner(otherUser);
        assertEquals(List.of(otherEvent), check);
    }
}