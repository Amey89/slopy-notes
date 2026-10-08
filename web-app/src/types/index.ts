export interface Note {
  id: string;
  title: string;
  content: string;
  color: string;
  folderId?: string; // Empty string or undefined if none
  createdAt: number; // Unix timestamp ms
  updatedAt: number;
}

// Ensure string for enums matching uppercase Android Enums
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';
export type TaskCategory = 'PERSONAL' | 'WORK' | 'SHOPPING' | 'OTHER';
export type TaskStatus = 'PENDING' | 'LATE' | 'COMPLETED';
export type ReminderType = 'NONE' | 'ONE_TIME' | 'RECURRING';
export type RepeatInterval = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY';

export interface Task {
  id: string;
  title: string;
  description: string;
  isCompleted: boolean;
  priority: Priority;
  category: TaskCategory;
  dueDate: number;
  timeSpent: number; // in seconds
  folderId?: string;
  status: TaskStatus;
  
  // Reminder data
  reminderType: ReminderType;
  reminderTime: number; // 0 if none
  repeatInterval: RepeatInterval;
  
  createdAt: number;
  updatedAt: number;
}

export interface Folder {
  id: string;
  name: string;
  color: string;
  createdAt: number;
  updatedAt: number;
}

export interface BackupPayload {
  exportVersion: number; // 1
  exportedAt: number; // timestamp ms
  notes: Note[];
  tasks: Task[];
  folders: Folder[];
}
