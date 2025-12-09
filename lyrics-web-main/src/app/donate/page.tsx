import DonateSection from '@/components/DonateSection';
import React from 'react';
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Support Development",
  description: "Support SimpMusic Lyrics development. Help us maintain this free lyrics database and improve the service for everyone.",
  openGraph: {
    title: "Support Development | SimpMusic Lyrics",
    description: "Support SimpMusic Lyrics development. Help us maintain this free lyrics database and improve the service for everyone.",
    url: "https://lyrics.simpmusic.org/donate",
  },
  twitter: {
    title: "Support Development | SimpMusic Lyrics",
    description: "Support SimpMusic Lyrics development. Help us maintain this free lyrics database and improve the service for everyone.",
  },
  alternates: {
    canonical: "https://lyrics.simpmusic.org/donate",
  },
};

const DonatePage: React.FC = () => {
  return (
    <DonateSection />
  );
};

export default DonatePage;