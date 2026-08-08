package com.cinemahub.cinemahub.security.service;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.security.entity.Role;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.entity.UserRole;
import com.cinemahub.cinemahub.security.entity.UserRoleId;
import com.cinemahub.cinemahub.security.entity.UserStatus;
import com.cinemahub.cinemahub.security.repository.RoleRepository;
import com.cinemahub.cinemahub.security.repository.UserRepository;
import com.cinemahub.cinemahub.security.repository.UserRoleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository,
                        RoleRepository roleRepository,
                        UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User no encontrado con email: " + email));
    }

    @Transactional
    public User register(String firstName, String lastName, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw DuplicateResourceException.of("un usuario", "email", email);
        }
        // TODO: reemplazar por hash real (BCrypt) cuando se agregue Spring Security.
        // Guardar la contraseña en texto plano es solo un placeholder para poder
        // avanzar con el resto de las capas; no usar así en ningún ambiente real.
        User user = new User(firstName, lastName, email, rawPassword);
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Long id, String firstName, String lastName, String phone) {
        User user = findById(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        return user;
    }

    @Transactional
    public User changeStatus(Long id, UserStatus status) {
        User user = findById(id);
        user.setStatus(status);
        return user;
    }

    @Transactional
    public UserRole assignRole(Long userId, Long roleId) {
        UserRoleId id = new UserRoleId(userId, roleId);
        return userRoleRepository.findById(id).orElseGet(() -> {
            User user = findById(userId);
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Role", roleId));
            return userRoleRepository.save(new UserRole(user, role));
        });
    }

    @Transactional
    public void removeRole(Long userId, Long roleId) {
        userRoleRepository.deleteById(new UserRoleId(userId, roleId));
    }

    public List<UserRole> findRoles(Long userId) {
        return userRoleRepository.findById_UserId(userId);
    }
}