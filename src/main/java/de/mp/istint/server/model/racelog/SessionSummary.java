package de.mp.istint.server.model.racelog;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class SessionSummary {
    @Id
    int sessionNum;
    float minTime;
    float maxTime;
    int minTick;
    int maxTick;
    int count;
}
