package de.mp.istint.server.model.racelog.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MinMaxAvg {
    int count;
    Double avg;
    Double min;
    Double max;
}