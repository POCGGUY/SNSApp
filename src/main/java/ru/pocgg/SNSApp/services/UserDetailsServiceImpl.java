package ru.pocgg.SNSApp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.pocgg.SNSApp.model.UserDetailsImpl;

import java.util.List;

@Service
@Scope("singleton")
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    public UserDetails loadUserByUsername(String userName) {
        ru.pocgg.SNSApp.model.User user = userService.getUserByUserName(userName);
        return new UserDetailsImpl(
                user.getId(),
                user.getUserName(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getSystemRole().toString())));
    }

}