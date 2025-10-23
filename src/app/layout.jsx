import Footer from "@/components/custom/footer/Footer";
import Header from "@/components/custom/header/Header";
import { cn } from "@/lib/utils";
import localFont from 'next/font/local';
import "./globals.css";
import Providers from "./providers";

// Enhanced font configuration with multiple weights and styles
const sfpDisplay = localFont({
  src: [
    {
      path: '../../public/fonts/sfprodisplay/sf-regular.otf',
      weight: '400',
      style: 'normal'
    },
    {
      path: '../../public/fonts/sfprodisplay/sf-medium.otf',
      weight: '500',
      style: 'normal'
    },
    {
      path: '../../public/fonts/sfprodisplay/sf-bold.otf',
      weight: '700',
      style: 'normal'
    }
  ],
  variable: '--font-sfpdisplay',
  display: 'swap',
  preload: true,
  fallback: ['Inter', 'system-ui', 'sans-serif']
})

// Viewport configuration (separate from metadata in Next.js 14+)
export const viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 5
}

// Enhanced metadata for better SEO
export const metadata = {
  title: {
    default: "VxMusic - Free Music Streaming App",
    template: "%s | VxMusic"
  },
  description: "VxMusic is a free open-source music streaming app. A perfect alternative to Spotify and other music streaming services with a clean, user-friendly interface.",
  keywords: ["vxmusic", "simpmusic", "music", "spotify", "free music", "music streaming", "open source music app", "free spotify alternative"],
  authors: [{ name: "VxMusic Team" }],
  creator: "VxMusic",
  publisher: "VxMusic",
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      'max-video-preview': -1,
      'max-image-preview': 'large',
      'max-snippet': -1,
    },
  },
  openGraph: {
    type: "website",
    locale: "en_US",
    url: "https://vxmusic.in",
    siteName: "VxMusic",
    title: "VxMusic - Free Music Streaming App",
    description: "Stream unlimited music for free with VxMusic. The best open-source alternative to Spotify and other music streaming services.",
    images: [
      {
        url: "/logo.webp",
        width: 1200,
        height: 630,
        alt: "VxMusic - Free Music Streaming App"
      }
    ]
  },
  twitter: {
    card: "summary_large_image",
    title: "VxMusic - Free Music Streaming App",
    description: "Stream unlimited music for free with VxMusic. The best open-source alternative to Spotify and other music streaming services.",
    creator: "@VxMusic",
    images: ["/logo.webp"]
  },
  alternates: {
    canonical: "https://vxmusic.in"
  },
  verification: {
    google: "your-google-verification-code",
    other: {
      "msvalidate.01": "your-bing-verification-code"
    }
  }
};

