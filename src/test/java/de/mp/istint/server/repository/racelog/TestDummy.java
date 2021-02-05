package de.mp.istint.server.repository.racelog;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.mp.istint.server.model.racelog.DriverData;
import de.mp.istint.server.model.racelog.RaceLogMetaData;
import de.mp.istint.server.model.racelog.SessionSummary;

@DataMongoTest
@ExtendWith(SpringExtension.class)

public class TestDummy {

    @Autowired
    private RaceLogDataRepository raceLogDataRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void pipelineTest() {
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(12.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(24.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(36.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(1).sessionTime(110.0f).build());

        List<Bson> pipeline = Arrays.asList(Aggregates.group("$sessionNum",
                Accumulators.min("minTime", "$sessionTime"), Accumulators.max("maxTime", "$sessionTime")));
        // Aggregates.project(Projections.excludeId()));

        // List<Bson> pipeline = Arrays.asList(Aggregates.group(new BsonNull(), Accumulators.min("minTime", "$sessionTime")), Aggregates.project(Projections.excludeId()));
        System.out.println(pipeline);

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true)
                // .register(MyData.class)
                .build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        MongoCollection<Document> mColl = mongoTemplate.getCollection(mongoTemplate.getCollectionName(RaceLogMetaData.class));
        mColl
                // .withCodecRegistry(codecRegistry)
                .aggregate(pipeline, Document.class).forEach(System.out::println);

        mColl
                .withCodecRegistry(codecRegistry)
                .aggregate(pipeline, MyData.class).forEach(System.out::println);

        // System.out.println("TestRaceLogDataRepository.pipelineTest() " + res);
    }

    @Test
    public void pipelineTestSprint() {
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(12.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(24.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(0).sessionTime(36.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(1).sessionTime(55.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(1).sessionTime(66.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("1").sessionNum(1).sessionTime(77.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("2").sessionNum(1).sessionTime(110.0f).build());
        raceLogDataRepository.save(RaceLogMetaData.builder().id(UUID.randomUUID().toString()).raceEventId("2").sessionNum(1).sessionTime(120.0f).build());

        GroupOperation groupOp = Aggregation.group("sessionNum")
                .min("sessionTime").as("minTime")
                .max("sessionTime").as("maxTime")
                .min("sessionTick").as("minTick")
                .max("sessionTick").as("maxTick")
                .count().as("count");
        MatchOperation match = Aggregation.match(Criteria.where("raceEventId").is("1"));
        Aggregation agg = Aggregation.newAggregation(match, groupOp);
        String mongoColl = mongoTemplate.getCollectionName(RaceLogMetaData.class);

        // .withCodecRegistry(codecRegistry)
        AggregationResults<Document> res = mongoTemplate.aggregate(agg, mongoColl, Document.class);
        res.forEach(System.out::println);

        AggregationResults<SessionSummary> res2 = mongoTemplate.aggregate(agg, mongoColl, SessionSummary.class);
        res2.forEach(System.out::println);

        // System.out.println("TestRaceLogDataRepository.pipelineTest() " + res);
    }

    @Test
    public void testJson() throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String data = "{\"iRating\": 12, \"carIdx\": 333 }";
        DriverData dd = om.readValue(data, DriverData.class);
        System.out.println("TestDummy.testJson()" + dd);

    }

}
