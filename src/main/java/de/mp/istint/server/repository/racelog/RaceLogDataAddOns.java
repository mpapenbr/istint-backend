package de.mp.istint.server.repository.racelog;

import java.util.List;

import de.mp.istint.server.model.racelog.SessionSummary;

public interface RaceLogDataAddOns {
    List<SessionSummary> getSummaryBySession(String raceEventId);
}
