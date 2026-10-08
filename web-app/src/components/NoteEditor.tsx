import React, { useState, useEffect } from 'react';
import { X, Save, Trash2, ArrowLeft } from 'lucide-react';
import { Note } from '../types';
import { db } from '../services/db';
import ReactMarkdown from 'react-markdown';

interface NoteEditorProps {
  note: Note | null;
  onClose: () => void;
  folderId?: string | null;
}

export default function NoteEditor({ note, onClose, folderId }: NoteEditorProps) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [color, setColor] = useState('#E53935');
  const [isPreview, setIsPreview] = useState(false);

  useEffect(() => {
    if (note) {
      setTitle(note.title);
      setContent(note.content);
      setColor(note.color || '#E53935');
    } else {
      setTitle('');
      setContent('');
      setColor('#E53935');
    }
  }, [note]);

  const handleSave = async () => {
    const now = Date.now();
    if (note) {
      await db.notes.update(note.id, {
        title,
        content,
        color,
        updatedAt: now
      });
    } else {
      const newNote: Note = {
        id: crypto.randomUUID(),
        title,
        content,
        color,
        folderId: folderId || undefined,
        createdAt: now,
        updatedAt: now
      };
      await db.notes.add(newNote);
    }
    onClose();
  };

  const handleDelete = async () => {
    if (note && window.confirm('Are you sure you want to delete this note?')) {
      await db.notes.delete(note.id);
      onClose();
    }
  };

  const colors = ['#E53935', '#D81B60', '#8E24AA', '#5E35B1', '#3949AB', '#1E88E5', '#039BE5', '#00ACC1', '#00897B', '#43A047', '#7CB342'];

  return (
    <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4 sm:p-6">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-4xl h-full max-h-[90vh] flex flex-col overflow-hidden">
        
        <header className="px-6 py-4 border-b flex items-center justify-between bg-slate-50">
          <div className="flex items-center space-x-4">
            <button onClick={onClose} className="p-2 -ml-2 text-slate-500 hover:text-slate-800 rounded-full hover:bg-slate-200 transition-colors">
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div className="flex items-center space-x-2">
              <button 
                onClick={() => setIsPreview(false)}
                className={\`px-3 py-1.5 rounded-md text-sm font-medium transition-colors \${!isPreview ? 'bg-white shadow-sm text-brand' : 'text-slate-500 hover:text-slate-800'}\`}
              >
                Edit
              </button>
              <button 
                onClick={() => setIsPreview(true)}
                className={\`px-3 py-1.5 rounded-md text-sm font-medium transition-colors \${isPreview ? 'bg-white shadow-sm text-brand' : 'text-slate-500 hover:text-slate-800'}\`}
              >
                Preview
              </button>
            </div>
          </div>
          
          <div className="flex items-center space-x-3">
            {note && (
              <button 
                onClick={handleDelete}
                className="p-2 text-slate-400 hover:text-red-500 rounded-full hover:bg-red-50 transition-colors"
                title="Delete"
              >
                <Trash2 className="w-5 h-5" />
              </button>
            )}
            <button 
              onClick={handleSave}
              className="flex items-center space-x-2 bg-brand text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-brand/90 transition-colors"
            >
              <Save className="w-4 h-4" />
              <span>Save</span>
            </button>
          </div>
        </header>

        <div className="flex-1 flex overflow-hidden">
          <div className="flex-1 flex flex-col p-6 overflow-y-auto">
            {isPreview ? (
              <div className="max-w-3xl mx-auto w-full">
                <h1 className="text-4xl font-bold text-slate-900 mb-8">{title || 'Untitled'}</h1>
                <div className="prose prose-slate max-w-none">
                  <ReactMarkdown>{content}</ReactMarkdown>
                </div>
              </div>
            ) : (
              <div className="max-w-3xl mx-auto w-full flex flex-col h-full">
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Note Title"
                  className="text-4xl font-bold text-slate-900 mb-6 focus:outline-none placeholder-slate-300 bg-transparent"
                />
                <textarea
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="Start typing your note... (Markdown supported)"
                  className="flex-1 resize-none focus:outline-none text-lg text-slate-700 leading-relaxed placeholder-slate-400 bg-transparent"
                />
              </div>
            )}
          </div>
          
          {!isPreview && (
            <div className="w-16 border-l bg-slate-50 flex flex-col items-center py-6 space-y-4">
              <span className="text-xs font-semibold text-slate-400 uppercase">Color</span>
              {colors.map(c => (
                <button
                  key={c}
                  onClick={() => setColor(c)}
                  className={\`w-8 h-8 rounded-full transition-transform \${color === c ? 'ring-2 ring-offset-2 ring-slate-400 scale-110' : 'hover:scale-110'}\`}
                  style={{ backgroundColor: c }}
                  title={c}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
