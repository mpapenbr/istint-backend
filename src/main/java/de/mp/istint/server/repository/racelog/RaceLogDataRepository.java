package de.mp.istint.server.repository.racelog;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import de.mp.istint.server.model.racelog.RaceLogMetaData;

@RestResource(exported = false)
public interface RaceLogDataRepository extends MongoRepository<RaceLogMetaData, String>, RaceLogDataAddOns {
    long deleteByRaceEventId(String raceEventId);

    // currently for debug only
    List<RaceLogMetaData> findByRaceEventIdAndSessionNum(@Param("raceEventId") String raceEventId, @Param("sessionNum") int sessionNum);

    List<RaceLogMetaData> findByRaceEventIdAndSessionNumAndSessionTimeBetweenOrderBySessionTimeAsc(@Param("raceEventId") String raceEventId, @Param("sessionNum") int sessionNum, @Param("startTime") float startTime, @Param("endTime") float endTime);

    List<RaceLogMetaData> findByRaceEventIdAndSessionNumAndSessionTickInOrderBySessionTimeAsc(@Param("raceEventId") String raceEventId, @Param("sessionNum") int sessionNum, @Param("sessionTick") List<Integer> sessionTicks);
}
