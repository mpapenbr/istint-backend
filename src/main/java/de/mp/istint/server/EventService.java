package de.mp.istint.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.mp.istint.server.model.Event;
import de.mp.istint.server.security.UserPrincipal;
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
        UserPrincipal u = appUserUtil.getCurrentUser().get();
        log.debug("{}", u.getUser());
        event.setOwner(u.getUser());
        return eventRepository.save(event);
    }
}