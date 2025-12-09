'use client';

import { useState, useEffect } from 'react';
import { Search, Play, MoreHorizontal } from 'lucide-react';
import { usePlayer } from '../context/PlayerContext';
import { Song } from '../types';

export default function SearchPage() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Song[]>([]);
  const [loading, setLoading] = useState(false);
  const { playSong } = usePlayer();

  useEffect(() => {
    const delayDebounceFn = setTimeout(async () => {
      if (!query.trim()) {
          setResults([]);
          return;
      }

      setLoading(true);
      try {
          const res = await fetch(`/api/search?q=${encodeURIComponent(query)}`);
          if (!res.ok) throw new Error("API Error");
          const songs = await res.json();
          setResults(songs);
      } catch (error) {
          console.error("Search failed", error);
      } finally {
          setLoading(false);
      }
    }, 500);

    return () => clearTimeout(delayDebounceFn);
  }, [query]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // Search is handled by useEffect
  };

  return (
    <div className="p-6 md:p-8 pt-16 md:pt-8 w-full max-w-5xl mx-auto">
        <form onSubmit={handleSearch} className="mb-8 relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
            <input 
                type="text" 
                placeholder="What do you want to listen to?" 
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                className="w-full md:w-96 py-3 pl-12 pr-4 bg-white/10 rounded-full text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-white/20 transition hover:bg-white/15"
                autoFocus
            />
        </form>

        <div className="space-y-4">
            {loading && <div className="text-gray-400 animate-pulse">Searching...</div>}
            
            {!loading && results.length === 0 && query && (
                <div className="text-center py-20 text-gray-500">
                    No results found for "{query}"
                </div>
            )}
            
            {!loading && results.length === 0 && !query && (
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    {['Pop', 'Rock', 'Hip Hop', 'Indie', 'Charts', 'Live Events', 'Made For You', 'New Releases'].map(genre => (
                        <div key={genre} className="aspect-square rounded-xl bg-gradient-to-br from-purple-700 to-blue-600 p-4 font-bold text-xl flex items-start break-all hover:scale-[1.02] transition cursor-pointer">
                            {genre}
                        </div>
                    ))}
                </div>
            )}

            {results.map((song) => (
                <div 
                    key={song.id} 
                    className="flex items-center gap-4 p-2 rounded-lg hover:bg-white/10 transition group cursor-pointer"
                    onClick={() => playSong(song, results)}
                >
                    <div className="relative w-12 h-12 flex-shrink-0">
                        <img src={song.thumbnail} alt={song.title} className="w-full h-full object-cover rounded" />
                        <div className="absolute inset-0 bg-black/50 hidden group-hover:flex items-center justify-center">
                            <Play size={20} fill="white" />
                        </div>
                    </div>
                    
                    <div className="flex-1 min-w-0">
                        <h4 className="font-medium text-white truncate group-hover:text-green-400 transition">{song.title}</h4>
                        <p className="text-sm text-gray-400 truncate">{song.artist}</p>
                    </div>

                    <div className="hidden md:block text-sm text-gray-400">
                        {/* Duration format helper */}
                        {song.duration ? `${Math.floor(song.duration/60)}:${(song.duration%60).toString().padStart(2,'0')}` : ''}
                    </div>

                    <button className="p-2 text-gray-400 hover:text-white opacity-0 group-hover:opacity-100 transition">
                        <MoreHorizontal size={20} />
                    </button>
                </div>
            ))}
        </div>
    </div>
  );
}
