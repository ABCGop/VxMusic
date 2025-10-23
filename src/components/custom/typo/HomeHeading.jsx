"use client";

import Link from "next/link";
import React, { useEffect, useState } from "react";

async function fetcher() {
  const res = await fetch(
    "https://api.github.com/repos/ABCGop/Music/releases"
  );
  if (!res.ok) {
    throw new Error(res.statusText);
  }
  return res.json();
}

const HomeHeading = () => {
    const [version, setVersion]= useState("N/A");
    useEffect(() => {
        fetcher().then((data) => {
            setVersion(data[0].tag_name);
        });
    }, [version]);

  return (
    <div>
      <h1 className="scroll-m-20 text-4xl font-bold tracking-tight lg:text-5xl pb-8 bg-clip-text text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
        VxMusic - Free Open-Source Music Streaming App
      </h1>
      <h2 className="scroll-m-20 text-xl font-semibold tracking-tight blink">
        Stream unlimited music, videos, podcasts, and radio for free. The perfect alternative to Spotify and SimpMusic.
      </h2>
      <Link 
        href="/download"
        className="inline-block mt-6 px-8 py-3 rounded-lg bg-gradient-to-r from-gradientstart to-gradientend text-white font-semibold hover:opacity-90 transition-opacity">
        Download Now
      </Link>
      <p className="py-4 text-sm text-gray-500/80">
        Support Android Devices and Android Auto only
      </p>
    </div>
  );
};

export default HomeHeading;
