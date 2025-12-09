'use client';

import Link from 'next/link';

export default function Footer() {
    return (
        <footer className="w-full py-12 border-t border-white/10 bg-[#050510] mt-auto">
            <div className="container mx-auto px-6">
                <div className="flex flex-col md:flex-row justify-between items-center gap-8">
                    
                    <div className="text-center md:text-left">
                        <h3 className="text-xl font-bold text-white mb-2">VxMusic Lyrics</h3>
                        <p className="text-gray-500 text-sm">© 2025 Vishesh Gangwar. All rights reserved.</p>
                    </div>

                    <div className="flex gap-8 text-sm font-medium text-gray-400">
                        {/* <Link href="/terms" className="hover:text-white transition">Terms of Service</Link> */}
                        <Link href="/privacy-policy" className="hover:text-white transition">Privacy Policy</Link>
                        {/* <Link href="/contact" className="hover:text-white transition">Contact</Link> */}
                    </div>
                </div>
            </div>
        </footer>
    );
}