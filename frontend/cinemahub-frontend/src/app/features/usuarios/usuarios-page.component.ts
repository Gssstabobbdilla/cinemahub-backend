import { Component, OnInit, inject, signal } from '@angular/core';

import { AppError } from '../../core/interceptors/error.interceptor';
import { Role, User } from '../../core/models/security.model';
import { RoleService } from '../../core/services/role.service';
import { UserService } from '../../core/services/user.service';

// Gestión de roles de usuarios. Reutiliza exactamente los mismos endpoints/servicios
// que ya existían (UserService.findAll/findRoles/assignRole/removeRole, RoleService.findAll);
// no se agrega nada nuevo al backend. El patrón de "agregar rol inline por fila" es el
// mismo que startAddGenre/confirmAddGenre/removeGenre en PeliculasPageComponent.
@Component({
  selector: 'app-usuarios-page',
  standalone: true,
  templateUrl: './usuarios-page.component.html',
  styleUrl: './usuarios-page.component.scss'
})
export class UsuariosPageComponent implements OnInit {
  private userService = inject(UserService);
  private roleService = inject(RoleService);

  users = signal<User[]>([]);
  allRoles = signal<Role[]>([]);
  userRoles = signal<Record<number, Role[]>>({});

  loading = signal(true);
  error = signal<string | null>(null);

  // --- asignar rol (inline por fila) ---
  assigningRoleFor = signal<number | null>(null);
  selectedRoleId = signal<number | null>(null);
  savingRole = signal(false);

  ngOnInit(): void {
    this.roleService.findAll().subscribe({
      next: roles => this.allRoles.set(roles)
    });
    this.loadUsers();
  }

  private loadUsers(): void {
    this.loading.set(true);
    this.userService.findAll().subscribe({
      next: users => {
        this.users.set(users);
        this.loading.set(false);
        users.forEach(u => this.loadRolesFor(u.id));
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }

  private loadRolesFor(userId: number): void {
    this.userService.findRoles(userId).subscribe({
      next: roles => {
        this.userRoles.update(current => ({ ...current, [userId]: roles }));
      }
    });
  }

  rolesFor(userId: number): Role[] {
    return this.userRoles()[userId] ?? [];
  }

  startAssignRole(userId: number): void {
    this.assigningRoleFor.set(userId);
    this.selectedRoleId.set(this.allRoles()[0]?.id ?? null);
    this.error.set(null);
  }

  cancelAssignRole(): void {
    this.assigningRoleFor.set(null);
  }

  onSelectedRoleChange(event: Event): void {
    this.selectedRoleId.set(Number((event.target as HTMLSelectElement).value));
  }

  confirmAssignRole(userId: number): void {
    const roleId = this.selectedRoleId();
    if (!roleId) {
      return;
    }

    this.savingRole.set(true);
    this.error.set(null);

    // assignRole es idempotente en el backend (UserService.assignRole hace
    // findById(id).orElseGet(...)), así que no hace falta validar en el frontend
    // si el usuario ya tenía ese rol — no se duplica lógica del backend acá.
    this.userService.assignRole(userId, { roleId }).subscribe({
      next: () => {
        this.loadRolesFor(userId);
        this.assigningRoleFor.set(null);
        this.savingRole.set(false);
      },
      error: (err: AppError) => {
        this.error.set(err.message);
        this.savingRole.set(false);
      }
    });
  }

  removeRole(userId: number, roleId: number): void {
    this.error.set(null);
    this.userService.removeRole(userId, roleId).subscribe({
      next: () => this.loadRolesFor(userId),
      error: (err: AppError) => this.error.set(err.message)
    });
  }
}