import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import Footer from './components/Footer';
import Globe from './components/Globe';
import Navbar from './components/Navbar';
import SmoothScroll from './components/SmoothScroll';
import SplashScreen from './components/SplashScreen';
import './globals.css';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'VxMusic Atlas',
  description: 'The world of music in your pocket.',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className={inter.className}>
        <SplashScreen />
        <SmoothScroll>
          <div className="grid-overlay" />
          <Globe />
          <Navbar />
          <main className="min-h-screen flex flex-col">
            {children}
            <Footer />
          </main>
        </SmoothScroll>
      </body>
    </html>
  );
}
