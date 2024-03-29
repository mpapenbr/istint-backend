package de.mp.istint.server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.mp.istint.server.model.Event;
import de.mp.istint.server.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@DataMongoTest
public class TestEventRepository extends TCBaseMongoDb {

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

    @Test
    public void testStealOwner() {
        var demoEvent = Event.builder().carName("Car").trackName("track").name("DemoEvent").owner(demoUser).build();
        var realEvent = Event.builder().carName("Car").trackName("track").name("RealEvent").owner(demoUser).build();

        mongoTemplate.save(demoEvent);
        mongoTemplate.save(realEvent);
        mongoTemplate.remove(demoUser);
        // note: the owner is just removed from the the entities ;) the value is now
        // null
        var check = eventRepository.findByNameRegex("Event");
        var expect = Arrays.asList(new Object[] { null, null });
        assertEquals(expect, check.stream().map(item -> item.getOwner()).collect(Collectors.toList()));
    }

    @Test
    public void testWithDummyData() throws Exception {
        ObjectMapper om = new ObjectMapper();
        var rawData = DummyRawData.builder().stints(List.of("s1", "s2")).car(Car.builder().id("13283").name("someCar").build()).build();
        var demoEvent = Event.builder().carName("Car").trackName("track").name("DemoEvent").owner(demoUser).rawData(rawData).build();

        mongoTemplate.save(demoEvent);

        var check = eventRepository.findById(demoEvent.id);
        System.out.println("TestEventRepository.testWithDummyData() " + check.toString());
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class DummyRawData {
        List<String> stints;
        Car car;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class Car {
        String id;
        String name;
    }

}