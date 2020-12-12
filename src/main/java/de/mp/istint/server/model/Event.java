package de.mp.istint.server.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class models an Event for computing Stints.
 * TODO: consider renaming this and put it into a separate collection.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    public String id;

    public String name;
    public String carName;
    public String trackName;
    public String raceData;
    public Object rawData;
    public String ownerId;
    public LocalDateTime lastModified;
    @DBRef()
    private User owner;

}