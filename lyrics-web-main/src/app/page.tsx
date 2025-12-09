"use client";

import React from 'react';
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";

import { Input } from "@/components/ui/input";

export default function Home() {
  const router = useRouter();
  const [q, setQ] = React.useState("");

  const canSearch = q.trim().length > 0;

  function submit() {
    if (!canSearch) return;
    router.push(`/search?q=${encodeURIComponent(q.trim())}`);
  }

  function onKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") {
      e.preventDefault();
      submit();
    }
  }

  return (
    <div className="min-h-[calc(100vh-100px)] flex flex-col items-center justify-center px-4 relative z-10">
      <section className="w-full max-w-4xl text-center space-y-10">
        {/* Hero text */}
        <header className="space-y-6">
          <h1 className="text-5xl md:text-7xl font-bold tracking-tighter text-white title-glow leading-tight">
            The <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 to-purple-400">Atlas</span> of Lyrics.
          </h1>
          <p className="text-gray-400 text-lg md:text-xl max-w-2xl mx-auto">
            Synced lyrics, translations in LRC format for your favorite songs. Community-sourced and built for music lovers.
          </p>
        </header>

        {/* Search bar (shadcn/ui Input + Button) */}
        <div className="flex items-center gap-3 p-2 bg-white/5 border border-white/10 rounded-2xl backdrop-blur-xl max-w-2xl mx-auto shadow-2xl">
          <Input
            type="text"
            placeholder="Search lyrics, artists, songs..."
            aria-label="Search lyrics"
            className="h-14 text-lg bg-transparent border-none focus-visible:ring-0 text-white placeholder:text-gray-500"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            onKeyDown={onKeyDown}
          />
          <Button
            className="h-12 px-8 text-base font-semibold bg-white text-black hover:bg-gray-200 rounded-xl transition-all"
            onClick={submit}
            disabled={!canSearch}
            aria-disabled={!canSearch}
          >
            Search
          </Button>
        </div>
      </section>
    </div>
  );
}