const RootLayout = ({ children }) => {
  return (
    <html lang="en" className="dark scroll-smooth" suppressHydrationWarning>
      <head>
        {/* Performance Optimizations */}
        <link rel="preconnect" href="https://api.github.com" crossOrigin="anonymous" />
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link rel="dns-prefetch" href="https://www.youtube.com" />
        
        {/* App Icons and Manifest */}
        <link rel="icon" href="/favicon.ico" sizes="any" />
        <link rel="icon" href="/logo.webp" type="image/webp" />
        <link rel="apple-touch-icon" href="/logo.webp" />
        <link rel="manifest" href="/manifest.json" />
        
        {/* Theme and Visual Configuration */}
        <meta name="theme-color" content="#8b5cf6" media="(prefers-color-scheme: light)" />
        <meta name="theme-color" content="#a855f7" media="(prefers-color-scheme: dark)" />
        <meta name="color-scheme" content="dark light" />
        <meta name="msapplication-TileColor" content="#8b5cf6" />
        
        {/* Google AdSense */}
        <meta name="google-adsense-account" content="ca-pub-4673648297883595" />
        
        {/* Viewport and Mobile Optimization */}
        <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
        <meta name="mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
        
        {/* Performance Hints */}
        <link rel="preload" href="/fonts/sfprodisplay/sf-regular.otf" as="font" type="font/otf" crossOrigin="anonymous" />
        <link rel="preload" href="/fonts/sfprodisplay/sf-medium.otf" as="font" type="font/otf" crossOrigin="anonymous" />
      </head>
      <body
        className={cn(
          "min-h-screen bg-background text-foreground antialiased",
          "flex flex-col overflow-x-hidden",
          "selection:bg-primary/20 selection:text-primary-foreground",
          "scroll-smooth",
          sfpDisplay.variable,
        )}
        suppressHydrationWarning
      >
        {/* Background Elements */}
        <div className="fixed inset-0 bg-grid opacity-[0.02] pointer-events-none" />
        <div className="fixed inset-0 bg-gradient-to-br from-primary/5 via-transparent to-accent/5 pointer-events-none" />
        
        <Providers>
          {/* Skip Navigation for Accessibility */}
          <a 
            href="#main-content" 
            className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 bg-primary text-primary-foreground px-4 py-2 rounded-lg font-medium transition-all duration-200 hover:bg-primary-hover focus-ring"
          >
            Skip to main content
          </a>
          
          {/* Header with Enhanced Styling */}
          <header className="sticky top-0 z-40 w-full border-b border-border/40 backdrop-blur-xl bg-background/80 supports-[backdrop-filter]:bg-background/60">
            <div className="container mx-auto">
              <Header />
            </div>
          </header>
          
          {/* Main Content Container */}
          <main 
            id="main-content" 
            className="flex-1 relative focus:outline-none"
            tabIndex={-1}
          >
            {/* Content Wrapper with Improved Spacing */}
            <div className="min-h-[calc(100vh-4rem)]">
              {children}
            </div>
          </main>
          
          {/* Enhanced Footer */}
          <footer className="mt-auto border-t border-border/40 bg-card/50 backdrop-blur-sm">
            <div className="container mx-auto">
              <Footer />
            </div>
          </footer>
          
          {/* Enhanced Structured Data */}
          <script
            type="application/ld+json"
            dangerouslySetInnerHTML={{
              __html: JSON.stringify({
                "@context": "https://schema.org",
                "@graph": [
                  {
                    "@type": "WebApplication",
                    "@id": "https://vxmusic.in/#webapp",
                    "name": "VxMusic",
                    "alternateName": ["VX Music", "VxMusic App"],
                    "url": "https://vxmusic.in",
                    "description": "Free open-source music streaming app. Stream unlimited music, videos, podcasts, and radio for free with a clean interface.",
                    "applicationCategory": "MusicApplication",
                    "operatingSystem": ["Android", "Android Auto"],
                    "downloadUrl": "https://vxmusic.in/download",
                    "softwareVersion": "latest",
                    "offers": {
                      "@type": "Offer",
                      "price": "0",
                      "priceCurrency": "USD",
                      "availability": "https://schema.org/InStock"
                    },
                    "publisher": {
                      "@type": "Organization",
                      "@id": "https://vxmusic.in/#organization",
                      "name": "VxMusic",
                      "url": "https://vxmusic.in",
                      "logo": {
                        "@type": "ImageObject",
                        "url": "https://vxmusic.in/logo.webp",
                        "width": 512,
                        "height": 512
                      }
                    },
                    "sameAs": [
                      "https://github.com/ABCGop/Music"
                    ],
                    "keywords": "music streaming, free music, open source music, spotify alternative, android music app",
                    "screenshot": "https://vxmusic.in/mockup/screenshot.png",
                    "featureList": [
                      "Free music streaming",
                      "Music streaming and playlists",
                      "Android Auto support",
                      "Offline listening",
                      "High quality audio"
                    ]
                  },
                  {
                    "@type": "WebSite",
                    "@id": "https://vxmusic.in/#website",
                    "url": "https://vxmusic.in",
                    "name": "VxMusic - Free Music Streaming App",
                    "description": "Download VxMusic and enjoy unlimited free music streaming. Perfect open-source alternative to Spotify.",
                    "publisher": {
                      "@id": "https://vxmusic.in/#organization"
                    },
                    "potentialAction": {
                      "@type": "SearchAction",
                      "target": "https://vxmusic.in/search?q={search_term_string}",
                      "query-input": "required name=search_term_string"
                    }
                  }
                ]
              })
            }}
          />
          
          {/* Performance Monitoring Script Placeholder */}
          {process.env.NODE_ENV === 'production' && (
            <script
              dangerouslySetInnerHTML={{
                __html: `
                  // Web Vitals tracking
                  if ('performance' in window) {
                    const observer = new PerformanceObserver((list) => {
                      for (const entry of list.getEntries()) {
                        if (entry.entryType === 'largest-contentful-paint') {
                          console.log('LCP:', entry.startTime);
                        }
                      }
                    });
                    observer.observe({entryTypes: ['largest-contentful-paint']});
                  }
                `
              }}
            />
          )}
        </Providers>
      </body>
    </html>
  );
};
export default RootLayout;
