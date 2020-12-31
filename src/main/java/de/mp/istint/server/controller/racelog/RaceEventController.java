package de.mp.istint.server.controller.racelog;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.mp.istint.server.model.racelog.DriverMetaData;
import de.mp.istint.server.model.racelog.EventSummary;
import de.mp.istint.server.model.racelog.RaceDataContainer;
import de.mp.istint.server.model.racelog.RaceEvent;
import de.mp.istint.server.model.racelog.RaceLogMetaData;
import de.mp.istint.server.service.racelog.NewRecordingRequestDto;
import de.mp.istint.server.service.racelog.RaceEventService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RepositoryRestController // Info: der "normale" RestController reicht nicht aus (mit dem geht nur das, was hier definiert ist)

/**
 * if we would add @RestController here the generated endpoints by spring data would not be visible
 * anymore.
 */

public class RaceEventController {

    @Autowired
    private RaceEventService raceEventService;

    @Autowired
    private EntityLinks entityLinks;

    @Primary
    @RequestMapping(method = RequestMethod.POST, path = "/raceevents/request", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> requestRecording(@RequestBody NewRecordingRequestDto dto) {
        log.debug("request event id");

        String ret = raceEventService.requestEventId(dto);
        return ResponseEntity
                .created(entityLinks.linkToItemResource(RaceEvent.class, ret).toUri())
                .build();
    }

    @Primary
    @RequestMapping(method = RequestMethod.DELETE, path = "/raceevents/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        log.debug("ownDelete begin");
        raceEventService.delete(id.toString());
        return ResponseEntity
                .noContent()
                .build();
    }

    @Primary
    @RequestMapping(method = RequestMethod.GET, path = "/raceevents/{id}/drivers")
    public ResponseEntity<CollectionModel<DriverMetaData>> eventDrivers(@PathVariable UUID id) {
        log.debug("get race event drivers");
        List<DriverMetaData> data = raceEventService.getDriversCondensed(id.toString());
        return ResponseEntity
                .ok(CollectionModel.of(data));
    }

    @Primary
    @RequestMapping(method = RequestMethod.GET, path = "/raceevents/{id}/{sessionNum}/{sessionTime}")
    public ResponseEntity<CollectionModel<RaceLogMetaData>> dataAtTime(@PathVariable UUID id, @PathVariable int sessionNum, @PathVariable int sessionTime) {
        log.debug("get race data for event {} in session {} at {}", id.toString(), sessionNum, sessionTime);
        List<RaceLogMetaData> data = raceEventService.getEventDataAt(id.toString(), sessionNum, sessionTime);
        return ResponseEntity
                .ok(CollectionModel.of(data));
    }

    @Primary
    @RequestMapping(method = RequestMethod.GET, path = "/raceevents/{id}/summary")
    public ResponseEntity<EventSummary> summmary(@PathVariable UUID id) {
        EventSummary summary = raceEventService.getSummary(id.toString());
        //EventSummary summary = EventSummary.builder().sessionSummaries(List.of(SessionSummary.builder().sessionNum(1).build())).build();
        return ResponseEntity
                .ok(summary);
    }

    @Primary
    @RequestMapping(method = RequestMethod.DELETE, path = "/raceevents/{id}/racedata")
    public ResponseEntity<Void> deleteRaceData(@PathVariable UUID id) {
        log.debug("deleteRaceData begin");
        raceEventService.clearRaceData(id.toString());
        return ResponseEntity
                .noContent()
                .build();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/raceevents/{id}/racedata", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addLogData(@PathVariable UUID id, @RequestBody RaceDataContainer data) {

        raceEventService.addData(id.toString(), data);
        return ResponseEntity
                .ok()
                .build();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/raceevents/{id}/dummy", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> dummy(@PathVariable UUID id, @RequestBody RaceDataContainer data) {

        return ResponseEntity
                .ok()
                .build();
    }
}
