package de.mp.istint.server.model.racelog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ResultData {
    int carIdx;
    int classPosition;
    int lap;
    int lapsComplete;
    int lapsDriven;
    int position;
    float delta;
    String reasonOut;
}
