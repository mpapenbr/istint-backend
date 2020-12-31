package de.mp.istint.server.repository.racelog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.mp.istint.server.model.racelog.DriverData;
import de.mp.istint.server.model.racelog.DriverMetaData;
import de.mp.istint.server.model.racelog.DriverMetaData.DriverMetaDataBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class TestDriverDataRepository {

    @Autowired
    private DriverDataRepository driverDataRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setupEach() {
        mongoTemplate.dropCollection(DriverMetaData.class);
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

        List<DriverMetaData> data = driverDataRepository.findByRaceEventIdOrderBySessionNumAscSessionTimeAsc("1");
        data.forEach(d -> log.debug("{} {} {} {} {}", d.getId(), d.getData().getCarIdx(), d.getSessionNum(), d.getSessionTime(), d.getData().getUserName()));

        // first: group by sessionNum, carIdx

        List<UUID> toRemoveEntries = new ArrayList<>();
        var bySessionNum = data.stream().collect(Collectors.groupingBy(item -> item.getSessionNum(), Collectors.toList()));
        bySessionNum.forEach((k, v) -> {
            var byCarIdx = v.stream().collect(Collectors.groupingBy(item -> item.getData().getCarIdx(), Collectors.toList()));
            // now we can collect the superflous entries
            byCarIdx.values().forEach(carDrivers -> {
                if (carDrivers.size() > 1) {
                    for (int i = 1; i < carDrivers.size(); i++) {
                        var current = carDrivers.get(i);
                        var prev = carDrivers.get(i - 1);
                        if (prev.getData().getUserName().equals(current.getData().getUserName())) {
                            toRemoveEntries.add(current.getId());
                        }
                    }
                }
            });
        });

        System.out.println("TestDriverDataRepository.condenseDriverData()" + toRemoveEntries);

        data.removeIf(d -> toRemoveEntries.contains(d.getId()));
        /**
         * what do we need?
         * a driver entry is "valid" until another driver at time X enters the car.
         */

        data.forEach(d -> log.debug("{} {} {} {} {}", d.getId(), d.getData().getCarIdx(), d.getSessionNum(), d.getSessionTime(), d.getData().getUserName()));
    }

    DriverMetaDataBuilder configureDriverMeta() {
        return DriverMetaData.builder().id(UUID.randomUUID()).raceEventId("1").sessionNum(1);
    }

    DriverData createDriver(int carIdx, String userName) {
        DriverData d = new DriverData();
        d.setCarIdx(carIdx);
        d.setUserName(userName);
        return d;
    }
}
