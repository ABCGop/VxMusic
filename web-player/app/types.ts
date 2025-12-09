export interface Song {
  id: string;
  title: string;
  artist: string;
  thumbnail: string;
  duration?: number;
}

export type RepeatMode = 'OFF' | 'ALL' | 'ONE';

export interface PlayerState {
  playing: boolean;
  activeSong: Song | null;
  queue: Song[];
  currentIndex: number;
  volume: number;
  shuffle: boolean;
  repeatMode: RepeatMode;
}
