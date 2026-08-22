// Espejo de com.cinemahub.cinemahub.movie.dto.* (backend)

export type MovieStatus = 'ACTIVE' | 'INACTIVE' | 'COMING_SOON' | 'ARCHIVED';

export interface Classification {
  id: number;
  code: string;
  description: string | null;
}

export interface ClassificationRequest {
  code: string;
  description?: string;
}

export interface Genre {
  id: number;
  name: string;
}

export interface GenreRequest {
  name: string;
}

// Los géneros de la película NO viajan acá (relación many-to-many vía movie_genres);
// se consultan aparte con MovieService.findGenres -> GET /api/movies/{id}/genres.
export interface Movie {
  id: number;
  title: string;
  synopsis: string | null;
  duration: number;
  releaseDate: string | null;
  posterUrl: string | null;
  trailerUrl: string | null;
  classification: Classification;
  status: MovieStatus;
}

// Coincide con MovieService.create(title, duration, classificationId) del backend:
// synopsis/releaseDate/posterUrl/trailerUrl no se pueden setear en la creación todavía.
export interface CreateMovieRequest {
  title: string;
  duration: number;
  classificationId: number;
}

export interface ChangeMovieStatusRequest {
  status: MovieStatus;
}

export interface AddGenreRequest {
  genreId: number;
}

export interface UpdateMovieRequest {
  title: string;
  synopsis?: string;
  duration: number;
  releaseDate?: string;
  posterUrl?: string;
  trailerUrl?: string;
  classificationId: number;
}