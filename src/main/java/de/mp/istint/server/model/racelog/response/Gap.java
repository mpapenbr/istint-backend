package de.mp.istint.server.model.racelog.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Gap {
    int lapNo;
    float delta;
    CarInfo ref;
    CarInfo other;

    @Data
    @Builder
    public static class CarInfo {
        int carIdx;
        int position;
        int classPosition;
        float rawDelta;
    }
}
