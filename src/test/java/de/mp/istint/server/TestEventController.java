package de.mp.istint.server;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.mp.istint.server.config.WithMyOwnUser;
import de.mp.istint.server.model.Event;

/*
 * This class tests one controller only. Note, it can be combined with security out of the box.
 * @author mpapenbr
 *
 */
@SpringBootTest(properties = { "de.flapdoodle.mongodb.embedded.version=5.0.5" })

public class TestEventController {

    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MongoTemplate mongoTemplate;

    // @TestConfiguration
    // @EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
    // class MyContext {
    //     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //         return http.authorizeHttpRequests(ar -> ar.requestMatchers("/**").authenticated())
    //                 .csrf(c -> c.disable())
    //                 .build(); // do this or have any POST have crsf() called in mock

    //     }

    // }

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())

                .build();
        mongoTemplate.dropCollection(Event.class);

    }

    @WithMyOwnUser(id = "12")
    @Test
    void testOwnEventsWithMyOwnStuff() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/events/own"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
        // should be empty

    }

    @WithMyOwnUser(id = "12")
    @Test
    void testAllEventsWithMyOwnStuff() throws Exception {
        // Note: even if both /events and /events/own have no "real" results, the response differs. Here we get an empty _embedded events. 
        // In the above test we just get no result
        mockMvc.perform(MockMvcRequestBuilders.get("/events"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.events").isEmpty());
        // should be empty

    }

    @WithAnonymousUser
    @Test
    void testProtectedAnonymous() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/events"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @WithMyOwnUser(id = "12")
    @Test
    void testCreateAndRead() throws Exception {
        Event e = Event.builder().id(UUID.randomUUID().toString()).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(e)))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());
        // verify we find it again
        mockMvc.perform(MockMvcRequestBuilders.get("/events/own"))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.events").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.events[0].ownerId").value("12"));
    }

    @WithMyOwnUser(id = "12")
    @Test
    void testOverrideOwnContent() throws Exception {
        // persist one entry with user id 34
        var myEvent = Event.builder().id(UUID.randomUUID().toString()).carName("Car").trackName("track").name("DemoEvent").ownerId("12").build();
        mongoTemplate.save(myEvent);

        Event myEventUpdate = Event.builder().carName("OtherCar").id(myEvent.getId()).build();
        mockMvc.perform(MockMvcRequestBuilders.put("/events/{id}", myEvent.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(myEventUpdate)))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.carName").value("OtherCar"));

    }

    @WithMyOwnUser(id = "12")
    @Test
    void testDoNotOverrideOtherUsersContent() throws Exception {
        // persist one entry with user id 34
        var otherEvent = Event.builder().id("001").carName("Car").trackName("track").name("DemoEvent").ownerId("34").build();
        mongoTemplate.save(otherEvent);

        Event myEvent = Event.builder().id("001").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(myEvent)))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());

    }

    @WithMyOwnUser(id = "12")
    @Test
    void testDeleteOwnContent() throws Exception {
        // persist one entry with user id 34
        var myEvent = Event.builder().id(UUID.randomUUID().toString()).carName("Car").trackName("track").name("DemoEvent").ownerId("12").build();
        mongoTemplate.save(myEvent);

        mockMvc.perform(MockMvcRequestBuilders.delete("/events/{id}", myEvent.getId()))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

    }

    @WithMyOwnUser(id = "12")
    @Test
    void testReadOwnContentViaGet() throws Exception {
        // persist one entry with user id 34
        var myEvent = Event.builder().id(UUID.randomUUID().toString()).carName("Car").trackName("track").name("DemoEvent").ownerId("12").build();
        mongoTemplate.save(myEvent);

        mockMvc.perform(MockMvcRequestBuilders.get("/events/{id}", myEvent.getId()))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("DemoEvent"));

    }

    @WithMyOwnUser(id = "12")
    @Test
    void testDoNotDeleteOtherUsersContent() throws Exception {
        // persist one entry with user id 34
        var otherEvent = Event.builder().id(UUID.randomUUID().toString()).carName("Car").trackName("track").name("DemoEvent").ownerId("34").build();
        mongoTemplate.save(otherEvent);

        mockMvc.perform(MockMvcRequestBuilders.delete("/events/{id}", otherEvent.getId()))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());

    }

}