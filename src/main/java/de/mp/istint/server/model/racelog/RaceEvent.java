package de.mp.istint.server.model.racelog;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @Id
    public UUID id;
    /**
     * 
     */
    public String ownerId;
    /**
     * this is the sessionId used by iRacing for non-local events
     */
    Long sessionId;
    Long trackId;
    String trackNameShort;
    String trackNameLong;

    public LocalDateTime lastModified;
}
