package de.mp.istint.server.model.racelog;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class EventSummary {
    List<SessionSummary> sessionSummaries;
}
