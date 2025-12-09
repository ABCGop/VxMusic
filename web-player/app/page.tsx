'use client';

import { Play } from 'lucide-react';
import { usePlayer } from './context/PlayerContext';
import { Song } from './types';

// Mock Data
const TRENDING_SONGS: Song[] = [
    { id: 'dQw4w9WgXcQ', title: 'Never Gonna Give You Up', artist: 'Rick Astley', thumbnail: 'https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg' },
    { id: '9bZkp7q19f0', title: 'Gangnam Style', artist: 'PSY', thumbnail: 'https://i.ytimg.com/vi/9bZkp7q19f0/maxresdefault.jpg' },
    { id: 'kJQP7kiw5Fk', title: 'Despacito', artist: 'Luis Fonsi', thumbnail: 'https://i.ytimg.com/vi/kJQP7kiw5Fk/maxresdefault.jpg' },
    { id: 'JGwWNGJdvx8', title: 'Shape of You', artist: 'Ed Sheeran', thumbnail: 'https://i.ytimg.com/vi/JGwWNGJdvx8/maxresdefault.jpg' },
    { id: 'OPf0YbXqDm0', title: 'Uptown Funk', artist: 'Mark Ronson', thumbnail: 'https://i.ytimg.com/vi/OPf0YbXqDm0/maxresdefault.jpg' },
    { id: '09R8_2nJtjg', title: 'Sugar', artist: 'Maroon 5', thumbnail: 'https://i.ytimg.com/vi/09R8_2nJtjg/maxresdefault.jpg' },
];

export default function Home() {
    const { playSong } = usePlayer();
    const hours = new Date().getHours();
    const greeting = hours < 12 ? 'Good morning' : hours < 18 ? 'Good afternoon' : 'Good evening';

    // Filter only music (mock logic to match user request)
    const musicOnly = TRENDING_SONGS.filter(s => {
        const lower = (s.title + s.artist).toLowerCase();
        const banned = ["gameplay", "walkthrough", "review", "reaction", "trailer"];
        return !banned.some(b => lower.includes(b));
    });

    return (
        <div className="p-4 md:p-8 pt-16 md:pt-8 aurora-bg min-h-full">
            <h1 className="text-3xl font-bold mb-6">{greeting}</h1>

            {/* Quick Picks */}
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mb-10">
                {TRENDING_SONGS.slice(0, 6).map(song => (
                    <div
                        key={song.id}
                        onClick={() => playSong(song, TRENDING_SONGS)}
                        className="bg-white/5 hover:bg-white/20 transition rounded-md flex items-center overflow-hidden cursor-pointer group"
                    >
                        <img src={song.thumbnail} alt={song.title} className="w-16 h-16 object-cover shadow-lg" />
                        <span className="font-bold px-4 truncate flex-1">{song.title}</span>
                        <div className="mr-4 w-10 h-10 bg-green-500 rounded-full flex items-center justify-center shadow-lg opacity-0 group-hover:opacity-100 transition-all translate-y-2 group-hover:translate-y-0">
                            <Play fill="black" size={20} className="ml-1" />
                        </div>
                    </div>
                ))}
            </div>

            <h2 className="text-2xl font-bold mb-6">Made for you</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-6">
                {TRENDING_SONGS.map(song => (
                    <div
                        key={song.id}
                        className="p-4 bg-[#181818] hover:bg-[#282828] transition rounded-lg cursor-pointer group flex flex-col gap-3"
                        onClick={() => playSong(song, TRENDING_SONGS)}
                    >
                        <div className="relative aspect-square rounded-md overflow-hidden shadow-xl mb-1">
                            <img src={song.thumbnail} alt={song.title} className="w-full h-full object-cover" />
                            <div className="absolute right-2 bottom-2 w-12 h-12 bg-green-500 rounded-full flex items-center justify-center shadow-xl opacity-0 translate-y-2 group-hover:opacity-100 group-hover:translate-y-0 transition-all duration-300">
                                <Play fill="black" size={24} className="ml-1" />
                            </div>
                        </div>
                        <div className="min-h-[60px]">
                            <h3 className="font-bold truncate" title={song.title}>{song.title}</h3>
                            <p className="text-sm text-gray-400 truncate mt-1">By {song.artist}</p>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
