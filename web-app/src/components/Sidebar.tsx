import React from 'react';
import { useLiveQuery } from 'dexie-react-hooks';
import { LayoutDashboard, CheckSquare, Folder as FolderIcon, Settings, Plus, LayoutGrid } from 'lucide-react';
import { db } from '../services/db';

interface SidebarProps {
  activeTab: 'notes' | 'tasks';
  setActiveTab: (tab: 'notes' | 'tasks') => void;
  activeFolderId: string | null;
  setActiveFolderId: (id: string | null) => void;
}

export default function Sidebar({ activeTab, setActiveTab, activeFolderId, setActiveFolderId }: SidebarProps) {
  const folders = useLiveQuery(() => db.folders.toArray()) || [];

  return (
    <aside className="w-64 bg-slate-900 h-full flex flex-col text-slate-300">
      <div className="p-6 flex items-center space-x-3">
        <div className="w-8 h-8 rounded-lg bg-brand flex items-center justify-center font-bold text-white shadow-lg">
          S
        </div>
        <h1 className="text-xl font-semibold text-white tracking-tight">Slopy Notes</h1>
      </div>

      <nav className="flex-1 px-4 space-y-1 overflow-y-auto">
        <div className="mb-8">
          <p className="px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Overview</p>
          <button 
            onClick={() => { setActiveTab('notes'); setActiveFolderId(null); }}
            className={\`w-full flex items-center px-3 py-2.5 text-sm font-medium rounded-lg transition-colors \${activeTab === 'notes' && !activeFolderId ? 'bg-slate-800 text-white' : 'hover:bg-slate-800/50 hover:text-white'}\`}
          >
            <LayoutDashboard className="w-5 h-5 mr-3 text-brand" />
            All Notes
          </button>
          <button 
            onClick={() => { setActiveTab('tasks'); setActiveFolderId(null); }}
            className={\`w-full flex items-center px-3 py-2.5 text-sm font-medium rounded-lg transition-colors mt-1 \${activeTab === 'tasks' && !activeFolderId ? 'bg-slate-800 text-white' : 'hover:bg-slate-800/50 hover:text-white'}\`}
          >
            <CheckSquare className="w-5 h-5 mr-3 text-brand" />
            All Tasks
          </button>
        </div>

        <div>
          <div className="flex items-center justify-between px-3 mb-2">
             <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Folders</p>
             <button className="text-slate-400 hover:text-white transition-colors" title="Add Folder">
                <Plus className="w-4 h-4" />
             </button>
          </div>
          {folders.length === 0 ? (
            <p className="px-3 text-sm text-slate-500 italic">No folders yet</p>
          ) : (
            folders.map(folder => (
              <button 
                key={folder.id}
                onClick={() => setActiveFolderId(folder.id)}
                className={\`w-full flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors \${activeFolderId === folder.id ? 'bg-slate-800 text-white' : 'hover:bg-slate-800/50 hover:text-white'}\`}
              >
                <FolderIcon className="w-4 h-4 mr-3" style={{ color: folder.color }}/>
                {folder.name}
              </button>
            ))
          )}
        </div>
      </nav>

      <div className="p-4 border-t border-slate-800">
        <button className="w-full flex items-center px-3 py-2.5 text-sm font-medium rounded-lg hover:bg-slate-800 transition-colors">
          <Settings className="w-5 h-5 mr-3 text-slate-400" />
          Settings
        </button>
      </div>
    </aside>
  );
}
