package de.mp.istint.server.repository.racelog.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import de.mp.istint.server.model.racelog.RaceLogMetaData;
import de.mp.istint.server.model.racelog.SessionSummary;
import de.mp.istint.server.repository.racelog.RaceLogDataAddOns;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RaceLogDataAddOnsImpl implements RaceLogDataAddOns {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<SessionSummary> getSummaryBySession(String raceEventId) {
       
        GroupOperation groupOp = Aggregation.group("sessionNum")
                .min("sessionTime").as("minTime")
                .max("sessionTime").as("maxTime")
                .min("sessionTick").as("minTick")
                .max("sessionTick").as("maxTick")
                .count().as("count");
        MatchOperation match = Aggregation.match(Criteria.where("raceEventId").is(raceEventId));
        Aggregation agg = Aggregation.newAggregation(match, groupOp);
        String mongoColl = mongoTemplate.getCollectionName(RaceLogMetaData.class);

        AggregationResults<SessionSummary> res = mongoTemplate.aggregate(agg, mongoColl, SessionSummary.class);
        return res.getMappedResults().stream().sorted(Comparator.comparing(SessionSummary::getSessionNum)).collect(Collectors.toList());
        
    }

}
