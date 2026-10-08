import React, { useState } from 'react';
import { useLiveQuery } from 'dexie-react-hooks';
import { Plus } from 'lucide-react';
import { db } from '../services/db';
import { Note, Task } from '../types';
import NoteCard from './NoteCard';
import TaskCard from './TaskCard';
import NoteEditor from './NoteEditor';
import TaskModal from './TaskModal';

interface DashboardProps {
  activeTab: 'notes' | 'tasks';
  activeFolderId: string | null;
  searchQuery: string;
}

export default function Dashboard({ activeTab, activeFolderId, searchQuery }: DashboardProps) {
  const [editingNote, setEditingNote] = useState<Note | null>(null);
  const [isNoteEditorOpen, setIsNoteEditorOpen] = useState(false);
  
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);

  // Queries
  const allNotes = useLiveQuery(() => db.notes.toArray()) || [];
  const allTasks = useLiveQuery(() => db.tasks.toArray()) || [];

  // Filtering
  const filteredNotes = allNotes
    .filter(n => activeFolderId ? n.folderId === activeFolderId : true)
    .filter(n => !searchQuery || n.title.toLowerCase().includes(searchQuery.toLowerCase()) || n.content.toLowerCase().includes(searchQuery.toLowerCase()))
    .sort((a,b) => b.updatedAt - a.updatedAt);

  const filteredTasks = allTasks
    .filter(t => activeFolderId ? t.folderId === activeFolderId : true)
    .filter(t => !searchQuery || t.title.toLowerCase().includes(searchQuery.toLowerCase()) || t.description.toLowerCase().includes(searchQuery.toLowerCase()))
    .sort((a,b) => {
       if (a.isCompleted !== b.isCompleted) return a.isCompleted ? 1 : -1;
       return b.updatedAt - a.updatedAt;
    });


  const handleOpenNote = (note?: Note) => {
    setEditingNote(note || null);
    setIsNoteEditorOpen(true);
  };

  const handleOpenTask = (task?: Task) => {
    setEditingTask(task || null);
    setIsTaskModalOpen(true);
  };

  return (
    <div className="h-full flex flex-col max-w-7xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-slate-800">
           {activeTab === 'notes' ? (activeFolderId ? 'Folder Notes' : 'All Notes') : (activeFolderId ? 'Folder Tasks' : 'All Tasks')}
        </h2>
        
        <button 
          onClick={() => activeTab === 'notes' ? handleOpenNote() : handleOpenTask()}
          className="bg-brand hover:bg-brand/90 text-white px-4 py-2 rounded-lg shadow-sm flex items-center space-x-2 transition-colors font-medium text-sm"
        >
          <Plus className="w-5 h-5" />
          <span>New {activeTab === 'notes' ? 'Note' : 'Task'}</span>
        </button>
      </div>

      {activeTab === 'notes' ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredNotes.length === 0 ? (
            <div className="col-span-full text-center py-12 text-slate-500">
               <p className="text-lg">No notes found.</p>
               <p className="text-sm">Click "New Note" to create one.</p>
            </div>
          ) : (
            filteredNotes.map(note => (
              <NoteCard key={note.id} note={note} onClick={() => handleOpenNote(note)} />
            ))
          )}
        </div>
      ) : (
        <div className="space-y-3 max-w-3xl">
          {filteredTasks.length === 0 ? (
            <div className="text-center py-12 text-slate-500">
               <p className="text-lg">No tasks found.</p>
               <p className="text-sm">Click "New Task" to create one.</p>
            </div>
          ) : (
            filteredTasks.map(task => (
              <TaskCard key={task.id} task={task} onClick={() => handleOpenTask(task)} />
            ))
          )}
        </div>
      )}

      {isNoteEditorOpen && (
        <NoteEditor 
          note={editingNote} 
          folderId={activeFolderId}
          onClose={() => setIsNoteEditorOpen(false)} 
        />
      )}

      {isTaskModalOpen && (
        <TaskModal
          task={editingTask}
          folderId={activeFolderId}
          onClose={() => setIsTaskModalOpen(false)}
        />
      )}
    </div>
  );
}
