package de.mp.istint.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import de.mp.istint.server.model.Event;
import de.mp.istint.server.model.User;

// @AutoConfigureMockMvc
// @WithMockUser(username = "demoUser")
// @ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS) // dann kann man auch Spring-Autowires in @BeforeAll verwenden.
@WithUserDetails(value = "demoUser", userDetailsServiceBeanName = "testUserDetails")
public class TestEventController {

    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EventRepository eventRepository;

    private User demoUser;

    @TestConfiguration
    static class MyContext {

        @Bean("testUserDetails")
        public UserDetailsService myUserDetailsService() {
            return new MyTestUserDetailsService();
        }
    }

    @BeforeAll
    public void setupOnce() {
        mongoTemplate.dropCollection(User.class);
        demoUser = mongoTemplate.save(User.builder().name("demoUser").email("demoUser").build());
    }

    @BeforeEach
    public void prepareTest() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        mongoTemplate.dropCollection(Event.class);

    }

    @Test

    public void testCreate() throws Exception {

        var event = Event.builder().carName("Car").trackName("track").name("DemoEvent")

                .build();
        String data = om.writeValueAsString(event);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(data))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        // re-read the data and compare
        var checkUrl = result.getResponse().getHeader("Location");

    }

    @WithAnonymousUser
    @Test
    public void testCreateAnonymous() throws Exception {

        var event = Event.builder().carName("Car").trackName("track").name("DemoEvent").build();
        String data = om.writeValueAsString(event);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/events").content(data))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.UNAUTHORIZED.value()))
                .andReturn();

    }

    @Test
    public void testCreateAndRead() throws Exception {

        var event = Event.builder().carName("Car").trackName("track").name("DemoEvent").build();
        String data = om.writeValueAsString(event);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(data))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
        // re-read the data and compare
        var checkUrl = result.getResponse().getHeader("Location");
        var id = checkUrl.substring(checkUrl.lastIndexOf("/"));
        result = this.mockMvc.perform(MockMvcRequestBuilders.get("/events/" + id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var check = om.readValue(result.getResponse().getContentAsString(), Event.class);
        assertTrue(new ReflectionEquals(event, "id", "owner").matches(check));
        assertTrue(new ReflectionEquals(demoUser, "id").matches(check.getOwner()));
        // assertEquals(demoUser, check.getOwner());
    }
}