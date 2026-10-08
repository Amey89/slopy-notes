import { useState, useCallback } from 'react';
import { db } from '../services/db';
import { driveService } from '../services/googleDriveService';
import { BackupPayload, Note, Task, Folder } from '../types';

export function useSync() {
  const [isSyncing, setIsSyncing] = useState(false);
  const [lastSyncTime, setLastSyncTime] = useState<number | null>(null);
  const [syncError, setSyncError] = useState<string | null>(null);

  const performSync = useCallback(async () => {
    if (!driveService.hasToken()) {
      setSyncError("Not authenticated");
      return;
    }

    setIsSyncing(true);
    setSyncError(null);

    try {
      // 1. Get remote data if exists
      const fileId = await driveService.findBackupFile();
      let remotePayload: BackupPayload | null = null;
      if (fileId) {
        remotePayload = await driveService.downloadBackup(fileId);
      }

      // 2. Get local data
      const localNotes = await db.notes.toArray();
      const localTasks = await db.tasks.toArray();
      const localFolders = await db.folders.toArray();

      // 3. Merge Strategy (Last Write Wins by updatedAt)
      const mergedNotes = mergeItems(localNotes, remotePayload?.notes || []);
      const mergedTasks = mergeItems(localTasks, remotePayload?.tasks || []);
      const mergedFolders = mergeItems<Folder>(localFolders, remotePayload?.folders || []);

      // 4. Update local DB with merged data
      await db.transaction('rw', db.notes, db.tasks, db.folders, async () => {
        await db.notes.clear();
        await db.notes.bulkAdd(mergedNotes);
        
        await db.tasks.clear();
        await db.tasks.bulkAdd(mergedTasks);
        
        await db.folders.clear();
        await db.folders.bulkAdd(mergedFolders);
      });

      // 5. Upload merged data back to Google Drive
      const newPayload: BackupPayload = {
        exportVersion: 1,
        exportedAt: Date.now(),
        notes: mergedNotes,
        tasks: mergedTasks,
        folders: mergedFolders
      };

      await driveService.uploadBackup(newPayload, fileId);

      const now = Date.now();
      await db.syncMeta.put({ id: 'lastSync', lastSyncedAt: now });
      setLastSyncTime(now);

    } catch (err: any) {
      console.error("Sync error:", err);
      setSyncError(err.message || "Failed to sync");
    } finally {
      setIsSyncing(false);
    }
  }, []);

  const loadInitialState = useCallback(async () => {
    const meta = await db.syncMeta.get('lastSync');
    if (meta) {
      setLastSyncTime(meta.lastSyncedAt);
    }
  }, []);

  return {
    isSyncing,
    lastSyncTime,
    syncError,
    performSync,
    loadInitialState
  };
}

function mergeItems<T extends { id: string; updatedAt: number }>(localItems: T[], remoteItems: T[]): T[] {
  const mergedMap = new Map<string, T>();

  // Add all local
  localItems.forEach(item => {
    mergedMap.set(item.id, item);
  });

  // Merge remote (replace if remote is newer)
  remoteItems.forEach(remoteItem => {
    const localItem = mergedMap.get(remoteItem.id);
    if (!localItem || remoteItem.updatedAt > localItem.updatedAt) {
      mergedMap.set(remoteItem.id, remoteItem);
    }
  });

  return Array.from(mergedMap.values());
}
