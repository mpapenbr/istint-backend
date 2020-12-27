package de.mp.istint.server.model.racelog;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The internal container for race data
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaceLogMetaData {
    @Id
    String id;

    @NonNull
    String raceEventId;

    float sessionTime;
    int sessionNum;
    int sessionTick;
    // @NonNull
    RaceLogData data;

}
