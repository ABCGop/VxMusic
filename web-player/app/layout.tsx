import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import BottomPlayer from './components/BottomPlayer';
import MobileNav from './components/MobileNav';
import Sidebar from './components/Sidebar';
import { PlayerProvider } from './context/PlayerContext';
import './globals.css';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'VxMusic Web',
  description: 'Listen to music without limits.',
};


// ...

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <PlayerProvider>
          <div className="flex min-h-screen w-full bg-black text-white selection:bg-purple-500/30">
            <Sidebar />
            <main className="flex-1 w-full relative z-0 pb-32 md:pb-0">
              {/* Gradient Header Overlay */}
              <div className="fixed top-0 left-0 right-0 h-32 bg-gradient-to-b from-black/80 to-transparent z-10 pointer-events-none md:hidden" />

              <div className="min-h-full">
                {children}
              </div>
            </main>
            <BottomPlayer />
            <MobileNav />
          </div>
        </PlayerProvider>
      </body>
    </html>
  );
}
