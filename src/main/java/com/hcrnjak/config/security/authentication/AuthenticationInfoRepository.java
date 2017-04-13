package com.hcrnjak.config.security.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hcrnjak.model.User;
import com.hcrnjak.repositories.UserRepository;

@Service
public class AuthenticationInfoRepository implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public AuthenticatedUser loadUserByUsername(String username) throws UsernameNotFoundException {
        // Get User from Repository
        User user = userRepository.findByUsername(username);

        if (user != null) {
            // Convert User to Spring Security compatible format
            return  AuthenticatedUser.from(user);
        } else {
            // User not found, throw exception
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        }
    }
}
