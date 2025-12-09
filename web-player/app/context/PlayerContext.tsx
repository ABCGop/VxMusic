'use client';

import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import { Song, PlayerState, RepeatMode } from '../types';

interface PlayerContextType {
  state: PlayerState;
  playSong: (song: Song, newQueue?: Song[]) => void;
  togglePlay: () => void;
  setVolume: (vol: number) => void;
  playNext: (auto?: boolean) => void;
  playPrev: () => void;
  toggleShuffle: () => void;
  toggleRepeat: () => void;
}

const PlayerContext = createContext<PlayerContextType | undefined>(undefined);

export function PlayerProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<PlayerState>({
    playing: false,
    activeSong: null,
    queue: [],
    currentIndex: -1,
    volume: 0.8,
    shuffle: false,
    repeatMode: 'OFF',
  });

  const playSong = (song: Song, newQueue?: Song[]) => {
    setState(prev => {
        let queue = newQueue ? [...newQueue] : prev.queue;
        // If queue is empty or newQueue not provided, ensure song is in queue
        if (queue.length === 0) queue = [song];
        
        // Find index
        const index = queue.findIndex(s => s.id === song.id);
        
        return {
            ...prev,
            activeSong: song,
            playing: true,
            queue,
            currentIndex: index !== -1 ? index : 0
        };
    });
  };

  const togglePlay = () => {
    setState(prev => ({ ...prev, playing: !prev.playing }));
  };

  const setVolume = (vol: number) => {
    setState(prev => ({ ...prev, volume: vol }));
  };

  const playNext = (auto: boolean = false) => {
      setState(prev => {
          if (prev.queue.length === 0) return prev;

          let nextIndex = -1;
          
          if (prev.shuffle) {
              do {
                nextIndex = Math.floor(Math.random() * prev.queue.length);
              } while (nextIndex === prev.currentIndex && prev.queue.length > 1);
          } else {
              nextIndex = prev.currentIndex + 1;
              if (nextIndex >= prev.queue.length) {
                  // End of Queue
                  if (prev.repeatMode === 'ALL' || !auto) {
                      // If Repeat All OR Manual Click, loop to start
                      nextIndex = 0;
                  } else {
                      // Auto-play end of queue -> Stop
                      return { ...prev, playing: false }; 
                  }
              }
          }

          return {
              ...prev,
              activeSong: prev.queue[nextIndex],
              currentIndex: nextIndex,
              playing: true
          };
      });
  };

  const playPrev = () => {
      setState(prev => {
          if (prev.queue.length === 0) return prev;
          let newIndex = prev.currentIndex - 1;
          if (newIndex < 0) newIndex = prev.queue.length - 1; // Wrap to end
          
          return {
              ...prev,
              activeSong: prev.queue[newIndex],
              currentIndex: newIndex,
              playing: true
          };
      });
  };

  const toggleShuffle = () => {
      setState(prev => ({ ...prev, shuffle: !prev.shuffle }));
  };

  const toggleRepeat = () => {
      setState(prev => {
          const modes: RepeatMode[] = ['OFF', 'ALL', 'ONE'];
          const nextIndex = (modes.indexOf(prev.repeatMode) + 1) % modes.length;
          return { ...prev, repeatMode: modes[nextIndex] };
      });
  };

  return (
    <PlayerContext.Provider
      value={{
        state,
        playSong,
        togglePlay,
        setVolume,
        playNext,
        playPrev,
        toggleShuffle,
        toggleRepeat,
      }}
    >
      {children}
    </PlayerContext.Provider>
  );
}

export function usePlayer() {
  const context = useContext(PlayerContext);
  if (context === undefined) {
    throw new Error('usePlayer must be used within a PlayerProvider');
  }
  return context;
}
