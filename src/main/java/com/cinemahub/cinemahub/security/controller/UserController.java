package com.cinemahub.cinemahub.security.controller;

import com.cinemahub.cinemahub.security.dto.AssignRoleRequest;
import com.cinemahub.cinemahub.security.dto.ChangeUserStatusRequest;
import com.cinemahub.cinemahub.security.dto.RegisterUserRequest;
import com.cinemahub.cinemahub.security.dto.RoleResponse;
import com.cinemahub.cinemahub.security.dto.UpdateProfileRequest;
import com.cinemahub.cinemahub.security.dto.UserResponse;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return UserResponse.from(userService.findById(id));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody RegisterUserRequest request) {
        User user = userService.register(
                request.firstName(), request.lastName(), request.email(), request.password());
        return UserResponse.from(user);
    }

    @PutMapping("/{id}/profile")
    public UserResponse updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest request) {
        User user = userService.updateProfile(id, request.firstName(), request.lastName(), request.phone());
        return UserResponse.from(user);
    }

    @PatchMapping("/{id}/status")
    public UserResponse changeStatus(@PathVariable Long id, @RequestBody ChangeUserStatusRequest request) {
        User user = userService.changeStatus(id, request.status());
        return UserResponse.from(user);
    }

    @GetMapping("/{id}/roles")
    public List<RoleResponse> findRoles(@PathVariable Long id) {
        return userService.findRoles(id).stream()
                .map(userRole -> RoleResponse.from(userRole.getRole()))
                .toList();
    }

    @PostMapping("/{id}/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public void assignRole(@PathVariable Long id, @RequestBody AssignRoleRequest request) {
        userService.assignRole(id, request.roleId());
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRole(@PathVariable Long id, @PathVariable Long roleId) {
        userService.removeRole(id, roleId);
    }
}