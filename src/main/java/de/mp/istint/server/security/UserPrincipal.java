package de.mp.istint.server.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import de.mp.istint.server.model.User;

public class UserPrincipal implements
        UserDetails {

    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;
    private User currentUser;

    public UserPrincipal(User user, Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
        this.currentUser = user;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserPrincipal(
                user,
                authorities);
    }

    @Override
    public String getPassword() {
        return "hasNoPassword";
    }

    @Override
    public String getUsername() {
        return currentUser.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public User getUser() {
        return currentUser;
    }
}
