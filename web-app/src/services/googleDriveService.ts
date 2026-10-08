import { BackupPayload } from "../types";

const GOOGLE_DRIVE_API_URL = 'https://www.googleapis.com/drive/v3/files';
const UPLOAD_API_URL = 'https://www.googleapis.com/upload/drive/v3/files';
const FILENAME = 'notes_tasks_backup.json';

export class GoogleDriveService {
  private accessToken: string | null = null;

  setToken(token: string) {
    this.accessToken = token;
  }

  hasToken(): boolean {
    return !!this.accessToken;
  }

  clearToken() {
    this.accessToken = null;
  }

  private get headers() {
    if (!this.accessToken) throw new Error("Not authenticated");
    return {
      Authorization: `Bearer \${this.accessToken}`,
    };
  }

  async findBackupFile(): Promise<string | null> {
    const q = `name = '\${FILENAME}' and trashed = false and spaces = 'appDataFolder'`;
    const response = await fetch(`\${GOOGLE_DRIVE_API_URL}?q=\${encodeURIComponent(q)}&spaces=appDataFolder`, {
      method: "GET",
      headers: this.headers,
    });
    
    if (!response.ok) {
      if (response.status === 401) {
        this.clearToken();
      }
      throw new Error("Failed to search for backup file");
    }

    const data = await response.json();
    if (data.files && data.files.length > 0) {
      return data.files[0].id;
    }
    return null;
  }

  async downloadBackup(fileId: string): Promise<BackupPayload> {
    const response = await fetch(`\${GOOGLE_DRIVE_API_URL}/\${fileId}?alt=media`, {
      method: "GET",
      headers: this.headers,
    });

    if (!response.ok) {
      if (response.status === 401) {
        this.clearToken();
      }
      throw new Error("Failed to download backup file");
    }

    return response.json();
  }

  async uploadBackup(payload: BackupPayload, existingFileId: string | null): Promise<void> {
    const boundary = '-------314159265358979323846';
    const delimiter = `\\r\\n--\${boundary}\\r\\n`;
    const close_delim = `\\r\\n--\${boundary}--\\r\\n`;

    const metadata = {
      name: FILENAME,
      mimeType: 'application/json',
      parents: existingFileId ? undefined : ['appDataFolder']
    };

    const multipartRequestBody =
      delimiter +
      'Content-Type: application/json; charset=UTF-8\\r\\n\\r\\n' +
      JSON.stringify(metadata) +
      delimiter +
      'Content-Type: application/json; charset=UTF-8\\r\\n\\r\\n' +
      JSON.stringify(payload) +
      close_delim;

    const url = existingFileId 
      ? `\${UPLOAD_API_URL}/\${existingFileId}?uploadType=multipart`
      : `\${UPLOAD_API_URL}?uploadType=multipart`;

    const response = await fetch(url, {
      method: existingFileId ? 'PATCH' : 'POST',
      headers: {
        ...this.headers,
        'Content-Type': `multipart/related; boundary=\${boundary}`
      },
      body: multipartRequestBody
    });

    if (!response.ok) {
        if (response.status === 401) {
          this.clearToken();
        }
      throw new Error(`Failed to upload backup: \${response.statusText}`);
    }
  }
}

export const driveService = new GoogleDriveService();
