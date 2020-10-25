package de.mp.istint.server.service;

import java.time.LocalDateTime;

import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.mp.istint.server.model.Event;
import de.mp.istint.server.repository.EventRepository;
import de.mp.istint.server.util.AppUserUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private AppUserUtil appUserUtil;

    public Event save(Event event) {
        AccessToken userToken = appUserUtil.getCurrentUser();
        log.debug("{}", userToken.getName());
        event.setOwnerId(userToken.getSubject());
        event.setLastModified(LocalDateTime.now());
        return eventRepository.save(event);
    }
}