'use client';

import { motion } from 'framer-motion';
import { ArrowRight, Globe, Layers, Music, PlayCircle, Shield, Smartphone, Zap } from 'lucide-react';
import Link from 'next/link';

import ThreeHero from './components/ThreeHero';

export default function Home() {
  return (
    <div className="flex-1 flex flex-col items-center justify-start relative min-h-screen overflow-x-hidden">

      {/* --- Initial Hero --- */}
      <div className="flex flex-col items-center justify-center min-h-screen w-full relative z-10 px-4">

        <ThreeHero />

        {/* Ambient Background */}
        <div className="aurora-bg" />

        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          className="text-center max-w-5xl mx-auto"
        >
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full border border-indigo-500/30 bg-indigo-500/10 text-indigo-300 text-sm font-medium mb-8 backdrop-blur-md shadow-[0_0_20px_rgba(99,102,241,0.3)]">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-indigo-500"></span>
            </span>
            VxMusic V4 is Live
          </div>

          <h1 className="text-4xl md:text-6xl lg:text-8xl font-bold tracking-tighter mb-8 title-glow leading-none px-2">
            The <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 to-purple-400">Atlas</span> of Audio.
          </h1>

          <p className="text-xl md:text-2xl text-gray-400 max-w-3xl mx-auto mb-12 leading-relaxed">
            Navigate a universe of sound with precision. <br />
            High-fidelity streaming. Zero latency. Infinite discovery.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-6">
            <Link href="/download" className="group btn-shine flex items-center gap-3 px-8 py-4 bg-white text-black rounded-full font-bold text-lg hover:scale-105 transition duration-300 shadow-[0_0_40px_rgba(255,255,255,0.3)]">
              Get the App
              <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </Link>

            <a href="https://web.vxmusic.in" className="flex items-center gap-3 px-8 py-4 rounded-full border border-white/10 hover:bg-white/5 transition text-lg font-medium backdrop-blur-sm hover:border-white/20">
              <PlayCircle className="w-5 h-5 text-indigo-400" />
              Web Player
            </a>
          </div>
        </motion.div>
      </div>

      {/* --- Feature Grid --- */}
      <div className="w-full bg-[#030308] relative z-20 py-32 border-t border-white/10">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-20">
            <h2 className="text-4xl md:text-5xl font-bold mb-6">Engineered for Immersion.</h2>
            <p className="text-xl text-gray-400">Everything you need to get lost in the music.</p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <FeatureCard
              icon={<Zap className="text-yellow-400" />}
              title="Instant Playback"
              desc="Powered by a distributed edge network. Audio starts before your finger leaves the screen."
            />
            <FeatureCard
              icon={<Shield className="text-green-400" />}
              title="Privacy First"
              desc="No tracking pixels. No data selling. Your listening history is encrypted and yours alone."
            />
            <FeatureCard
              icon={<Globe className="text-blue-400" />}
              title="YouTube Sync"
              desc="Import your playlists directly from YouTube Music. Your entire library, instantly available."
            />
            <FeatureCard
              icon={<Layers className="text-purple-400" />}
              title="High-Res Audio"
              desc="Support for FLAC and ALAC formats. Hear every detail exactly as the artist intended."
            />
            <FeatureCard
              icon={<Music className="text-pink-400" />}
              title="Smart Lyrics"
              desc="Time-synced lyrics that float in 3D space. Sing along or just appreciate the poetry."
            />
            <FeatureCard
              icon={<Smartphone className="text-indigo-400" />}
              title="Offline Mode"
              desc="Download entire playlists in seconds. Your music, available even in the void of space."
            />
          </div>
        </div>
      </div>

      {/* --- Stats / Trust --- */}
      <div className="w-full py-24 border-y border-white/5 bg-white/[0.02] relative z-20">
        <div className="max-w-6xl mx-auto px-6 text-center">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-12">
            <Stat number="100M+" label="Songs Indexed" />
            <Stat number="0.2s" label="Latency" />
            <Stat number="99.9%" label="Uptime" />
            <Stat number="1.9k+" label="Downloads" />
          </div>
        </div>
      </div>


      {/* --- 3D Process Section --- */}
      <div className="w-full py-32 relative z-20">
        <div className="max-w-7xl mx-auto px-6">
          <div className="grid md:grid-cols-2 gap-16 items-center">
            <motion.div
              initial={{ opacity: 0, x: -50 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
            >
              <h2 className="text-4xl md:text-5xl font-bold mb-8">Built for the <br /> <span className="text-indigo-400">Audio Paranoid.</span></h2>
              <div className="space-y-8">
                <ProcessStep
                  number="01"
                  title="Encryption at Rest"
                  desc="Your library database is encrypted with AES-256. Even if someone steals your phone, they can't see what you're listening to."
                />
                <ProcessStep
                  number="02"
                  title="P2P Delivery"
                  desc="Content is fetched from a decentralized swarm of nodes. No central server knows your full playback history."
                />
                <ProcessStep
                  number="03"
                  title="Local Processing"
                  desc="EQ, spatial rendering, and analytics happen on your device's NPU. Zero data leaves your phone."
                />
              </div>
            </motion.div>
            <motion.div
              initial={{ opacity: 0, scale: 0.8 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              className="relative h-[600px] bg-indigo-500/5 rounded-[3rem] border border-indigo-500/20 backdrop-blur-3xl overflow-hidden flex items-center justify-center"
            >
              {/* Abstract decorative elements */}
              <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-indigo-500/30 rounded-full blur-[100px] animate-pulse" />
              <div className="relative z-10 text-center">
                <Shield className="w-32 h-32 text-indigo-400 mx-auto mb-6 opacity-80" />
                <div className="text-2xl font-bold tracking-widest uppercase">Secure Core</div>
              </div>
            </motion.div>
          </div>
        </div>
      </div>

      {/* --- Testimonials --- */}
      <div className="w-full py-24 bg-gradient-to-b from-transparent to-[#05050A] relative z-20">
        <div className="max-w-6xl mx-auto px-6 text-center">
          <h2 className="text-4xl font-bold mb-16">The Community Speaks</h2>
          <div className="grid md:grid-cols-3 gap-8">
            <TestimonialCard
              quote="Finally, a music player that respects my privacy and doesn't try to sell me podcasts."
              author="Alex M."
              role="Security Researcher"
            />
            <TestimonialCard
              quote="The audio engine is noticeably cleaner than Spotify. The FLAC support is a game changer."
              author="Sarah K."
              role="Audio Engineer"
            />
            <TestimonialCard
              quote="Open source, beautiful UI, and zero ads. This is what the internet used to be."
              author="David R."
              role="Frontend Dev"
            />
          </div>
        </div>
      </div>

      {/* --- FAQ Section --- */}
      <div className="w-full py-32 relative z-20 max-w-4xl mx-auto px-6">
        <div className="text-center mb-20">
          <h2 className="text-4xl font-bold mb-6">Frequency Asked Questions</h2>
          <p className="text-gray-400">Common queries about our protocol.</p>
        </div>

        <div className="space-y-6">
          <FaqItem
            question="Is VxMusic really free?"
            answer="Yes. VxMusic is open-source software. You can download the APK or use the web player for free. We do not have a premium subscription model."
          />
          <FaqItem
            question="How does the 'YouTube Sync' work?"
            answer="We use a read-only token to fetch your playlists from YouTube Music. We then match those songs against our high-fidelity audio database. We do not store your Google password."
          />
          <FaqItem
            question="Is it legal?"
            answer="VxMusic is a frontend client. We do not host copyrighted MP3 files on our servers. All audio is streamed via legitimate third-party APIs (like YouTube) or P2P networks where allowed by law."
          />
          <FaqItem
            question="Why 'God Mode'?"
            answer="It's a design philosophy. We give you total control over the UI, the audio engine, and the data. No locked features. No hidden toggles."
          />
          <FaqItem
            question="How do I remove ads?"
            answer="VxMusic does not inject audio ads into your stream. The only ads you might see are minimally invasive visual banners on the website to support server costs."
          />
        </div>
      </div>

    </div>
  );
}

function FeatureCard({ icon, title, desc }: { icon: any, title: string, desc: string }) {
  return (
    <div className="p-8 rounded-3xl bg-white/5 border border-white/5 hover:border-white/20 hover:bg-white/10 transition duration-500 group">
      <div className="w-12 h-12 rounded-xl bg-white/5 flex items-center justify-center mb-6 group-hover:scale-110 transition">
        {icon}
      </div>
      <h3 className="text-2xl font-bold mb-3">{title}</h3>
      <p className="text-gray-400 leading-relaxed">
        {desc}
      </p>
    </div>
  )
}

function Stat({ number, label }: { number: string, label: string }) {
  return (
    <div>
      <div className="text-5xl md:text-6xl font-bold text-white mb-2">{number}</div>
      <div className="text-indigo-400 font-mono text-sm uppercase tracking-widest">{label}</div>
    </div>
  )
}

function FaqItem({ question, answer }: { question: string, answer: string }) {
  return (
    <div className="p-8 rounded-3xl bg-white/5 border border-white/5 hover:border-white/10 transition duration-300">
      <h3 className="text-xl font-bold text-white mb-3">{question}</h3>
      <p className="text-gray-400 leading-relaxed text-lg">
        {answer}
      </p>
    </div>
  )
}

function TestimonialCard({ quote, author, role }: { quote: string, author: string, role: string }) {
  return (
    <div className="p-8 rounded-3xl bg-white/5 border border-white/5 hover:border-white/10 text-left transition hover:-translate-y-1">
      <div className="mb-6 text-indigo-400">
        <Music className="w-8 h-8" />
      </div>
      <p className="text-lg text-gray-300 mb-6 italic">"{quote}"</p>
      <div>
        <div className="font-bold text-white">{author}</div>
        <div className="text-sm text-gray-500">{role}</div>
      </div>
    </div>
  )
}

function ProcessStep({ number, title, desc }: { number: string, title: string, desc: string }) {
  return (
    <div className="flex gap-6 group">
      <div className="text-4xl font-bold text-indigo-500/20 group-hover:text-indigo-400 transition-colors font-mono">{number}</div>
      <div>
        <h3 className="text-xl font-bold mb-2">{title}</h3>
        <p className="text-gray-400 leading-relaxed">{desc}</p>
      </div>
    </div>
  )
}
