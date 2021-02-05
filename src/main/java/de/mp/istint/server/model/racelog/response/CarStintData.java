package de.mp.istint.server.model.racelog.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarStintData {
    int carIdx;
    List<StintData> stints;
}
