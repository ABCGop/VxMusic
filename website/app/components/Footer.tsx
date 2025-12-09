'use client';

import Link from 'next/link';

export default function Footer() {
    return (
        <footer className="w-full py-16 border-t border-white/5 bg-[#010103] mt-auto relative z-20">
            <div className="container mx-auto px-6 max-w-7xl">
                <div className="grid grid-cols-2 md:grid-cols-4 gap-12 mb-16">

                    <div className="col-span-2 md:col-span-1">
                        <Link href="/" className="text-2xl font-bold text-white mb-6 block title-glow">VxMusic</Link>
                        <p className="text-gray-500 text-sm leading-relaxed mb-6">
                            Open source, high-fidelity music streaming for the decentralized web. Built for audiophiles, by audiophiles.
                        </p>
                    </div>

                    <div>
                        <h4 className="font-bold text-white mb-6">Product</h4>
                        <ul className="space-y-4 text-sm text-gray-400">
                            <li><Link href="/download" className="hover:text-indigo-400 transition">Download</Link></li>
                            <li><a href="https://web.vxmusic.in" className="hover:text-indigo-400 transition">Web Player</a></li>
                            <li><Link href="/#features" className="hover:text-indigo-400 transition">Features</Link></li>
                        </ul>
                    </div>

                    <div>
                        <h4 className="font-bold text-white mb-6">Resources</h4>
                        <ul className="space-y-4 text-sm text-gray-400">
                            <li><Link href="/blog" className="hover:text-indigo-400 transition flex items-center gap-2">Blog <span className="px-1.5 py-0.5 rounded text-[10px] bg-indigo-500/10 text-indigo-400 font-bold border border-indigo-500/20">NEW</span></Link></li>
                            <li><Link href="/about" className="hover:text-indigo-400 transition">About Us</Link></li>
                            <li><Link href="/contact" className="hover:text-indigo-400 transition">Support</Link></li>
                            <li><Link href="/developer" className="hover:text-indigo-400 transition">Developers</Link></li>
                        </ul>
                    </div>

                    <div>
                        <h4 className="font-bold text-white mb-6">Legal</h4>
                        <ul className="space-y-4 text-sm text-gray-400">
                            <li><Link href="/privacy-policy" className="hover:text-indigo-400 transition">Privacy Policy</Link></li>
                            <li><Link href="/terms" className="hover:text-indigo-400 transition">Terms of Service</Link></li>
                            <li><a href="#" className="hover:text-indigo-400 transition">Cookie Settings</a></li>
                        </ul>
                    </div>
                </div>

                <div className="pt-8 border-t border-white/5 flex flex-col md:flex-row justify-between items-center gap-4">
                    <p className="text-gray-600 text-sm">© 2025 Vishesh Gangwar. Open Source (MIT).</p>
                    <div className="flex gap-6">
                        {/* Social Icons could go here */}
                    </div>
                </div>
            </div>
        </footer>
    );
}
