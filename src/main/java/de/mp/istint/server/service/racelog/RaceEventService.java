package de.mp.istint.server.service.racelog;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.mp.istint.server.model.racelog.RaceEvent;
import de.mp.istint.server.repository.racelog.RaceEventRepository;
import de.mp.istint.server.util.IAppUserUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@Service
public class RaceEventService {

    private RaceEventRepository raceEventRepository;
    @Autowired
    private IAppUserUtil appUserUtil;

    /**
     * if a RaceEvent with the given sessionId is registered for the current user that event id is
     * returned, otherwise a new RaceEvent is created.
     * if the sessionId is null (e.g. it is a local test session) a new race event is created.
     * 
     * @param dto
     * @return UUID of our RaceEvent
     */

    public UUID requestEventId(NewRecordingRequestDto dto) {
        Supplier<RaceEvent> newEvent = () -> raceEventRepository
                .save(RaceEvent.builder()
                        .id(UUID.randomUUID())
                        .trackId(dto.getTrackId())
                        .trackNameLong(dto.getTrackNameLong())
                        .trackNameShort(dto.getTrackNameShort())
                        .lastModified(LocalDateTime.now())
                        .ownerId(getCurrentUserId())
                        .build());

        if (Optional.ofNullable(dto.getSessionId()).isPresent()) {
            Optional<RaceEvent> event = raceEventRepository.findBySessionIdAndOwnerId(dto.getSessionId(), getCurrentUserId());
            return event.map(RaceEvent::getId).orElseGet(() -> newEvent.get().getId());
        } else {
            return newEvent.get().getId();
        }
    }

    private String getCurrentUserId() {
        // TODO: remove for prodution. workaround until user management for racelog is defined.
        return Optional.of(appUserUtil.getCurrentUser().getId()).orElse("testUser");
    }
}
