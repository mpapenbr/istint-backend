package de.mp.istint.server.model.racelog;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the race data at certain timestamps
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaceLogData {
    @Id
    UUID id;

    @NonNull
    UUID raceEventId;

    int[] carIdxPosition;
    int[] carIdxClassPosition;
    float[] carIdxLapDistPct;
    int[] carIdxLap;
    int[] carIdxLapCompleted;
    boolean[] carIdxOnPitRoad;

    long sessionTime;
    long sessionTimeRemain;
    long sessionTimeOfDay;

}
