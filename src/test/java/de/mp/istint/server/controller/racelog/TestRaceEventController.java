package de.mp.istint.server.controller.racelog;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import de.mp.istint.server.config.WithMyOwnUser;
import de.mp.istint.server.model.racelog.RaceEvent;
import de.mp.istint.server.model.racelog.RaceLogData;

@SpringBootTest
public class TestRaceEventController {

    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MongoTemplate mongoTemplate;

    @TestConfiguration

    @EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
    static class MyContext extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests(ar -> ar.antMatchers("/**").authenticated())
                    .csrf(c -> c.disable()); // do this or have any POST have crsf() called in mock
        }

    }

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        mongoTemplate.dropCollection(RaceEvent.class);
        mongoTemplate.dropCollection(RaceLogData.class);

    }

    @WithMyOwnUser(id = "12")
    @Test
    void testSpringControllerGetAll() throws Exception {
        // this is the "generated" rest controller by spring data
        mockMvc.perform(MockMvcRequestBuilders.get("/raceevents"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.raceEvents").isEmpty());
        // should be empty

    }

    @WithAnonymousUser
    @Test
    void testProtectedAnonymous() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/raceevents"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMyOwnUser(id = "12")
    @Test
    void testReadOwnContentViaGet() throws Exception {
        // persist one entry with user id 34
        var myEvent = RaceEvent.builder().id(UUID.randomUUID().toString()).sessionId(34L).ownerId("12").build();
        mongoTemplate.save(myEvent);

        mockMvc.perform(MockMvcRequestBuilders.get("/raceevents/{id}", myEvent.getId()))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.sessionId").value(34L));

    }
}
