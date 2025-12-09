'use client';

import { motion } from 'framer-motion';
import { GitBranch, Globe, Shield, Zap } from 'lucide-react';

export default function About() {
    return (
        <div className="flex-1 flex flex-col items-center justify-start min-h-screen relative overflow-x-hidden">

            {/* --- Header --- */}
            <div className="container mx-auto px-6 pt-32 pb-20 max-w-5xl text-center">
                <motion.div
                    initial={{ opacity: 0, scale: 0.95 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ duration: 0.6 }}
                >
                    <h1 className="text-5xl md:text-7xl font-bold mb-8 title-glow leading-tight">
                        Redefining the <br /> <span className="text-indigo-500">Soundscape.</span>
                    </h1>
                    <p className="text-xl md:text-2xl text-gray-400 max-w-3xl mx-auto leading-relaxed">
                        VxMusic isn't just a player. It's a manifesto. <br />
                        We believe audio should be free, fast, and owned by the listener.
                    </p>
                </motion.div>
            </div>

            {/* --- Core Values Grid --- */}
            <div className="w-full bg-white/[0.02] border-y border-white/5 py-24">
                <div className="max-w-7xl mx-auto px-6">
                    <div className="grid md:grid-cols-3 gap-12">
                        <ValueCard
                            icon={<Zap className="w-8 h-8 text-yellow-500" />}
                            title="Speed is a Feature"
                            desc="We write native code. We optimize for milliseconds. We believe waiting for a song to load is a failure of engineering."
                        />
                        <ValueCard
                            icon={<Shield className="w-8 h-8 text-green-500" />}
                            title="Radical Privacy"
                            desc="You are not a product. We don't track your location, your contacts, or your habits. What you listen to is your business."
                        />
                        <ValueCard
                            icon={<Globe className="w-8 h-8 text-blue-500" />}
                            title="Universal Access"
                            desc="Music belongs to everyone. We build for low-end devices, slow networks, and open standards."
                        />
                    </div>
                </div>
            </div>



            {/* --- Open Source CTA --- */}
            <div className="w-full bg-gradient-to-b from-[#0a0a0a] to-black py-24 border-t border-white/5">
                <div className="max-w-4xl mx-auto px-6 text-center">
                    <GitBranch className="w-16 h-16 text-white/20 mx-auto mb-8" />
                    <h2 className="text-4xl font-bold mb-6">Built in the Open.</h2>
                    <p className="text-xl text-gray-400 mb-10">
                        VxMusic is 100% open source. Audit our code, contribute a feature, or fork it and build your own.
                    </p>
                    <a href="https://github.com/ABCGop/VxMusic" className="inline-flex items-center gap-3 px-8 py-4 bg-white/10 hover:bg-white/20 border border-white/10 rounded-full font-bold transition">
                        <GithubIcon />
                        View Source Code
                    </a>
                </div>
            </div>

        </div>
    );
}

function ValueCard({ icon, title, desc }: { icon: any, title: string, desc: string }) {
    return (
        <div className="space-y-4">
            <div className="w-16 h-16 rounded-2xl bg-white/5 border border-white/10 flex items-center justify-center mb-6">
                {icon}
            </div>
            <h3 className="text-2xl font-bold">{title}</h3>
            <p className="text-gray-400 leading-relaxed text-lg">{desc}</p>
        </div>
    )
}



function GithubIcon() {
    return (
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36.5-8 4-2.64-3.5-5.36-4.5-8-4-1 0-3 .5-3 5.5.28 1.15.28 2.35 0 3.5A5.403 5.403 0 0 0 4 18c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4" /><path d="M9 18c-4.51 2-5-2-7-2" /></svg>
    )
}
