"use client";

import { ArrowDownTrayIcon, PlayIcon, StarIcon } from "@heroicons/react/24/solid";
import Link from "next/link";
import React, { useEffect, useState } from "react";

const ModernHero = () => {
  const [version, setVersion] = useState("N/A");
  const [isLoaded, setIsLoaded] = useState(false);

  useEffect(() => {
    // Fetch version info
    const fetchVersion = async () => {
      try {
        const res = await fetch("https://api.github.com/repos/ABCGop/Music/releases");
        if (res.ok) {
          const data = await res.json();
          setVersion(data[0]?.tag_name || "N/A");
        }
      } catch (error) {
        console.error("Failed to fetch version:", error);
      }
    };
    
    fetchVersion();
    setIsLoaded(true);
  }, []);

  const stats = [
    { number: "100M+", label: "Songs Available" },
    { number: "100%", label: "Free Forever" },
    { number: "0", label: "Ads or Limits" },
  ];

  return (
    <section className="relative min-h-screen flex items-center justify-center overflow-hidden bg-gradient-to-br from-background via-background/95 to-muted/20 pt-10 md:pt-12">
      {/* Animated Background Elements */}
      <div className="absolute inset-0">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-gradient-to-r from-primary/20 to-accent/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-gradient-to-r from-accent/20 to-primary/20 rounded-full blur-3xl animate-pulse delay-1000" />
        <div className="absolute top-3/4 left-1/2 w-64 h-64 bg-gradient-to-r from-purple-500/10 to-pink-500/10 rounded-full blur-3xl animate-pulse delay-2000" />
      </div>
      
      {/* Grid Pattern */}
      <div className="absolute inset-0 bg-grid opacity-[0.02]" />
      
      <div className={`relative z-10 container mx-auto px-6 text-center transition-all duration-1000 ${isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-10'}`}>
        
        {/* Version Badge */}
        <div className="inline-flex items-center gap-3 bg-card/80 backdrop-blur-md border border-border/50 rounded-full px-6 py-3 mb-8 shadow-lg hover:shadow-xl transition-all duration-300 group">
          <div className="relative">
            <div className="w-3 h-3 bg-gradient-to-r from-green-400 to-green-500 rounded-full animate-pulse" />
            <div className="absolute inset-0 w-3 h-3 bg-gradient-to-r from-green-400 to-green-500 rounded-full animate-ping opacity-75" />
          </div>
          <span className="text-sm font-medium bg-gradient-to-r from-foreground to-muted-foreground bg-clip-text text-transparent">
            Latest Version {version}
          </span>
          <StarIcon className="w-4 h-4 text-yellow-500 group-hover:animate-spin transition-all duration-300" />
        </div>
        
        {/* Main Hero Content */}
        <div className="space-y-8 mb-16">
          {/* Brand Name */}
          <div className="space-y-4">
            <h1 className="text-5xl sm:text-6xl md:text-8xl font-bold tracking-tight">
              <span className="bg-gradient-to-r from-gradientstart via-primary to-gradientend bg-clip-text text-transparent drop-shadow-sm">
                VxMusic
              </span>
            </h1>
            
            {/* Tagline */}
            <p className="text-2xl sm:text-3xl md:text-4xl font-semibold text-foreground/90">
              Free Music Streaming
              <span className="block text-lg sm:text-xl md:text-2xl font-normal text-muted-foreground mt-2">
                Open Source • No Ads • Unlimited
              </span>
            </p>
          </div>
          
          {/* Description */}
          <p className="text-lg sm:text-xl text-muted-foreground max-w-3xl mx-auto leading-relaxed">
            Experience unlimited music streaming with <span className="text-primary font-semibold">VxMusic</span> - 
            the powerful open-source alternative to premium music services. 
            Stream millions of songs, create playlists, and enjoy high-quality audio completely free.
          </p>
        </div>
        
        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-16">
          <Link 
            href="/download"
            className="group relative bg-gradient-to-r from-primary via-primary to-primary-hover text-primary-foreground px-8 py-4 rounded-2xl font-semibold text-lg shadow-xl hover:shadow-2xl transition-all duration-300 hover:-translate-y-1 min-w-[200px]"
          >
            <div className="absolute inset-0 bg-gradient-to-r from-primary-hover to-primary rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
            <div className="relative flex items-center justify-center gap-3">
              <ArrowDownTrayIcon className="w-5 h-5 group-hover:animate-bounce" />
              Download Free
            </div>
          </Link>
          
          <Link 
            href="/about"
            className="group bg-card/50 backdrop-blur-sm border border-border hover:border-primary/50 text-foreground px-8 py-4 rounded-2xl font-semibold text-lg transition-all duration-300 hover:shadow-lg min-w-[200px]"
          >
            <div className="flex items-center justify-center gap-3">
              <PlayIcon className="w-5 h-5 text-accent group-hover:text-primary transition-colors" />
              Learn More
            </div>
          </Link>
        </div>
        
        {/* Stats */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 max-w-2xl mx-auto mb-16">
          {stats.map((stat, index) => (
            <div key={index} className="text-center p-4">
              <div className="text-3xl sm:text-4xl font-bold bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
                {stat.number}
              </div>
              <div className="text-sm text-muted-foreground mt-1">
                {stat.label}
              </div>
            </div>
          ))}
        </div>
        
        {/* Scroll Indicator */}
        <div className="mt-16 animate-bounce">
          <div className="mx-auto w-6 h-10 border-2 border-muted-foreground/30 rounded-full flex justify-center">
            <div className="w-1 h-3 bg-gradient-to-b from-primary to-transparent rounded-full mt-2 animate-pulse" />
          </div>
          <p className="text-xs text-muted-foreground mt-2">Scroll to explore</p>
        </div>
      </div>
    </section>
  );
};

export default ModernHero;