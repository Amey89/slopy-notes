import React from 'react';
import ReactMarkdown from 'react-markdown';
import { Note } from '../types';

interface NoteCardProps {
  note: Note;
  onClick: () => void;
}

export default function NoteCard({ note, onClick }: NoteCardProps) {
  const date = new Date(note.updatedAt).toLocaleDateString(undefined, { 
    month: 'short', 
    day: 'numeric' 
  });

  return (
    <div 
      onClick={onClick}
      className="bg-white rounded-xl p-5 shadow-sm border border-slate-100 hover:shadow-md transition-all cursor-pointer flex flex-col h-64 group relative overflow-hidden"
    >
      <div 
        className="absolute top-0 left-0 w-1 h-full"
        style={{ backgroundColor: note.color || '#E53935' }}
      />
      <h3 className="font-semibold text-lg text-slate-800 line-clamp-1 mb-2">
        {note.title || 'Untitled Note'}
      </h3>
      <div className="text-slate-600 text-sm flex-1 overflow-hidden prose prose-sm max-w-none">
        <ReactMarkdown>{note.content}</ReactMarkdown>
      </div>
      <div className="mt-4 flex items-center justify-between text-xs text-slate-400 pt-3 border-t border-slate-50">
        <span>{date}</span>
      </div>
    </div>
  );
}
