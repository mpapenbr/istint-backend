package de.mp.istint.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TestEventController {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ObjectMapper om;

    @Autowired
    private EventRepository eventRepository;

    private User demoUser;

    @BeforeEach
    public void prepareTest() {
        mongoTemplate.dropCollection(Event.class);
        demoUser = mongoTemplate.save(User.builder().name("DemoUser").build());
    }

    @Test
    public void testCreateAndRead() throws Exception {
        // this.mockMvc.perform(get("/events")).andDo(print());
        var event = Event.builder().carName("Car").trackName("track").name("DemoEvent").owner(demoUser).build();
        String data = om.writeValueAsString(event);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/events").content(data))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        // re-read the data and compare
        var checkUrl = result.getResponse().getHeader("Location");
        var id = checkUrl.substring(checkUrl.lastIndexOf("/"));
        result = this.mockMvc.perform(MockMvcRequestBuilders.get("/events/" + id)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        var check = om.readValue(result.getResponse().getContentAsString(), Event.class);
        assertTrue(new ReflectionEquals(event, "id", "owner").matches(check));
    }
}