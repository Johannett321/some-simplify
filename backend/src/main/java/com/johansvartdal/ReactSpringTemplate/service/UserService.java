package com.johansvartdal.ReactSpringTemplate.service;

import com.johansvartdal.ReactSpringTemplate.model.AuthenticationToken;
import com.johansvartdal.ReactSpringTemplate.repository.UserRepository;
import com.johansvartdal.ReactSpringTemplate.model.User;
import com.johansvartdal.ReactSpringTemplate.dto.UserDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByClerkID(String clerkID) {
        return userRepository.findUserByClerkID(clerkID);
    }

    /**
     * Adds the newly created user to the database
     * @param userDTO
     */
    public void addUser(UserDTO userDTO) {
        User user = new User();
        user.setClerkID(userDTO.getUserClerkID());
        userRepository.save(user);
    }

    /**
     * Returns the user that is currently logged in
     * @return The user that is currently logged in
     */
    public User getCurrentUser() {
        AuthenticationToken token = (AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String clerkUserID = token.getPrincipal();
        return getUserByClerkID(clerkUserID);
    }

    /**
     * Creates the user if it does not exist. It is based on the currently logged in user.
     */
    public void createUserIfItDoesNotExist() {
        AuthenticationToken token = (AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String clerkUserID = token.getPrincipal();

        if (!userRepository.existsByClerkID(clerkUserID)) {
            // user does not exist, let's create it
            UserDTO userDTO = new UserDTO(clerkUserID);
            addUser(userDTO);
            System.out.println("A user was created with the clerkID: " + clerkUserID);
        }else {
            System.out.println("The user with the following clerkID already exists, skipping creation...: " + clerkUserID);
        }
    }
}
