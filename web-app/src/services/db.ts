import Dexie, { Table } from 'dexie';
import { Note, Task, Folder } from '../types';

export class SlopeNotesDB extends Dexie {
  notes!: Table<Note, string>;
  tasks!: Table<Task, string>;
  folders!: Table<Folder, string>;

  // Sync tracking table (metadata)
  syncMeta!: Table<{ id: string; lastSyncedAt: number }, string>;

  constructor() {
    super('SlopyNotesAndTasksDB');
    this.version(1).stores({
      notes: 'id, folderId, updatedAt, createdAt',
      tasks: 'id, folderId, updatedAt, createdAt, isCompleted, dueDate',
      folders: 'id, updatedAt',
      syncMeta: 'id'
    });
  }
}

export const db = new SlopeNotesDB();
