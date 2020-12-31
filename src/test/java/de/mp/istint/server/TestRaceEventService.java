package de.mp.istint.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
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
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import de.mp.istint.server.model.racelog.DriverData;
import de.mp.istint.server.model.racelog.DriverMetaData;
import de.mp.istint.server.repository.racelog.DriverDataRepository;
import de.mp.istint.server.service.racelog.RaceEventService;
import lombok.extern.slf4j.Slf4j;

/*
 * This class tests one controller only. Note, it can be combined with security out of the box.
 * @author mpapenbr
 *
 */
@Slf4j
@SpringBootTest

public class TestRaceEventService {

    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DriverDataRepository driverDataRepository;

    @Autowired
    private RaceEventService raceEventService;

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
        mongoTemplate.getCollectionNames().forEach(c -> mongoTemplate.dropCollection(c));

    }

    @Test
    public void condenseDriverData() {
        DriverData a = createDriver(1, "A");
        DriverData b = createDriver(1, "B");
        DriverData x = createDriver(2, "B");

        driverDataRepository.save(configureDriverMeta().data(a).sessionTime(1).build());
        driverDataRepository.save(configureDriverMeta().data(b).sessionTime(2).build());
        driverDataRepository.save(configureDriverMeta().data(b).sessionTime(3).build());
        driverDataRepository.save(configureDriverMeta().data(x).sessionTime(3).build()); // on different car
        driverDataRepository.save(configureDriverMeta().data(x).sessionTime(4).build()); // on different car
        driverDataRepository.save(configureDriverMeta().data(a).sessionTime(4).build());
        driverDataRepository.save(configureDriverMeta().data(a).sessionTime(5).build());

        driverDataRepository.save(configureDriverMeta().data(a).sessionTime(1).sessionNum(0).build()); // other session
        driverDataRepository.save(configureDriverMeta().data(a).sessionTime(12).sessionNum(132).build()); // other session
        driverDataRepository.save(configureDriverMeta().data(a).sessionTime(1).sessionNum(132).build()); // other session
        driverDataRepository.save(configureDriverMeta().data(a).sessionTime(1).raceEventId("2").build()); // other race

        List<DriverMetaData> data = raceEventService.getDriversCondensed("1");
        data.forEach(d -> log.debug("{} {} {} {} {}", d.getId(), d.getData().getCarIdx(), d.getSessionNum(), d.getSessionTime(), d.getData().getUserName()));

        var expected = List.of(
                configureDriverMeta().data(a).sessionNum(0).sessionTime(1).build(),
                configureDriverMeta().data(a).sessionNum(1).sessionTime(1).build(),
                configureDriverMeta().data(b).sessionNum(1).sessionTime(2).build(),
                configureDriverMeta().data(x).sessionNum(1).sessionTime(3).build(),
                configureDriverMeta().data(a).sessionNum(1).sessionTime(4).build(),
                configureDriverMeta().data(a).sessionNum(132).sessionTime(1).build());
        assertEquals(data.size(), expected.size());
        int i = 0;
        for (DriverMetaData item : expected) {
            assertEquals(item.getSessionNum(), data.get(i).getSessionNum());
            assertEquals(item.getSessionTime(), data.get(i).getSessionTime());
            assertEquals(item.getData(), data.get(i).getData());
            i++;
        }
    }

    DriverMetaData.DriverMetaDataBuilder configureDriverMeta() {
        return DriverMetaData.builder().id(UUID.randomUUID()).raceEventId("1").sessionNum(1);
    }

    DriverData createDriver(int carIdx, String userName) {
        DriverData d = new DriverData();
        d.setCarIdx(carIdx);
        d.setUserName(userName);
        return d;
    }

}