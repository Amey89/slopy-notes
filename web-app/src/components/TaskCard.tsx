import React from 'react';
import { Task } from '../types';
import { AlertCircle, Calendar, CheckCircle2, Circle } from 'lucide-react';
import { db } from '../services/db';

interface TaskCardProps {
  task: Task;
  onClick: () => void;
}

export default function TaskCard({ task, onClick }: TaskCardProps) {
  const toggleComplete = async (e: React.MouseEvent) => {
    e.stopPropagation();
    await db.tasks.update(task.id, { 
      isCompleted: !task.isCompleted,
      updatedAt: Date.now()
    });
  };

  const priorityColors = {
    HIGH: 'text-red-500 bg-red-50',
    MEDIUM: 'text-orange-500 bg-orange-50',
    LOW: 'text-blue-500 bg-blue-50'
  };

  const getPriorityColor = (p: string) => {
      if(p==='HIGH') return priorityColors.HIGH;
      if(p==='MEDIUM') return priorityColors.MEDIUM;
      return priorityColors.LOW;
  }

  return (
    <div 
      onClick={onClick}
      className={`bg-white rounded-xl p-4 shadow-sm border \${task.isCompleted ? 'border-slate-100 opacity-75' : 'border-slate-200'} hover:shadow-md transition-all cursor-pointer flex items-start space-x-4`}
    >
      <button onClick={toggleComplete} className="mt-1 flex-shrink-0 focus:outline-none">
        {task.isCompleted ? (
          <CheckCircle2 className="w-6 h-6 text-emerald-500" />
        ) : (
          <Circle className="w-6 h-6 text-slate-300 hover:text-brand transition-colors" />
        )}
      </button>
      
      <div className="flex-1 min-w-0">
        <h3 className={`font-medium text-base truncate \${task.isCompleted ? 'text-slate-400 line-through' : 'text-slate-800'}`}>
          {task.title || 'Untitled Task'}
        </h3>
        {task.description && (
          <p className="text-sm text-slate-500 line-clamp-2 mt-1">
            {task.description}
          </p>
        )}
        
        <div className="flex items-center space-x-3 mt-3 text-xs">
          <span className={`px-2 py-1 rounded-md font-medium \${getPriorityColor(task.priority)}`}>
            {task.priority}
          </span>
          {task.dueDate > 0 && (
            <span className={`flex items-center \${task.dueDate < Date.now() && !task.isCompleted ? 'text-red-500' : 'text-slate-500'}`}>
              <Calendar className="w-3.5 h-3.5 mr-1" />
              {new Date(task.dueDate).toLocaleDateString()}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
