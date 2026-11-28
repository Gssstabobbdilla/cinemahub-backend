-- Datos iniciales para el entorno de demostración de CinemaHub.
-- Se puede ejecutar más de una vez sin duplicar roles, permisos ni películas.

INSERT INTO roles (name, description)
VALUES
    ('ROLE_ADMIN', 'Administrador del sistema'),
    ('ROLE_USER', 'Usuario estándar')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO permissions (name, description)
VALUES
    ('MOVIE_CREATE', 'Crear películas'),
    ('MOVIE_UPDATE', 'Actualizar películas'),
    ('MOVIE_DELETE', 'Eliminar películas'),
    ('MOVIE_READ', 'Ver películas')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('MOVIE_CREATE', 'MOVIE_UPDATE', 'MOVIE_DELETE', 'MOVIE_READ')
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO classifications (code, description)
VALUES
    ('G', 'Apto para todo público'),
    ('PG', 'Se recomienda orientación de un adulto'),
    ('PG-13', 'Contenido no recomendado para menores de 13 años'),
    ('R', 'Contenido para público adulto')
ON CONFLICT (code) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO genres (name)
VALUES
    ('Acción'),
    ('Aventura'),
    ('Animación'),
    ('Ciencia ficción'),
    ('Comedia'),
    ('Drama'),
    ('Familia')
ON CONFLICT (name) DO NOTHING;

WITH movie_seed (title, synopsis, duration, release_date, classification_code, poster_url) AS (
    VALUES
        ('Toy Story',
         'Un muñeco vaquero debe adaptarse cuando llega un nuevo juguete a la habitación.',
         81, DATE '1995-11-22', 'G', 'https://image.tmdb.org/t/p/w500/uXDfjJbdP4ijW5hWSBrPrlKpxab.jpg'),
        ('Jurassic Park',
         'Un parque temático con dinosaurios clonados se convierte en una lucha por sobrevivir.',
         127, DATE '1993-06-11', 'PG-13', 'https://image.tmdb.org/t/p/w500/fjTU1Bgh3KJu4aatZil3sofR2zC.jpg'),
        ('The Matrix',
         'Un programador descubre que la realidad que conoce es una simulación.',
         136, DATE '1999-03-31', 'R', 'https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg'),
        ('Inception',
         'Un equipo especializado intenta implantar una idea dentro del sueño de un objetivo.',
         148, DATE '2010-07-16', 'PG-13', 'https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg'),
        ('Interstellar',
         'Un grupo de exploradores busca un nuevo hogar para la humanidad más allá de la Tierra.',
         169, DATE '2014-11-07', 'PG-13', 'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg'),
        ('Coco',
         'Un niño músico emprende un viaje para descubrir la historia de su familia.',
         105, DATE '2017-11-22', 'PG', 'https://image.tmdb.org/t/p/w500/ywkEW12j0wRy4sOBar15QEVwFMN.jpg'),
        ('Avatar',
         'Un exmarine llega a Pandora y debe decidir entre su misión y proteger a sus habitantes.',
         162, DATE '2009-12-18', 'PG-13', 'https://image.tmdb.org/t/p/w500/kyeqWdyUXW608qlYkRqosgbbJyK.jpg'),
        ('Avengers: Endgame',
         'Los Vengadores restantes intentan revertir las consecuencias de una catástrofe universal.',
         181, DATE '2019-04-26', 'PG-13', 'https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg'),
        ('Finding Nemo',
         'Un pez payaso cruza el océano para encontrar a su hijo, acompañado por una olvidadiza amiga.',
         100, DATE '2003-05-30', 'G', 'https://image.tmdb.org/t/p/w500/eHuGQ10FUzK1mdOY69wF5pGgEf5.jpg'),
        ('Shrek',
         'Un ogro emprende una aventura para recuperar la tranquilidad de su pantano.',
         90, DATE '2001-05-18', 'PG', 'https://image.tmdb.org/t/p/w500/iB64vpL3dIObA4wLz7ddtpyXW2.jpg'),
        ('The Dark Knight',
         'Batman se enfrenta a un criminal que lleva a Ciudad Gótica al caos.',
         152, DATE '2008-07-18', 'PG-13', 'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg'),
        ('Spider-Man: Into the Spider-Verse',
         'Miles Morales descubre sus poderes y conoce a otros Spider-Man de distintas dimensiones.',
         117, DATE '2018-12-14', 'PG', 'https://image.tmdb.org/t/p/w500/iiZZdoQBEYBv6id8su7ImL0oCbD.jpg')
), updated_movies AS (
    UPDATE movies existing
    SET poster_url = seed.poster_url
    FROM movie_seed seed
    WHERE existing.title = seed.title
      AND existing.poster_url IS DISTINCT FROM seed.poster_url
    RETURNING existing.id
), inserted_movies AS (
    INSERT INTO movies (title, synopsis, duration, release_date, poster_url, classification_id, status)
    SELECT seed.title,
           seed.synopsis,
           seed.duration,
           seed.release_date,
           seed.poster_url,
           classification.id,
           'ACTIVE'
    FROM movie_seed seed
    JOIN classifications classification ON classification.code = seed.classification_code
    WHERE NOT EXISTS (
        SELECT 1
        FROM movies existing
        WHERE existing.title = seed.title
    )
    RETURNING id, title
)
SELECT (SELECT count(*) FROM inserted_movies) AS inserted_demo_movies,
       (SELECT count(*) FROM updated_movies) AS updated_demo_movie_posters;

WITH movie_genre_seed (movie_title, genre_name) AS (
    VALUES
        ('Toy Story', 'Animación'),
        ('Toy Story', 'Comedia'),
        ('Toy Story', 'Familia'),
        ('Jurassic Park', 'Aventura'),
        ('Jurassic Park', 'Ciencia ficción'),
        ('The Matrix', 'Acción'),
        ('The Matrix', 'Ciencia ficción'),
        ('Inception', 'Acción'),
        ('Inception', 'Ciencia ficción'),
        ('Inception', 'Drama'),
        ('Interstellar', 'Aventura'),
        ('Interstellar', 'Ciencia ficción'),
        ('Interstellar', 'Drama'),
        ('Coco', 'Animación'),
        ('Coco', 'Aventura'),
        ('Coco', 'Familia'),
        ('Avatar', 'Acción'),
        ('Avatar', 'Aventura'),
        ('Avatar', 'Ciencia ficción'),
        ('Avengers: Endgame', 'Acción'),
        ('Avengers: Endgame', 'Aventura'),
        ('Avengers: Endgame', 'Ciencia ficción'),
        ('Finding Nemo', 'Animación'),
        ('Finding Nemo', 'Aventura'),
        ('Finding Nemo', 'Familia'),
        ('Shrek', 'Animación'),
        ('Shrek', 'Comedia'),
        ('Shrek', 'Familia'),
        ('The Dark Knight', 'Acción'),
        ('The Dark Knight', 'Drama'),
        ('Spider-Man: Into the Spider-Verse', 'Acción'),
        ('Spider-Man: Into the Spider-Verse', 'Animación'),
        ('Spider-Man: Into the Spider-Verse', 'Aventura')
)
INSERT INTO movie_genres (movie_id, genre_id)
SELECT movie.id, genre.id
FROM movie_genre_seed seed
JOIN movies movie ON movie.title = seed.movie_title
JOIN genres genre ON genre.name = seed.genre_name
ON CONFLICT (movie_id, genre_id) DO NOTHING;
