import React, { useState, useEffect } from 'react';
import { useGoogleLogin } from '@react-oauth/google';
import { Cloud, CloudOff, RefreshCw } from 'lucide-react';
import { driveService } from './services/googleDriveService';
import { useSync } from './hooks/useSync';
import Sidebar from './components/Sidebar';
import SearchBar from './components/SearchBar';
import Dashboard from './components/Dashboard';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const { isSyncing, lastSyncTime, syncError, performSync, loadInitialState } = useSync();

  const [activeTab, setActiveTab] = useState<'notes' | 'tasks'>('notes');
  const [activeFolderId, setActiveFolderId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    loadInitialState();
  }, [loadInitialState]);

  const login = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      driveService.setToken(tokenResponse.access_token);
      setIsAuthenticated(true);
      await performSync();
    },
    onError: (error) => console.log('Login Failed:', error),
    scope: 'https://www.googleapis.com/auth/drive.appdata https://www.googleapis.com/auth/drive.file'
  });

  return (
    <div className="flex h-screen w-full font-sans">
      <Sidebar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab}
        activeFolderId={activeFolderId}
        setActiveFolderId={setActiveFolderId}
      />
      
      <main className="flex-1 flex flex-col h-full bg-white relative">
        <header className="h-16 border-b flex items-center justify-between px-6 bg-white/80 backdrop-blur-md sticky top-0 z-10 w-full">
          <div className="flex-1 max-w-2xl">
            <SearchBar value={searchQuery} onChange={setSearchQuery} />
          </div>
          
          <div className="flex items-center space-x-4 ml-4">
            <div className="flex flex-col items-end text-xs text-slate-500 mr-2">
              {isSyncing ? (
                <span className="flex items-center text-blue-500"><RefreshCw className="w-3 h-3 mr-1 animate-spin"/> Syncing...</span>
              ) : syncError ? (
                <span className="text-red-500" title={syncError}>Sync Failed</span>
              ) : lastSyncTime ? (
                <span>Synced: {new Date(lastSyncTime).toLocaleTimeString()}</span>
              ) : (
                <span>Not synced</span>
              )}
            </div>

            {!isAuthenticated ? (
              <button 
                onClick={() => login()}
                className="flex items-center space-x-2 bg-brand text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-brand/90 transition-colors"
                title="Sign in with Google to sync with Android App via Drive"
              >
                <CloudOff className="w-4 h-4" />
                <span>Connect Sync</span>
              </button>
            ) : (
              <button 
                onClick={() => performSync()}
                disabled={isSyncing}
                className="p-2 text-slate-600 hover:bg-slate-100 rounded-full transition-colors disabled:opacity-50"
                title="Force Sync"
              >
                <Cloud className="w-5 h-5 text-brand" />
              </button>
            )}
          </div>
        </header>

        <div className="flex-1 overflow-auto bg-slate-50 p-6 w-full">
           <Dashboard 
             activeTab={activeTab} 
             activeFolderId={activeFolderId}
             searchQuery={searchQuery}
           />
        </div>
      </main>
    </div>
  );
}

export default App;
