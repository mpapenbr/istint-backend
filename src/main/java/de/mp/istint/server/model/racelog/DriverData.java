package de.mp.istint.server.model.racelog;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverData {
    int carIdx;
    int carId;
    int carClassId;
    String carClassShortName;
    int carNumber;
    String carNumberRaw;
    String carShortName;
    String carName;

    @JsonProperty("iRating")
    int iRating;
    boolean spectator;
    int userId;
    int teamId;
    String teamName;
    String userName;
}
