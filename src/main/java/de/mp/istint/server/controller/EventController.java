package de.mp.istint.server.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.mp.istint.server.model.Event;
import de.mp.istint.server.service.EventService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RepositoryRestController // Info: der "normale" RestController reicht nicht aus (mit dem geht nur das, was hier definiert ist)
//  @RestController
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EntityLinks entityLinks;

    @Primary
    @RequestMapping(method = RequestMethod.POST, path = "/events", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> ownSaveMethod(@RequestBody Event data, Principal principal, Authentication auth) {
        log.debug("ownSaveMethod begin");

        Event ret = eventService.save(data);
        return ResponseEntity
                .created(entityLinks.linkToItemResource(Event.class, ret.getId()).toUri())
                .build();
    }

    @Primary
    @RequestMapping(method = RequestMethod.PUT, path = "/events/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Event> ownPutMethod(@PathVariable UUID id, @RequestBody Event data, Principal principal, Authentication auth) {
        log.debug("ownPutMethod begin");
        Event ret = eventService.save(data);
        return ResponseEntity
                .ok(ret);

    }

    @Primary
    @RequestMapping(method = RequestMethod.GET, path = "/events/own")
    public ResponseEntity<CollectionModel<Event>> ownEvents(Principal principal, Authentication auth) {

        List<Event> ret = eventService.loadMyEvents();
        return ResponseEntity.ok(CollectionModel.of(ret));

    }

}