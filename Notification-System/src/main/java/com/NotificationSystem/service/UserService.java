package com.NotificationSystem.service;

import com.NotificationSystem.entities.User;
import com.NotificationSystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // CHANGED: new import
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // CHANGED: new field, injects the bean from PasswordConfig

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // CHANGED: this whole method is new — replaces the old saveUser() for creation.
    // Hashes the incoming plaintext password before it ever touches the database.
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // CHANGED: this whole method is new — replaces the old saveUser() for updates.
    // Fetches the existing row, updates the editable fields, and only re-hashes
    // the password if a new one was actually sent (so editing name/role doesn't
    // wipe out the user's password).
    public Optional<User> updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(existing -> {
            existing.setName(updatedUser.getName());
            existing.setEmail(updatedUser.getEmail());
            existing.setRole(updatedUser.getRole());

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            return userRepository.save(existing);
        });
    }

    // REMOVED: the old `public User saveUser(User user) { return userRepository.save(user); }`
    // is gone — it used to save passwords as plain text with no hashing at all.

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }
}