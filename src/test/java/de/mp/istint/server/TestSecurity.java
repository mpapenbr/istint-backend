package de.mp.istint.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
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

/**
 * Die bleiben hier mal stehen als Merker, was alles so ausprobiert wurde.
 */
// @AutoConfigureMockMvc
// @ContextConfiguration(classes = { SecurityConfig.class})
// @WebAppConfiguration // ? klären, was das macht
// @ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS) // dann kann man auch Spring-Autowires in @BeforeAll verwenden.
public class TestSecurity {

    @TestConfiguration
    static class MyContext {

        /**
         * Varianten:
         * Wenn man es via "@WithUserDetails(value = "demoUser", userDetailsServiceBeanName =
         * "dings")" verwenden will,
         * dann,
         * a) als @Bean("dings") und der Methodenname ist egal
         * oder
         * b) @Bean
         * public UserDetailsService dings()
         * 
         * Spring-Magic halt ;)
         * 
         */
        @Bean("dings")
        public UserDetailsService myUserDetailsService() {
            return new MyTestUserDetailsService();
        }
    }

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MongoTemplate mongoTemplate;

    private MockMvc mockMvc;

    @BeforeAll
    public void setupOnce() {
        mongoTemplate.dropCollection(User.class);
        var demoUser = mongoTemplate.save(User.builder().name("demoUser").email("demoUser").build());

    }

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        mongoTemplate.dropCollection(Event.class);

    }

    @Test
    @WithUserDetails(value = "demoUser", userDetailsServiceBeanName = "dings")
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
}