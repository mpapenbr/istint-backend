package de.mp.istint.server.model;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    private String id;
    private String name;

    private String password;

    private String email;

    private String oauth2Provider;

    private String providerId;

}