package de.mp.istint.server.service.racelog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.mp.istint.server.model.racelog.DriverMetaData;
import de.mp.istint.server.model.racelog.EventSummary;
import de.mp.istint.server.model.racelog.LapDataMetaData;
import de.mp.istint.server.model.racelog.PitStopMetaData;
import de.mp.istint.server.model.racelog.RaceDataContainer;
import de.mp.istint.server.model.racelog.RaceEvent;
import de.mp.istint.server.model.racelog.RaceLogMetaData;
import de.mp.istint.server.model.racelog.ResultMetaData;
import de.mp.istint.server.repository.racelog.DriverDataRepository;
import de.mp.istint.server.repository.racelog.LapDataRepository;
import de.mp.istint.server.repository.racelog.PitStopDataRepository;
import de.mp.istint.server.repository.racelog.RaceEventRepository;
import de.mp.istint.server.repository.racelog.RaceLogDataRepository;
import de.mp.istint.server.repository.racelog.ResultDataRepository;
import de.mp.istint.server.util.IAppUserUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@Service
public class RaceEventService {

    @Autowired
    private RaceEventRepository raceEventRepository;
    @Autowired
    private RaceLogDataRepository raceLogDataRepository;
    @Autowired
    private PitStopDataRepository pitStopDataRepository;
    @Autowired
    private DriverDataRepository driverDataRepository;
    @Autowired
    private ResultDataRepository resultDataRepository;
    @Autowired
    private LapDataRepository lapDataRepository;

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

    public String requestEventId(NewRecordingRequestDto dto) {
        Supplier<RaceEvent> newEvent = () -> RaceEvent.builder()
                .id(UUID.randomUUID().toString())
                .trackId(dto.getTrackId())
                .trackNameLong(dto.getTrackNameLong())
                .trackNameShort(dto.getTrackNameShort())
                .lastModified(LocalDateTime.now())
                .ownerId(getCurrentUserId())
                .build();

        if (Optional.ofNullable(dto.getSessionId()).isPresent()) {
            Optional<RaceEvent> event = raceEventRepository.findBySessionIdAndOwnerId(dto.getSessionId(), getCurrentUserId());
            return event.map(RaceEvent::getId).orElseGet(() -> {
                var toSave = newEvent.get();
                toSave.setSessionId(dto.getSessionId());
                raceEventRepository.save(toSave);
                return toSave.getId();
            });
        } else {

            var toSave = newEvent.get();
            raceEventRepository.save(toSave);
            return toSave.getId();
        }
    }

    public void delete(String id) {
        String userId = getCurrentUserId();
        Optional<RaceEvent> inDb = raceEventRepository.findById(id);
        if (inDb.isPresent()) {
            if (inDb.get().getOwnerId().equals(userId)) {
                raceEventRepository.deleteById(id);
                raceLogDataRepository.deleteByRaceEventId(id);
                pitStopDataRepository.deleteByRaceEventId(id);
                resultDataRepository.deleteByRaceEventId(id);
                driverDataRepository.deleteByRaceEventId(id);
                lapDataRepository.deleteByRaceEventId(id);
            } else {
                throw new AccessDeniedException("");
            }
        }
    }

    public void clearRaceData(String id) {
        String userId = getCurrentUserId();
        Optional<RaceEvent> inDb = raceEventRepository.findById(id);
        if (inDb.isPresent()) {
            if (inDb.get().getOwnerId().equals(userId)) {
                raceLogDataRepository.deleteByRaceEventId(id);
                pitStopDataRepository.deleteByRaceEventId(id);
                resultDataRepository.deleteByRaceEventId(id);
                driverDataRepository.deleteByRaceEventId(id);
                lapDataRepository.deleteByRaceEventId(id);
            } else {
                throw new AccessDeniedException("");
            }
        }
    }

    public List<DriverMetaData> getRaceDrivers(String raceEventId) {
        return driverDataRepository.findByRaceEventId(raceEventId);
    }

