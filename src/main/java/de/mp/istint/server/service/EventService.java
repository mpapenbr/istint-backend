package de.mp.istint.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.mp.istint.server.model.Event;
import de.mp.istint.server.model.User;
import de.mp.istint.server.repository.EventRepository;
import de.mp.istint.server.util.IAppUserUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private IAppUserUtil appUserUtil;

    public Event save(Event event) {
        User user = appUserUtil.getCurrentUser();
        log.debug("{}", user.getName());
        Optional<Event> inDb = eventRepository.findById(event.getId());
        if (inDb.isEmpty()) {
            event.setOwnerId(user.getId());
            event.setLastModified(LocalDateTime.now());
            return eventRepository.save(event);
        } else {
            if (inDb.get().getOwnerId().equals(user.getId())) {
                event.setOwnerId(user.getId());
                event.setLastModified(LocalDateTime.now());
                return eventRepository.save(event);
            }
            throw new AccessDeniedException("");
        }
        //.filter(item -> item.getOwnerId().equals(user.getId())).orElseThrow(() -> new ));

    }

    public List<Event> loadMyEvents() {
        User user = appUserUtil.getCurrentUser();
        return eventRepository.findByOwnerId(user.getId());
    }

    public void delete(String id) {
        User user = appUserUtil.getCurrentUser();
        Optional<Event> inDb = eventRepository.findById(id);
        if (inDb.isPresent()) {
            if (inDb.get().getOwnerId().equals(user.getId())) {
                eventRepository.deleteById(id);
            } else {
                throw new AccessDeniedException("");
            }
        }
    }

}