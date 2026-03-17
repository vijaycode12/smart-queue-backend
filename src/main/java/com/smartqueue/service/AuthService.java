package com.smartqueue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartqueue.model.User;
import com.smartqueue.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // Register user
    public User registerUser(User user) {
    	if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        return userRepository.save(user);
    }

    // Login user
    public User login(String email, String password) {

        User user = userRepository.findByEmail(email);

        if(user == null){
            return null;
        }

        if(user.getPassword() == null){
            return null;
        }

        if(user.getPassword().equals(password)){
            return user;
        }

        return null;
    }
}