package de.mp.istint.server.service.racelog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * used by clients on initial connect to submit race log data
 */

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewRecordingRequestDto {
    Long sessionId;
    Long trackId;
    String trackNameLong;
    String trackNameShort;
}
