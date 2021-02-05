package de.mp.istint.server.repository.racelog;

import org.bson.codecs.pojo.annotations.BsonId;

import lombok.Data;

@Data
public class MyData {
    @BsonId

    int sessionNum;
    float minTime;
    float maxTime;
}
