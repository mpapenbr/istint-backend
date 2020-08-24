package de.mp.istint.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import de.mp.istint.server.UserRepository;
import de.mp.istint.server.exception.ResourceNotFoundException;
import de.mp.istint.server.model.User;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository appUserService;

    @Override
    
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        User user = appUserService.findOptionalByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + email));

        return UserPrincipal.create(user);
    }

    
    public UserDetails loadUserById(Long id) {
        throw new ResourceNotFoundException("User", "id", id);
        // User user = appUserService.findById(id).orElseThrow(
        //         () -> new ResourceNotFoundException("User", "id", id));

        // return UserPrincipal.create(user);
    }
}