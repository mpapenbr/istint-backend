package de.mp.istint.server.model.racelog.response;

import de.mp.istint.server.model.racelog.LapData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LapDataExtended {
    float sessionTime;
    LapData lapData;
    boolean filtered;
    float rollAvg;
    float rollAvgFiltered;

}
