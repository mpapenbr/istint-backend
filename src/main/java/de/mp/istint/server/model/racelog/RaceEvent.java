package de.mp.istint.server.model.racelog;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class models a race event for racelog data.
 * 
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaceEvent {
    /**
     * holds the UUID
     */
    @Id
    public String id;
    /**
     * 
     */
    public String ownerId;
    public LocalDateTime lastModified;

    /**
     * this is the sessionId used by iRacing for non-local events
     */
    Long sessionId;
    Long trackId;
    String trackNameShort;
    String trackNameLong;
    String trackConfig;

    float trackLength;
    int teamRacing;
    int trackDynamicTrack;

    int numCarClasses;
    int numCarTypes;
    LocalDateTime eventStart;
    List<SessionInfo> sessions; // changing this to SessionInfo[] (as in Dto) would result in Json-Errors in Controller on delivery

}
