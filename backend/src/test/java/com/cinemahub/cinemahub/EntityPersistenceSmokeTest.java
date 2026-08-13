package com.cinemahub.cinemahub;

import com.cinemahub.cinemahub.movie.entity.Classification;
import com.cinemahub.cinemahub.movie.entity.Movie;
import com.cinemahub.cinemahub.movie.repository.ClassificationRepository;
import com.cinemahub.cinemahub.movie.repository.MovieRepository;
import com.cinemahub.cinemahub.security.entity.Role;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.entity.UserRole;
import com.cinemahub.cinemahub.security.entity.UserStatus;
import com.cinemahub.cinemahub.security.repository.RoleRepository;
import com.cinemahub.cinemahub.security.repository.UserRepository;
import com.cinemahub.cinemahub.security.repository.UserRoleRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de humo: persiste y lee una entidad simple (Role), una con enum (User/status),
 * una con @ManyToOne (Movie/Classification) y una de clave compuesta (UserRole).
 * Corre contra la base real configurada en application.properties, pero con rollback
 * automático al final de cada test — no deja datos de prueba.
 */
@SpringBootTest
@Transactional
@Rollback
class EntityPersistenceSmokeTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    void persistsSimpleEntity() {
        Role role = roleRepository.save(new Role("SMOKE_TEST_ROLE", "solo para pruebas"));
        assertThat(role.getId()).isNotNull();

        Role found = roleRepository.findById(role.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("SMOKE_TEST_ROLE");
    }

    @Test
    void persistsEntityWithEnumDefault() {
        User user = userRepository.save(
                new User("Smoke", "Test", "smoke.test@cinemahub.local", "hash"));

        User found = userRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void persistsEntityWithManyToOne() {
        Classification classification =
                classificationRepository.save(new Classification("SMK", "smoke test"));
        Movie movie = movieRepository.save(new Movie("Smoke Test Movie", 100, classification));

        Movie found = movieRepository.findById(movie.getId()).orElseThrow();
        assertThat(found.getClassification().getCode()).isEqualTo("SMK");
    }

    @Test
    void persistsCompositeKeyEntity() {
        Role role = roleRepository.save(new Role("SMOKE_ROLE_2", null));
        User user = userRepository.save(
                new User("Smoke2", "Test", "smoke.test2@cinemahub.local", "hash"));

        UserRole userRole = userRoleRepository.save(new UserRole(user, role));

        assertThat(userRoleRepository.findById(userRole.getId())).isPresent();
        assertThat(userRoleRepository.findById_UserId(user.getId())).hasSize(1);
    }
}