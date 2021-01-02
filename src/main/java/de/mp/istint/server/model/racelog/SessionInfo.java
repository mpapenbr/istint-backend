package de.mp.istint.server.model.racelog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionInfo {
    int num;
    String name;
    String type;
}
