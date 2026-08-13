// Espejo de com.cinemahub.cinemahub.security.dto.* (backend)

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BLOCKED' | 'PENDING_VERIFICATION';

export interface Role {
  id: number;
  name: string;
  description: string | null;
}

export interface RoleRequest {
  name: string;
  description?: string;
}

export interface Permission {
  id: number;
  name: string;
  description: string | null;
}

export interface PermissionRequest {
  name: string;
  description?: string;
}

// Nunca incluye password: el backend nunca lo devuelve en UserResponse.
export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  birthDate: string | null;
  status: UserStatus;
  createdAt: string;
}

export interface RegisterUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface ChangeUserStatusRequest {
  status: UserStatus;
}

export interface AssignRoleRequest {
  roleId: number;
}

export interface AssignPermissionRequest {
  permissionId: number;
}