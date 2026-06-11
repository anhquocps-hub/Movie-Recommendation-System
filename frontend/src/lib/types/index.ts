// --- Common wrappers ---

export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// --- Auth ---

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: number;
    email: string;
    username: string;
    role: string;
  };
}

// --- User ---

export interface UserProfileResponse {
  id: number;
  email: string;
  username: string;
  role: string;
  avatarUrl: string | null;
  preferences: string[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateProfileRequest {
  username?: string;
  avatarUrl?: string;
}

export interface UpdatePreferencesRequest {
  genreNames: string[];
}

// --- Movie ---

export interface MovieResponse {
  id: number;
  title: string;
  slug: string;
  posterUrl: string | null;
  releaseDate: string;
  avgRating: number | null;
  voteCount: number;
  genres: string[];
}

export interface MovieDetailResponse {
  id: number;
  title: string;
  slug: string;
  overview: string;
  posterUrl: string | null;
  backdropUrl: string | null;
  releaseDate: string;
  runtimeMinutes: number;
  avgRating: number | null;
  voteCount: number;
  isActive: boolean;
  genres: GenreResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateMovieRequest {
  title: string;
  overview: string;
  posterUrl?: string;
  backdropUrl?: string;
  releaseDate: string;
  runtimeMinutes: number;
  genreIds: number[];
}

export interface UpdateMovieRequest extends Partial<CreateMovieRequest> {}

// --- Genre ---

export interface GenreResponse {
  id: number;
  name: string;
  slug: string;
}

export interface CreateGenreRequest {
  name: string;
}

export interface UpdateGenreRequest {
  name: string;
}

// --- Review ---

export interface ReviewResponse {
  id: number;
  userId: number;
  username: string;
  movieId: number;
  movieTitle: string;
  rating: number;
  content: string;
  isSpoiler: boolean;
  likeCount: number;
  replyCount: number;
  likedByCurrentUser: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReviewRequest {
  rating: number;
  content: string;
  isSpoiler?: boolean;
}

export interface UpdateReviewRequest {
  rating?: number;
  content?: string;
  isSpoiler?: boolean;
}

export interface ReplyResponse {
  id: number;
  userId: number;
  username: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReplyRequest {
  content: string;
}

// --- Watchlist ---

export interface WatchlistResponse {
  id: number;
  movieId: number;
  movieTitle: string;
  movieSlug: string;
  posterUrl: string | null;
  avgRating: number | null;
  addedAt: string;
}

// --- Notification ---

export interface NotificationResponse {
  id: number;
  type: string;
  actorId: number;
  actorName: string;
  referenceId: number;
  message: string;
  read: boolean;
  createdAt: string;
}

// --- Recommendation ---

export interface RecommendationResponse {
  id: number;
  score: number;
  strategyType: string;
  generatedAt: string;
  movie: MovieSummary;
}

export interface MovieSummary {
  id: number;
  title: string;
  slug: string;
  posterUrl: string | null;
  releaseDate: string;
  avgRating: number | null;
  voteCount: number;
  genres: string[];
}
