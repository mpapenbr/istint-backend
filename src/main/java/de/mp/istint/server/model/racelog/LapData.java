package de.mp.istint.server.model.racelog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LapData {
    int carIdx;
    int lapNo;
    float lapTime;
    float[] sectors;
    boolean inLap;
    boolean outLap;
    boolean incomplete;
}
