package de.mp.istint.server.model.racelog.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StintData {
    int stintNo;
    MinMaxAvg all;
    MinMaxAvg ranged;

    List<LapDataExtended> laps;

}
