'use client';

export default function AuthorInfo({ author }) {
  return (
    <div className="flex items-center space-x-4 p-4 bg-white dark:bg-gray-800 rounded-lg shadow-sm">
      <div className="w-12 h-12 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center">
        <span className="text-xl">{author?.name?.[0] || '?'}</span>
      </div>
      <div>
        <h3 className="font-semibold text-gray-900 dark:text-white">
          {author?.name || 'Anonymous'}
        </h3>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          {author?.role || 'Author'}
        </p>
      </div>
    </div>
  );
} 