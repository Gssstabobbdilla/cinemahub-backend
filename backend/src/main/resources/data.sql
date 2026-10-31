INSERT INTO roles(name, description)
VALUES
('ROLE_ADMIN', 'Administrador del sistema'),
('ROLE_USER', 'Usuario estándar');


INSERT INTO permissions(name, description)
VALUES
('MOVIE_CREATE', 'Crear películas'),
('MOVIE_UPDATE', 'Actualizar películas'),
('MOVIE_DELETE', 'Eliminar películas'),
('MOVIE_READ', 'Ver películas');

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN';