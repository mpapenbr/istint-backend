package de.mp.istint.server.model.racelog;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

public class LapDataMetaData {
    @Id
    UUID id;
    @NonNull
    String raceEventId;

    float sessionTime;
    int sessionNum;
    int sessionTick;
    // @NonNull
    LapData data;
}
