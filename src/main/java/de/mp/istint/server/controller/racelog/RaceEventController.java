package de.mp.istint.server.controller.racelog;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.mp.istint.server.model.racelog.RaceEvent;
import de.mp.istint.server.service.racelog.NewRecordingRequestDto;
import de.mp.istint.server.service.racelog.RaceEventService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RepositoryRestController() // Info: der "normale" RestController reicht nicht aus (mit dem geht nur das, was hier definiert ist)
public class RaceEventController {

    @Autowired
    private RaceEventService raceEventService;

    @Autowired
    private EntityLinks entityLinks;

    @Primary
    @RequestMapping(method = RequestMethod.POST, path = "/racelog/events", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UUID> requestRecording(@RequestBody NewRecordingRequestDto dto) {
        log.debug("ownSaveMethod begin");

        UUID ret = raceEventService.requestEventId(dto);
        return ResponseEntity
                .created(entityLinks.linkToItemResource(RaceEvent.class, ret).toUri())
                .build();
    }
}
