package com.NotificationSystem.controller;

import com.NotificationSystem.entities.User;
import com.NotificationSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Optional<User> caller(Authentication auth) {
        return userService.getUserByEmail(auth.getName());
    }

    //  The frontend uses this because the JWT cookie is HttpOnly.
    @GetMapping("/me")
    public ResponseEntity<User> me(Authentication auth) {
        return caller(auth)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id, Authentication auth) {
        boolean self = caller(auth).map(c -> c.getId().equals(id)).orElse(false);
        if (!isAdmin(auth) && !self) return ResponseEntity.status(403).build();

        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(
            @PathVariable User.Role role, Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user, Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser,
            Authentication auth) {

        User me = caller(auth).orElse(null);
        if (me == null) return ResponseEntity.status(401).build();

        if (!isAdmin(auth)) {
            if (!me.getId().equals(id)) return ResponseEntity.status(403).build();
            updatedUser.setEmail(me.getEmail()); // staff cannot change their email
        }

        return userService.updateUser(id, updatedUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication auth) {
        User me = caller(auth).orElse(null);
        if (!isAdmin(auth) || me == null || me.getId().equals(id)) {
            return ResponseEntity.status(403).build(); // admins only, never yourself
        }
        if (userService.getUserById(id).isEmpty()) return ResponseEntity.notFound().build();

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}