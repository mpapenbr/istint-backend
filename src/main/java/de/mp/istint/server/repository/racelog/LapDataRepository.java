package de.mp.istint.server.repository.racelog;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import de.mp.istint.server.model.racelog.LapDataMetaData;

public interface LapDataRepository extends MongoRepository<LapDataMetaData, UUID> {
    long deleteByRaceEventId(String raceEventId);

    List<LapDataMetaData> findByRaceEventIdAndSessionNumOrderBySessionTimeAsc(@Param("raceEventId") String raceEventId, @Param("sessionNum") int sessionNum);

    List<LapDataMetaData> findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(@Param("raceEventId") String raceEventId, @Param("sessionNum") int sessionNum, @Param("data.carIdx") int carIdx);

    // used for Debugging
    List<LapDataMetaData> findByRaceEventIdAndSessionNumAndDataLapTimeGreaterThanOrderBySessionTimeAsc(@Param("raceEventId") String raceEventId, @Param("sessionNum") int sessionNum, float minLapTime);
}
