package com.tadeo.fish_project.service;

import com.tadeo.fish_project.dto.UserPasswordDto;
import com.tadeo.fish_project.entity.User;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public User createUser(String username, String password) throws Exception {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and password should not be null");
        }
        if (userExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User(username,
                passwordEncoder.encode(password),
                AuthorityUtils.createAuthorityList("user"));
        return userRepository.save(user);
    }

    public String authenticateAndGetToken(String username, String password) throws Exception {
        UserDetails userDetails = loadUserByUsername(username);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new AuthenticationServiceException("Password doesn't match");
        }
        return jwtUtil.generateToken(username);
    }

    public void changePassword(UserPasswordDto userPasswordDto) throws Exception {
        User user = reauthenticate(userPasswordDto.oldPassword());
        user.setPassword(passwordEncoder.encode(userPasswordDto.newPassword()));
        userRepository.save(user);
    }

    public void delete(String password) throws Exception {
        User user = reauthenticate(password);
        userRepository.delete(user);
    }

    /*
    returns already authenticated user if password matches
    */
    private User reauthenticate(String password) throws Exception {
        User user = getUser();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Password doesn't match authenticated user: " + user.getUsername());
        }
        return user;
    }

    public Optional<UserDetails> getUserDetails() {
        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            return Optional.of(loadUserByUsername(userName));
        }
        return Optional.empty();
    }

    public User getUser() throws AuthenticationServiceException {
        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException(userName));
        }
        else throw new AuthenticationServiceException("Not authenticated");
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

}
