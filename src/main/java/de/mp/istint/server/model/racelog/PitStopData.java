package de.mp.istint.server.model.racelog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

public class PitStopData {
    int carIdx;
    float pitLaneTime; // how long took was the car in the pit lane
    float pitStopTime; // how long took the pit stop

    float laneEnterTime; // sessionTime when the car entered the pits
    float stopEnterTime; // sessionTime when the car stopped at its box
}
