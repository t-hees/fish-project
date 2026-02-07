package com.tadeo.fish_project.service;

import com.tadeo.fish_project.dto.UserPasswordDto;
import com.tadeo.fish_project.entity.User;
import com.tadeo.fish_project.exception.UserCredentialsException;
import com.tadeo.fish_project.repository.UserRepository;
import com.tadeo.fish_project.util.JwtUtil;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserCredentialsException("Failed to find user: " + username));
    }

    public User createUser(String username, String password) {
        if (username == null || password == null) {
            throw new UserCredentialsException("Username and password should not be null");
        }
        if (userExists(username)) {
            throw new UserCredentialsException("Username already exists");
        }
        User user = new User(username,
                passwordEncoder.encode(password),
                AuthorityUtils.createAuthorityList("user"));
        return userRepository.save(user);
    }

    public String login(String username, String password) {
        UserDetails userDetails = loadUserByUsername(username);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new UserCredentialsException("Password doesn't match user: " + username);
        }
        return jwtUtil.generateToken(username);
    }

    public void changePassword(UserPasswordDto userPasswordDto) {
        User user = reauthenticate(userPasswordDto.oldPassword());
        user.setPassword(passwordEncoder.encode(userPasswordDto.newPassword()));
        userRepository.save(user);
    }

    public void delete(String password) {
        User user = reauthenticate(password);
        userRepository.delete(user);
    }

    /*
    returns already authenticated user if password matches
    */
    private User reauthenticate(String password) {
        User user = getUser();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserCredentialsException("Password doesn't match authenticated user: " + user.getUsername());
        }
        return user;
    }

    public Optional<String> getUsername() {
        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            return Optional.of(userName);
        }
        return Optional.empty();
    }

    public User getUser() {
        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByUsername(userName)
                .orElseThrow(() -> new UserCredentialsException("Failed to find user: " + userName));
        }
        else throw new UserCredentialsException("Not authenticated");
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

}
