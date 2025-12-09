'use client';

import { useSearchParams } from 'next/navigation';
import { useEffect, useState, useRef } from 'react';
import ReactPlayer from 'react-player/youtube';
import Link from 'next/link';
import { Play, Pause, SkipForward, SkipBack, Share2, Volume2, ExternalLink, Repeat, Shuffle, Heart } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

import { Suspense } from 'react';

function PlayerContent() {
  const searchParams = useSearchParams();
  const id = searchParams.get('id');
  const [playing, setPlaying] = useState(true);
  const [mounted, setMounted] = useState(false);
  const [volume, setVolume] = useState(0.8);
  const [duration, setDuration] = useState(0);
  const [played, setPlayed] = useState(0);
  const [liked, setLiked] = useState(false);
  const playerRef = useRef<ReactPlayer>(null);

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) return null;

  if (!id) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-black text-white">
        <h1 className="text-2xl font-bold mb-4">No song selected</h1>
        <Link href="/" className="px-6 py-2 bg-white/10 rounded-full hover:bg-white/20 transition">
          Return Home
        </Link>
      </div>
    );
  }

  const url = `https://www.youtube.com/watch?v=${id}`;
  const imgUrl = `https://i.ytimg.com/vi/${id}/maxresdefault.jpg`;

  const handleSeekChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const newPlayed = parseFloat(e.target.value);
      setPlayed(newPlayed);
      playerRef.current?.seekTo(newPlayed);
  }
  
  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${minutes}:${secs < 10 ? '0' : ''}${secs}`;
  }

  return (
    <div className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden bg-background text-white selection:bg-primary">
       <div className="noise-overlay" />

      {/* Dynamic Background */}
      <div 
        className="absolute inset-0 z-0 opacity-30 transform scale-150 blur-[120px] transition-all duration-1000"
        style={{ backgroundImage: `url(${imgUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }}
      />
      
      {/* Navbar */}
      <div className="absolute top-0 left-0 w-full p-6 flex justify-between items-center z-50">
          <Link href="/" className="font-bold tracking-tight px-4 py-2 rounded-full glass-button text-sm">
             VxMusic
          </Link>
      </div>

      <div className="relative z-10 w-full max-w-6xl p-4 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
        
        {/* Left: Visuals */}
        <div className="flex flex-col items-center justify-center">
             {/* Hidden Player - 1px size to prevent browser culling */}
             <div className="absolute top-0 left-0 w-px h-px opacity-0 overflow-hidden pointer-events-none">
                <ReactPlayer
                    ref={playerRef}
                    url={url}
                    playing={playing}
                    volume={volume}
                    controls={false}
                    onProgress={(state) => setPlayed(state.played)}
                    onDuration={setDuration}
                    onEnded={() => setPlaying(false)}
                    config={{ 
                        playerVars: { 
                            autoplay: 1,
                            playsinline: 1,
                            origin: typeof window !== 'undefined' ? window.location.origin : undefined
                        } 
                    }}
                />
            </div>
            
            {/* Vinyl Container */}
            <motion.div 
               initial={{ scale: 0.9, opacity: 0 }}
               animate={{ scale: 1, opacity: 1 }}
               transition={{ duration: 0.5 }}
               className="relative group"
            >
                <motion.div 
                  animate={{ rotate: playing ? 360 : 0 }}
                  transition={{ duration: 8, repeat: Infinity, ease: "linear", paused: !playing }}
                  className="w-[300px] h-[300px] md:w-[450px] md:h-[450px] rounded-full overflow-hidden shadow-2xl shadow-black/50 border-[8px] border-surfaceHighlight relative bg-black"
                >
                     <img src={imgUrl} alt="Album Art" className="w-full h-full object-cover opacity-90 group-hover:scale-105 transition duration-700" />
                     {/* Vinyl Reflection */}
                     <div className="absolute inset-0 bg-gradient-to-tr from-white/10 to-transparent pointer-events-none" />
                     {/* Center hole */}
                     <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-28 h-28 bg-surfaceHighlight rounded-full flex items-center justify-center shadow-inner">
                        <div className="w-3 h-3 bg-black rounded-full" />
                     </div>
                </motion.div>
                
                {/* Visualizer Bars simulation */}
                <div className="absolute -bottom-12 left-1/2 -translate-x-1/2 flex items-end gap-1 h-8">
                     {[...Array(12)].map((_, i) => (
                        <div 
                            key={i} 
                            className={`w-1 bg-white/40 rounded-full transition-all duration-300 ${playing ? 'animate-pulse' : ''}`} 
                            style={{ 
                                height: playing ? `${Math.random() * 100}%` : '4px',
                                animationDelay: `${i * 0.05}s` 
                            }} 
                        />
                     ))}
                </div>
            </motion.div>
        </div>

        {/* Right: Controls */}
        <div className="flex flex-col justify-center glass-panel p-8 md:p-12 rounded-[2.5rem]">
            
            <div className="flex justify-between items-start mb-8">
                 <div className="overflow-hidden">
                    <h1 className="text-3xl md:text-5xl font-bold mb-2 leading-tight">
                         Shared Song
                    </h1>
                    <p className="text-gray-400 text-lg flex items-center gap-2">
                        <span className="w-2 h-2 rounded-full bg-primary animate-pulse" />
                        Live on Web Player
                    </p>
                 </div>
                 <button onClick={() => setLiked(!liked)} className={`p-3 rounded-full transition ${liked ? 'text-red-500 bg-red-500/10' : 'text-gray-400 hover:bg-white/5'}`}>
                      <Heart fill={liked ? "currentColor" : "none"} />
                 </button>
            </div>

            {/* Progress Bar */}
            <div className="mb-8 group">
                <input 
                    type="range" 
                    min={0} 
                    max={0.999999} 
                    step="any"
                    value={played} 
                    onChange={handleSeekChange}
                    className="w-full h-2 bg-white/10 rounded-lg appearance-none cursor-pointer accent-white hover:accent-primary transition-all" 
                />
                 <div className="flex justify-between text-xs text-gray-500 mt-2 font-mono group-hover:text-gray-300 transition">
                    <span>{formatTime(played * duration)}</span>
                    <span>{formatTime(duration)}</span>
                </div>
            </div>

            {/* Playback Controls */}
            <div className="flex justify-between items-center mb-10">
                <button className="text-gray-500 hover:text-white transition">
                    <Shuffle size={20} />
                </button>
                
                <div className="flex items-center gap-8">
                    <button className="text-white hover:text-primary transition transform hover:-translate-x-1">
                        <SkipBack size={32} />
                    </button>
                    <button
                        onClick={() => setPlaying(!playing)}
                        className="w-20 h-20 bg-white rounded-full flex items-center justify-center text-black hover:scale-105 active:scale-95 transition shadow-lg shadow-white/20"
                    >
                        {playing ? <Pause size={32} fill="black" /> : <Play size={32} fill="black" className="ml-1" />}
                    </button>
                    <button className="text-white hover:text-primary transition transform hover:translate-x-1">
                        <SkipForward size={32} />
                    </button>
                </div>

                 <button className="text-gray-500 hover:text-white transition">
                    <Repeat size={20} />
                </button>
            </div>

            {/* Actions */}
            <div className="space-y-4">
                 <a 
                    href={`vxmusic://play?id=${id}`}
                    className="w-full py-4 bg-primary hover:bg-blue-600 rounded-2xl text-white font-bold text-center transition flex items-center justify-center gap-3 shadow-lg shadow-blue-500/20 group hover:-translate-y-1"
                 >
                    <span className="bg-white/20 p-1 rounded-full"><ExternalLink size={16} /></span>
                    Open in VxMusic App
                 </a>

                 <div className="flex gap-4">
                    <button 
                         onClick={() => {
                             // Mock Share
                             if (navigator.share) navigator.share({ title: 'VxMusic', url: window.location.href });
                         }}
                        className="flex-1 py-3 glass-button rounded-xl font-medium flex items-center justify-center gap-2 text-sm text-gray-300 hover:text-white"
                    >
                        <Share2 size={16} />
                        Share
                    </button>
                     <div className="flex-1 py-3 glass-button rounded-xl font-medium flex items-center justify-center gap-3 text-sm px-4 text-gray-300 overflow-hidden group">
                        <Volume2 size={16} />
                        <div className="w-24 h-1 bg-white/20 rounded-full relative overflow-hidden">
                             <input 
                                type="range"
                                min={0}
                                max={1}
                                step={0.1}
                                value={volume}
                                onChange={(e) => setVolume(parseFloat(e.target.value))}
                                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
                            />
                            <div className="h-full bg-white rounded-full transition-all" style={{ width: `${volume * 100}%` }} />
                        </div>
                    </div>
                 </div>
            </div>

        </div>
      </div>
    </div>
  );
}

export default function PlayerPage() {
    return (
        <Suspense fallback={<div className="flex min-h-screen items-center justify-center bg-black text-white">Loading...</div>}>
            <PlayerContent />
        </Suspense>
    )
}