    public List<DriverMetaData> getDriversCondensed(String raceEventId) {
        List<DriverMetaData> data = driverDataRepository.findByRaceEventIdOrderBySessionNumAscSessionTimeAsc(raceEventId);
        // data.forEach(d -> log.debug("{} {} {} {} {}", d.getId(), d.getData().getCarIdx(), d.getSessionNum(), d.getSessionTime(), d.getData().getUserName()));

        // first: group by sessionNum, carIdx

        List<UUID> toRemoveEntries = new ArrayList<>();
        var bySessionNum = data.stream().collect(Collectors.groupingBy(item -> item.getSessionNum(), Collectors.toList()));
        bySessionNum.forEach((k, v) -> {
            var byCarIdx = v.stream().collect(Collectors.groupingBy(item -> item.getData().getCarIdx(), Collectors.toList()));
            // now we can collect the superflous entries
            byCarIdx.values().forEach(carDrivers -> {
                if (carDrivers.size() > 1) {
                    for (int i = 1; i < carDrivers.size(); i++) {
                        // if the userName of the current entry matches the predeccor, remove it.
                        var current = carDrivers.get(i);
                        var prev = carDrivers.get(i - 1);
                        if (prev.getData().getUserName().equals(current.getData().getUserName())) {
                            toRemoveEntries.add(current.getId());
                        }
                    }
                }
            });
        });
        log.debug("will remove {} entries from original {} entries", toRemoveEntries.size(), data.size());
        data.removeIf(d -> toRemoveEntries.contains(d.getId()));
        return data;
    }

    public EventSummary getSummary(String raceEventId) {
        return EventSummary.builder()
                .sessionSummaries(raceLogDataRepository.getSummaryBySession(raceEventId))
                .build();
    }

    public List<RaceLogMetaData> getEventDataAt(String raceEventId, int sessionNum, int sessionTime) {
        return raceLogDataRepository.findByRaceEventIdAndSessionNumAndSessionTimeBetweenOrderBySessionTimeAsc(raceEventId, sessionNum, (float) (sessionTime - 1), (float) (sessionTime + 0.017));
    }

    public void addData(String raceEventId, RaceDataContainer data) {
        var sessionTime = data.getRaceData().getSessionTime();
        var sessionTick = data.getRaceData().getSessionTick();
        var sessionNum = data.getRaceData().getSessionNum();
        RaceLogMetaData dbData = RaceLogMetaData.builder()
                .id(UUID.randomUUID().toString())
                .raceEventId(raceEventId)
                .data(data.getRaceData())
                .sessionTime(sessionTime)
                .sessionTick(sessionTick)
                .sessionNum(sessionNum)
                .build();
        raceLogDataRepository.save(dbData);
        Optional.ofNullable(data.getPitStops()).ifPresent(item -> pitStopDataRepository.saveAll(
                Arrays.stream(item)
                        .map(p -> PitStopMetaData.builder()
                                .id(UUID.randomUUID())
                                .raceEventId(raceEventId)
                                .sessionTime(sessionTime)
                                .sessionTick(sessionTick)
                                .sessionNum(sessionNum)
                                .data(p)
                                .build())
                        .collect(Collectors.toList())));

        Optional.ofNullable(data.getDriverData()).ifPresent(item -> driverDataRepository.saveAll(
                Arrays.stream(item)
                        .map(p -> DriverMetaData.builder()
                                .id(UUID.randomUUID())
                                .raceEventId(raceEventId)
                                .sessionTime(sessionTime)
                                .sessionTick(sessionTick)
                                .sessionNum(sessionNum)
                                .data(p)
                                .build())
                        .collect(Collectors.toList())));

        Optional.ofNullable(data.getResultData()).ifPresent(item -> resultDataRepository.saveAll(
                Arrays.stream(item)
                        .map(p -> ResultMetaData.builder()
                                .id(UUID.randomUUID())
                                .raceEventId(raceEventId)
                                .sessionTime(sessionTime)
                                .sessionTick(sessionTick)
                                .sessionNum(sessionNum)
                                .data(p)
                                .build())
                        .collect(Collectors.toList())));

        Optional.ofNullable(data.getOwnLaps()).ifPresent(item -> lapDataRepository.saveAll(
                Arrays.stream(item)
                        .map(p -> LapDataMetaData.builder()
                                .id(UUID.randomUUID())
                                .raceEventId(raceEventId)
                                .sessionTime(sessionTime)
                                .sessionTick(sessionTick)
                                .sessionNum(sessionNum)
                                .data(p)
                                .build())
                        .collect(Collectors.toList())));

    }

    private String getCurrentUserId() {
        // TODO: remove for prodution. workaround until user management for racelog is defined.
        // log.debug("{}", appUserUtil);
        return Optional.ofNullable(appUserUtil.getCurrentUser()).map(u -> u.getId()).orElse("testUser");
    }
}
