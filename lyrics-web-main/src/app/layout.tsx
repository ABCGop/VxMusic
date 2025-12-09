import Footer from '@/components/Footer';
import Globe from '@/components/Globe';
import Navbar from '@/components/Navbar';
import SmoothScroll from '@/components/SmoothScroll';
import SplashScreen from '@/components/SplashScreen';
import './globals.css';

// ...

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark" suppressHydrationWarning>
      <body className={`${inter.variable} antialiased font-sans`} suppressHydrationWarning>
        <SmoothScroll>
          <SplashScreen />
          <div className="grid-overlay" />
          <Globe />
          <Navbar />
          <main className="min-h-screen flex flex-col pt-24">
            {children}
            <Toaster richColors />
          </main>
          <Footer />
        </SmoothScroll>
      </body>
    </html>
  );
}
