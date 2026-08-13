// Espejo de com.cinemahub.cinemahub.notification.dto.* (backend)

export interface Notification {
  id: number;
  userId: number;
  title: string;
  message: string | null;
  read: boolean;
  createdAt: string;
}

export interface CreateNotificationRequest {
  userId: number;
  title: string;
  message?: string;
}