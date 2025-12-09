'use client';

import { Maximize2, Minimize2, MoreHorizontal, Pause, Play, Repeat, Shuffle, SkipBack, SkipForward, Volume2 } from 'lucide-react';
import { useRef, useState } from 'react';
import ReactPlayer from 'react-player/youtube';
import { usePlayer } from '../context/PlayerContext';

export default function BottomPlayer() {
    const { state, togglePlay, setVolume, playNext, playPrev, toggleShuffle, toggleRepeat } = usePlayer();
    const { activeSong, playing, volume, shuffle, repeatMode } = state;
    const [progress, setProgress] = useState(0);
    const [duration, setDuration] = useState(0);
    const [isFullScreen, setIsFullScreen] = useState(false);
    const playerRef = useRef<ReactPlayer>(null);

    if (!activeSong) return null;

    const handleEnded = () => {
        if (repeatMode === 'ONE') {
            playerRef.current?.seekTo(0);
            playerRef.current?.getInternalPlayer()?.playVideo();
        } else {
            playNext(true);
        }
    };

    /* Full Screen Overlay */
    /* Full Screen Overlay */
    /* Full Screen Overlay */
    return (
        <>
            {/* React Player - Always mounted, never unmounts */}
            {/* React Player - Persistence is key, so we keep it mounted. 
            When full screen, we position it to cover the artwork. 
            When minimized, we hide it visually but keep audio running. */}
            <div className={`fixed z-[70] transition-all duration-500 ease-in-out
            ${isFullScreen
                    ? 'inset-x-0 top-32 mx-auto w-full max-w-lg aspect-square opacity-100 pointer-events-auto'
                    : 'w-1 h-1 opacity-0 pointer-events-none -top-10 left-0'}
            `}>
                <div className={`w-full h-full rounded-2xl overflow-hidden shadow-2xl ring-1 ring-white/10 ${!isFullScreen && 'hidden'}`}>
                    <ReactPlayer
                        ref={playerRef}
                        url={`https://www.youtube.com/watch?v=${activeSong.id}`}
                        playing={playing}
                        volume={volume}
                        width="100%"
                        height="100%"
                        onProgress={(p) => setProgress(p.played)}
                        onDuration={setDuration}
                        onEnded={handleEnded}
                        config={{
                            playerVars: {
                                autoplay: 1,
                                controls: 0,
                                modestbranding: 1,
                                rel: 0
                            }
                        }}
                    />
                </div>

                {/* Fallback hidden player for audio-only mode when minimized (if above is hidden) */}
                {/* Actually, ReactPlayer needs to stay mounted. If we hide it with display:none it might stop buffering in some browsers, 
               but opacity:0 is safer. 
           */}
                {!isFullScreen && (
                    <ReactPlayer
                        url={`https://www.youtube.com/watch?v=${activeSong.id}`}
                        playing={playing}
                        volume={volume}
                        width="0"
                        height="0"
                        onProgress={(p) => setProgress(p.played)}
                        onDuration={setDuration}
                        onEnded={handleEnded}
                    />
                )}
            </div>

            {isFullScreen && (
                <div className="fixed inset-0 bg-black/95 z-[60] flex flex-col p-6 backdrop-blur-3xl animate-in fade-in slide-in-from-bottom-10 duration-300">
                    {/* Background Blur */}
                    <div className="absolute inset-0 z-[-1] opacity-30 pointer-events-none">
                        <img src={activeSong.thumbnail} alt="" className="w-full h-full object-cover blur-3xl scale-125" />
                    </div>

                    {/* Header */}
                    <div className="w-full flex justify-between items-center mb-4 shrink-0">
                        <button onClick={() => setIsFullScreen(false)} className="text-white/70 hover:text-white p-2">
                            <Minimize2 size={24} />
                        </button>
                        <div className="flex flex-col items-center">
                            <span className="text-[10px] md:text-xs font-bold tracking-[0.2em] text-white/50 uppercase">Now Playing</span>
                            <span className="text-[10px] md:text-xs font-bold tracking-widest text-white/30 hidden md:block">VX MUSIC PLAYER</span>
                        </div>
                        <button className="text-white/70 hover:text-white p-2">
                            <MoreHorizontal size={24} />
                        </button>
                    </div>

                    {/* Main Content Container - Fits viewport */}
                    <div className="flex-1 flex flex-col items-center justify-between w-full max-w-lg mx-auto py-2 min-h-0">

                        {/* Artwork - Flexible height */}
                        <div className="relative aspect-square w-auto h-auto max-h-[45vh] lg:max-h-[50vh] rounded-2xl overflow-hidden shadow-2xl shadow-black/50 ring-1 ring-white/10 shrink-1">
                            <img src={activeSong.thumbnail} alt={activeSong.title} className="w-full h-full object-cover" />
                        </div>

                        {/* Info */}
                        <div className="text-center space-y-1 w-full px-4 shrink-0 my-4">
                            <h1 className="text-xl md:text-3xl font-bold text-white truncate px-2">{activeSong.title}</h1>
                            <p className="text-base md:text-lg text-gray-400 truncate">{activeSong.artist}</p>
                        </div>

                        {/* Progress & Controls Container */}
                        <div className="w-full flex flex-col gap-4 shrink-0 pb-4">
                            {/* Progress */}
                            <div className="w-full space-y-2">
                                <div className="h-1.5 bg-white/10 rounded-full w-full relative group cursor-pointer">
                                    <input
                                        type="range"
                                        min={0}
                                        max={0.999999}
                                        step="any"
                                        value={progress}
                                        onChange={(e) => {
                                            const newPlayed = parseFloat(e.target.value);
                                            setProgress(newPlayed);
                                            playerRef.current?.seekTo(newPlayed);
                                        }}
                                        className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
                                    />
                                    <div
                                        className="absolute h-full bg-white rounded-full group-hover:bg-green-400 transition-colors pointer-events-none"
                                        style={{ width: `${progress * 100}%` }}
                                    />
                                </div>
                                <div className="flex justify-between text-xs text-gray-400 font-mono font-medium">
                                    <span>{formatTime(progress * duration)}</span>
                                    <span>{formatTime(duration)}</span>
                                </div>
                            </div>

                            {/* Controls */}
                            <div className="flex items-center justify-between w-full max-w-xs md:max-w-md mx-auto">
                                <button
                                    onClick={toggleShuffle}
                                    className={`transition p-2 ${shuffle ? 'text-green-500' : 'text-gray-400 hover:text-white'}`}
                                >
                                    <Shuffle size={20} className="md:w-6 md:h-6" />
                                </button>

                                <button onClick={playPrev} className="text-white hover:scale-110 transition p-2"><SkipBack size={28} className="md:w-10 md:h-10" fill="currentColor" /></button>

                                <button
                                    onClick={togglePlay}
                                    className="w-16 h-16 md:w-20 md:h-20 bg-white rounded-full flex items-center justify-center text-black hover:scale-105 transition shadow-lg shadow-white/20"
                                >
                                    {playing ? <Pause size={32} className="md:w-10 md:h-10" fill="black" /> : <Play size={32} className="md:w-10 md:h-10 ml-1" fill="black" />}
                                </button>

                                <button onClick={() => playNext()} className="text-white hover:scale-110 transition p-2"><SkipForward size={28} className="md:w-10 md:h-10" fill="currentColor" /></button>

                                <button
                                    onClick={toggleRepeat}
                                    className={`transition p-2 ${repeatMode !== 'OFF' ? 'text-green-500' : 'text-gray-400 hover:text-white'} relative`}
                                >
                                    <Repeat size={20} className="md:w-6 md:h-6" />
                                    {repeatMode === 'ONE' && (
                                        <span className="absolute top-1 right-1 text-[8px] font-bold bg-black rounded-full w-3.5 h-3.5 flex items-center justify-center border border-green-500">1</span>
                                    )}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Floating Glass Island Player */}
            <div className="fixed bottom-6 left-4 right-4 md:left-1/2 md:-translate-x-1/2 md:w-full md:max-w-3xl z-40 transition-all duration-500 ease-in-out">
                <div className="glass-panel rounded-[2rem] p-3 md:p-4 flex items-center justify-between shadow-2xl shadow-black/50 ring-1 ring-white/10 bg-black/40 backdrop-blur-2xl">

                    {/* Left: Info */}
                    <div
                        className="flex items-center gap-4 cursor-pointer hover:opacity-80 transition group min-w-0 flex-1"
                        onClick={() => setIsFullScreen(true)}
                    >
                        <div className="w-12 h-12 md:w-14 md:h-14 bg-gray-800 rounded-full md:rounded-2xl overflow-hidden relative shrink-0 shadow-lg animate-[spin_10s_linear_infinite] md:animate-none group-hover:scale-105 transition">
                            <img src={activeSong.thumbnail} alt={activeSong.title} className="object-cover w-full h-full" />
                            <div className="absolute inset-0 bg-black/40 hidden md:flex items-center justify-center opacity-0 group-hover:opacity-100 transition">
                                <Maximize2 size={16} className="text-white" />
                            </div>
                        </div>
                        <div className="min-w-0 pr-4">
                            <h4 className="text-sm md:text-base font-bold truncate text-white group-hover:text-purple-400 transition">{activeSong.title}</h4>
                            <p className="text-xs text-gray-400 truncate">{activeSong.artist}</p>
                        </div>
                    </div>

                    {/* Center: Controls */}
                    <div className="flex items-center gap-2 md:gap-6 shrink-0">
                        <button
                            onClick={(e) => { e.stopPropagation(); toggleShuffle(); }}
                            className={`transition hidden md:block p-2 hover:bg-white/5 rounded-full ${shuffle ? 'text-green-400' : 'text-gray-400 hover:text-white'}`}
                        >
                            <Shuffle size={18} />
                        </button>

                        <button onClick={(e) => { e.stopPropagation(); playPrev(); }} className="text-gray-300 hover:text-white transition p-2 hover:bg-white/5 rounded-full hidden md:block"><SkipBack size={22} fill="currentColor" /></button>

                        <button
                            onClick={(e) => { e.stopPropagation(); togglePlay(); }}
                            className="w-10 h-10 md:w-12 md:h-12 bg-white text-black rounded-full flex items-center justify-center hover:scale-105 transition shadow-lg shadow-white/20 active:scale-95"
                        >
                            {playing ? <Pause size={20} fill="black" /> : <Play size={20} fill="black" className="ml-1" />}
                        </button>

                        <button onClick={(e) => { e.stopPropagation(); playNext(); }} className="text-gray-300 hover:text-white transition p-2 hover:bg-white/5 rounded-full"><SkipForward size={22} fill="currentColor" /></button>

                        <button
                            onClick={(e) => { e.stopPropagation(); toggleRepeat(); }}
                            className={`transition hidden md:block p-2 hover:bg-white/5 rounded-full ${repeatMode !== 'OFF' ? 'text-green-400' : 'text-gray-400 hover:text-white'} relative`}
                        >
                            <Repeat size={18} />
                            {repeatMode === 'ONE' && (
                                <span className="absolute top-1 right-1 text-[8px] font-bold bg-black rounded-full w-3 h-3 flex items-center justify-center border border-green-400">1</span>
                            )}
                        </button>
                    </div>

                    {/* Right: Volume / Progress */}
                    <div className="hidden md:flex items-center gap-4 flex-1 justify-end pl-4">
                        {/* Mini Progress */}
                        <div className="w-24 flex flex-col gap-1 group">
                            <div className="h-1 bg-white/10 rounded-full overflow-hidden relative">
                                <div className="absolute inset-0 bg-gradient-to-r from-blue-500 to-purple-500 w-full transform origin-left transition-transform duration-300"
                                    style={{ transform: `scaleX(${progress})` }}
                                />
                            </div>
                            <div className="flex justify-between text-[10px] text-gray-500 font-mono">
                                <span>{formatTime(progress * duration)}</span>
                            </div>
                        </div>

                        <div className="flex items-center gap-2">
                            <Volume2 size={18} className="text-gray-400" />
                            <div className="w-16 h-1 bg-white/10 rounded-full relative overflow-hidden group">
                                <div className="absolute inset-0 bg-white w-full transform origin-left" style={{ transform: `scaleX(${volume})` }} />
                                <input
                                    type="range" min={0} max={1} step={0.05} value={volume}
                                    onChange={(e) => setVolume(parseFloat(e.target.value))}
                                    className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}

const formatTime = (seconds: number) => {
    if (!seconds) return "0:00";
    const min = Math.floor(seconds / 60);
    const sec = Math.floor(seconds % 60);
    return `${min}:${sec < 10 ? '0' : ''}${sec}`;
}
