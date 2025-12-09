'use client';

import { AlertCircle, CheckCircle2, Download, Monitor, Play, Smartphone } from 'lucide-react';

export default function DownloadPage() {
    return (
        <div className="flex-1 flex flex-col items-center justify-start min-h-screen pt-32 pb-20 px-6">

            <div className="text-center mb-16 w-full">
                <h1 className="text-5xl md:text-7xl font-bold mb-6 title-glow">Get VxMusic</h1>
                <p className="text-xl text-gray-400 mb-8">Choose your platform and start your journey.</p>
            </div>

            {/* --- Platform Cards --- */}
            <div className="grid md:grid-cols-2 gap-8 max-w-6xl w-full mb-24">
                {/* Android Card */}
                <div className="bg-[#0a0a0a] rounded-[3rem] p-10 border border-white/10 relative overflow-hidden group hover:border-indigo-500/50 transition duration-500 flex flex-col">
                    <div className="absolute top-0 left-1/2 -translate-x-1/2 w-64 h-64 bg-indigo-500/10 blur-[100px] group-hover:bg-indigo-500/20 transition duration-500" />

                    <div className="relative z-10 flex-1 flex flex-col items-center text-center">
                        <div className="w-24 h-24 bg-white/5 rounded-3xl flex items-center justify-center mb-8 text-white group-hover:scale-110 transition duration-300 ring-1 ring-white/10">
                            <Smartphone size={48} />
                        </div>

                        <h2 className="text-3xl font-bold mb-4">Android</h2>
                        <p className="text-gray-400 mb-10 max-w-sm">
                            The flagship experience. Background play, offline caching, and system-wide audio control.
                        </p>

                        <div className="w-full max-w-xs space-y-4 mt-auto">
                            <a href="https://github.com/ABCGop/music/releases" className="w-full flex items-center justify-center gap-3 py-4 bg-white text-black rounded-2xl font-bold hover:bg-gray-200 transition">
                                <Download size={20} />
                                Download APK
                            </a>
                            <a href="https://play.google.com/store/apps/details?id=com.abcg.music" className="w-full flex items-center justify-center gap-3 py-4 bg-white/5 border border-white/10 text-white rounded-2xl font-bold hover:bg-white/10 transition">
                                <Play size={20} fill="currentColor" />
                                Google Play
                            </a>
                        </div>
                        <div className="mt-6 flex items-center gap-4 text-xs font-mono text-gray-500">
                            <span>v0.2.24</span>
                            <span className="w-1 h-1 bg-gray-700 rounded-full" />
                            <span>arm64-v8a</span>
                        </div>
                    </div>
                </div>

                {/* Windows Card */}
                <div className="bg-[#0a0a0a] rounded-[3rem] p-10 border border-white/10 relative overflow-hidden group hover:border-blue-500/50 transition duration-500 flex flex-col">
                    <div className="absolute top-0 left-1/2 -translate-x-1/2 w-64 h-64 bg-blue-500/10 blur-[100px] group-hover:bg-blue-500/20 transition duration-500" />

                    <div className="relative z-10 flex-1 flex flex-col items-center text-center">
                        <div className="w-24 h-24 bg-white/5 rounded-3xl flex items-center justify-center mb-8 text-white group-hover:scale-110 transition duration-300 ring-1 ring-white/10">
                            <Monitor size={48} />
                        </div>

                        <h2 className="text-3xl font-bold mb-4">Windows</h2>
                        <p className="text-gray-400 mb-10 max-w-sm">
                            Desktop-class performance. Global media keys, rich presence, and mini-player overlay.
                        </p>

                        <div className="w-full max-w-xs space-y-4 mt-auto">
                            <a href="https://github.com/ABCGop/music/releases/download/1.0/vxmusic-1.0.0.exe" className="w-full flex items-center justify-center gap-3 py-4 bg-white/10 text-white border border-white/10 rounded-2xl font-bold hover:bg-white hover:text-black transition">
                                <Download size={20} />
                                Download EXE
                            </a>
                            <button className="w-full py-4 text-gray-500 text-sm cursor-not-allowed">
                                Mac & Linux coming soon
                            </button>
                        </div>
                        <div className="mt-6 flex items-center gap-4 text-xs font-mono text-gray-500">
                            <span>v1.0.0</span>
                            <span className="w-1 h-1 bg-gray-700 rounded-full" />
                            <span>x64</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* --- Detailed Specs Grid --- */}
            <div className="grid md:grid-cols-2 gap-8 max-w-6xl w-full">

                {/* Changelog */}
                <div className="p-8 rounded-3xl bg-white/5 border border-white/5">
                    <h3 className="text-xl font-bold mb-6 flex items-center gap-2">
                        <CheckCircle2 className="text-green-400" /> What's New in v4.3
                    </h3>
                    <ul className="space-y-4">
                        <ChangeItem text="Synced with YouTube Music playlists." />
                        <ChangeItem text="Search inside playlist." />
                        <ChangeItem text="New 'Sleep Timer' and 'Crossfade' settings." />
                        <ChangeItem text="Reduced battery consumption by 15% on background play." />
                    </ul>
                </div>

                {/* Requirements */}
                <div className="p-8 rounded-3xl bg-white/5 border border-white/5">
                    <h3 className="text-xl font-bold mb-6 flex items-center gap-2">
                        <AlertCircle className="text-indigo-400" /> System Requirements
                    </h3>
                    <div className="space-y-6 text-sm">
                        <div>
                            <div className="text-gray-400 mb-1">Android</div>
                            <div className="font-medium text-white">Android 8.1 (Oreo) or higher. 2GB RAM minimum.</div>
                        </div>
                        <div>
                            <div className="text-gray-400 mb-1">Windows</div>
                            <div className="font-medium text-white">Windows 10/11 (64-bit). DirectX 11 compatible GPU.</div>
                        </div>
                        <div>
                            <div className="text-gray-400 mb-1">Network</div>
                            <div className="font-medium text-white">3G/4G or Wi-Fi required for streaming. Offline mode available.</div>
                        </div>
                    </div>
                </div>
            </div>

        </div >
    );
}

function ChangeItem({ text }: { text: string }) {
    return (
        <li className="flex items-start gap-3 text-gray-300">
            <div className="w-1.5 h-1.5 rounded-full bg-white/20 mt-2 shrink-0" />
            {text}
        </li>
    )
}
