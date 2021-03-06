package de.mp.istint.server.model.racelog;

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

    int[] carIdxPosition;
    int[] carIdxClassPosition;
    float[] carIdxLapDistPct;
    float[] carIdxLastLapTime;
    int[] carIdxLap;
    int[] carIdxLapCompleted;
    float[][] carIdxLapSectors;
    boolean[] carIdxOnPitRoad;
    float[] carIdxSpeed;
    float[] carIdxDelta;
    float[] carIdxDistMeters;

    int sessionTick;
    float sessionTime;
    float sessionTimeRemain;
    long sessionTimeOfDay;
    long sessionFlags;
    int sessionNum;
    int sessionState;

    float airDensity;
    float airPressure;
    float airTemp;
    float trackTemp;
    float trackTempCrew;

}
