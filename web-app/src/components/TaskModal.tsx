import React, { useState, useEffect } from 'react';
import { X, Save, Trash2, Calendar, Clock, Flag, Tag, Repeat } from 'lucide-react';
import { Task, Priority, TaskCategory, ReminderType, RepeatInterval } from '../types';
import { db } from '../services/db';

interface TaskModalProps {
  task: Task | null;
  onClose: () => void;
  folderId?: string | null;
}

export default function TaskModal({ task, onClose, folderId }: TaskModalProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<Priority>('MEDIUM');
  const [category, setCategory] = useState<TaskCategory>('PERSONAL');
  const [dueDateStr, setDueDateStr] = useState('');
  const [reminderType, setReminderType] = useState<ReminderType>('NONE');
  const [repeatInterval, setRepeatInterval] = useState<RepeatInterval>('NONE');

  useEffect(() => {
    if (task) {
      setTitle(task.title);
      setDescription(task.description);
      setPriority(task.priority);
      setCategory(task.category);
      if (task.dueDate > 0) {
        const d = new Date(task.dueDate);
        const tzoffset = d.getTimezoneOffset() * 60000;
        const localISOTime = (new Date(d.getTime() - tzoffset)).toISOString().slice(0, 16);
        setDueDateStr(localISOTime);
      }
      setReminderType(task.reminderType);
      setRepeatInterval(task.repeatInterval);
    } else {
      setTitle('');
      setDescription('');
      setPriority('MEDIUM');
      setCategory('PERSONAL');
      setDueDateStr('');
      setReminderType('NONE');
      setRepeatInterval('NONE');
    }
  }, [task]);

  const handleSave = async () => {
    const now = Date.now();
    const parsedDueDate = dueDateStr ? new Date(dueDateStr).getTime() : 0;

    if (task) {
      await db.tasks.update(task.id, {
        title,
        description,
        priority,
        category,
        dueDate: parsedDueDate,
        reminderType,
        repeatInterval,
        updatedAt: now,
        // Reset status if due date changes and it was late? Simple logic below.
        status: (parsedDueDate > 0 && parsedDueDate < now && !task.isCompleted) ? 'LATE' : (task.isCompleted ? 'COMPLETED' : 'PENDING')
      });
    } else {
      const newTask: Task = {
        id: crypto.randomUUID(),
        title,
        description,
        isCompleted: false,
        priority,
        category,
        dueDate: parsedDueDate,
        timeSpent: 0,
        folderId: folderId || undefined,
        status: (parsedDueDate > 0 && parsedDueDate < now) ? 'LATE' : 'PENDING',
        reminderType,
        reminderTime: reminderType !== 'NONE' ? parsedDueDate : 0, 
        repeatInterval,
        createdAt: now,
        updatedAt: now
      };
      await db.tasks.add(newTask);
    }
    onClose();
  };

  const handleDelete = async () => {
    if (task && window.confirm('Are you sure you want to delete this task?')) {
      await db.tasks.delete(task.id);
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden flex flex-col">
        <header className="px-6 py-4 border-b flex items-center justify-between bg-slate-50">
          <h2 className="text-lg font-semibold text-slate-800">{task ? 'Edit Task' : 'New Task'}</h2>
          <button onClick={onClose} className="p-2 -mr-2 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-200 transition-colors">
            <X className="w-5 h-5" />
          </button>
        </header>

        <div className="p-6 overflow-y-auto space-y-5 flex-1">
          <div>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Task Title"
              className="w-full text-xl font-medium text-slate-900 focus:outline-none placeholder-slate-400 border-b border-transparent focus:border-brand pb-1 transition-colors"
            />
          </div>

          <div>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Description (optional)"
              rows={3}
              className="w-full resize-none focus:outline-none text-slate-600 placeholder-slate-400 border border-slate-200 rounded-lg p-3 focus:ring-2 focus:ring-brand focus:border-brand transition-all"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-500 uppercase flex items-center"><Flag className="w-3 h-3 mr-1"/> Priority</label>
              <select 
                value={priority} 
                onChange={(e) => setPriority(e.target.value as Priority)}
                className="w-full bg-slate-50 border border-slate-200 text-slate-700 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </div>
            
            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-500 uppercase flex items-center"><Tag className="w-3 h-3 mr-1"/> Category</label>
              <select 
                value={category} 
                onChange={(e) => setCategory(e.target.value as TaskCategory)}
                className="w-full bg-slate-50 border border-slate-200 text-slate-700 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
              >
                <option value="PERSONAL">Personal</option>
                <option value="WORK">Work</option>
                <option value="SHOPPING">Shopping</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>

          <div className="space-y-1">
             <label className="text-xs font-semibold text-slate-500 uppercase flex items-center"><Calendar className="w-3 h-3 mr-1"/> Due Date & Time</label>
             <input 
               type="datetime-local"
               value={dueDateStr}
               onChange={(e) => setDueDateStr(e.target.value)}
               className="w-full bg-slate-50 border border-slate-200 text-slate-700 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
             />
          </div>

          <div className="grid grid-cols-2 gap-4 pt-2 border-t border-slate-100">
             <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-500 uppercase flex items-center"><Clock className="w-3 h-3 mr-1"/> Reminder</label>
              <select 
                value={reminderType} 
                onChange={(e) => setReminderType(e.target.value as ReminderType)}
                className="w-full bg-slate-50 border border-slate-200 text-slate-700 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
              >
                <option value="NONE">None</option>
                <option value="ONE_TIME">One Time (at due date)</option>
              </select>
            </div>
            
            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-500 uppercase flex items-center"><Repeat className="w-3 h-3 mr-1"/> Repeat</label>
              <select 
                value={repeatInterval} 
                onChange={(e) => setRepeatInterval(e.target.value as RepeatInterval)}
                className="w-full bg-slate-50 border border-slate-200 text-slate-700 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
              >
                <option value="NONE">Don't repeat</option>
                <option value="DAILY">Daily</option>
                <option value="WEEKLY">Weekly</option>
                <option value="MONTHLY">Monthly</option>
              </select>
            </div>
          </div>

        </div>

        <footer className="px-6 py-4 bg-slate-50 border-t flex justify-between items-center">
           {task ? (
             <button 
                onClick={handleDelete}
                className="flex items-center space-x-1 text-red-500 hover:text-red-700 hover:bg-red-50 px-3 py-1.5 rounded-lg transition-colors text-sm font-medium"
              >
                <Trash2 className="w-4 h-4" />
                <span>Delete</span>
              </button>
           ) : <div/>}

           <div className="flex space-x-2">
             <button 
                onClick={onClose}
                className="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-200 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button 
                onClick={handleSave}
                disabled={!title.trim()}
                className="flex items-center space-x-2 bg-brand text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-brand/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Save className="w-4 h-4" />
                <span>Save</span>
              </button>
           </div>
        </footer>
      </div>
    </div>
  );
}
