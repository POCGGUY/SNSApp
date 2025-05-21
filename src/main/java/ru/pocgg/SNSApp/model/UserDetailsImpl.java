package ru.pocgg.SNSApp.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UserDetailsImpl implements UserDetails {
    private final int id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public boolean isAccountNonExpired(){
        return true;
    }

    public boolean isAccountNonLocked(){
        return true;
    }

    public boolean isCredentialsNonExpired(){
        return true;
    }

    public boolean isEnabled(){
        return true;
    }
}
