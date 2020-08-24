package de.mp.istint.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import de.mp.istint.server.security.UserPrincipal;

public class MyTestUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findOptionalByName(username)
                .map(item -> UserPrincipal.create(item))
                .orElseThrow(() -> new UsernameNotFoundException(String.format("user %s not found", username)));
    }

}