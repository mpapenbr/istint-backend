package de.mp.istint.server.service.racelog;

import java.time.LocalDateTime;

import de.mp.istint.server.model.racelog.SessionInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaceEventDataDto {

    Long sessionId;
    Long trackId;
    String trackNameShort;
    String trackNameLong;
    String trackConfig;

    Float trackLength;
    Integer teamRacing;
    Integer trackDynamicTrack;

    Integer numCarClasses;
    Integer numCarTypes;
    LocalDateTime eventStart;
    SessionInfo sessions[];
}
