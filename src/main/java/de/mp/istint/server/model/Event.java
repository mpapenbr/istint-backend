package de.mp.istint.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @DBRef()
    private User owner;

}