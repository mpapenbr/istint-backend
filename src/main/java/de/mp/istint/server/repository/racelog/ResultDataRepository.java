package de.mp.istint.server.repository.racelog;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import de.mp.istint.server.model.racelog.ResultMetaData;

public interface ResultDataRepository extends MongoRepository<ResultMetaData, UUID> {
    long deleteByRaceEventId(String raceEventId);

    List<ResultMetaData> findByRaceEventIdAndSessionNumAndDataCarIdxOrderBySessionTimeAsc(@Param("raceEventId") String raceEventId, @Param("sessionNum") int sessionNum, @Param("data.carIdx") int carIdx);

    List<ResultMetaData> findByRaceEventIdAndSessionNumAndDataCarIdxInOrderBySessionTimeAsc(@Param("raceEventId") String raceEventId, @Param("sessionNum") int sessionNum, @Param("data.carIdx") List<Integer> carIdx);
}
