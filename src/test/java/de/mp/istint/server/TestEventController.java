package de.mp.istint.server;

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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import de.mp.istint.server.model.Event;

/*
 * This class tests one controller only. Note, it can be combined with security out of the box.
 * @author mpapenbr
 *
 */
@SpringBootTest

public class TestEventController {

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
            http.authorizeRequests()
                    .antMatchers("/**")
                    .authenticated()

                    .and()
                    .httpBasic();
        }

        public UserDetailsService myUserDetailsService() {
            return new MyTestUserDetailsService();
        }
    }

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())

                .build();
        mongoTemplate.dropCollection(Event.class);

    }

    @WithMockUser
    @Test
    void testPublic() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/events", UUID.randomUUID().toString()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testProtectedNoUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/private/some"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @WithAnonymousUser
    @Test
    void testProtectedAnonymous() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/events"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @WithMockUser
    @Test
    void testProtectedWithSomeMockUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/private/some"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}