'use client';

import { AnimatePresence, motion } from 'framer-motion';
import { Menu, X } from 'lucide-react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useState } from 'react';

export default function Navbar() {
    const pathname = usePathname();
    const [isOpen, setIsOpen] = useState(false);

    const links = [
        { href: '/', label: 'Atlas' },
        { href: '/about', label: 'Mission' },
        // { href: '/developer', label: 'Developer' }, // Commented out as likely not present
        // { href: '/download', label: 'Download' },   // Commented out as likely not present
    ];

    return (
        <nav className="fixed top-0 left-0 right-0 z-50 flex justify-center items-center py-6 px-4">
            {/* Desktop Nav */}
            <div className="hidden md:flex items-center gap-1 p-1 rounded-full bg-white/5 border border-white/10 backdrop-blur-md">
                {links.map(link => (
                    <Link
                        key={link.href}
                        href={link.href}
                        className={`px-6 py-2 rounded-full text-sm font-medium transition-all duration-300 ${pathname === link.href
                            ? 'bg-white text-black shadow-lg'
                            : 'text-gray-400 hover:text-white'
                            }`}
                    >
                        {link.label}
                    </Link>
                ))}
            </div>

            {/* Mobile Nav Toggle */}
            <div className="md:hidden w-full flex justify-between items-center bg-black/50 backdrop-blur-md p-4 rounded-2xl border border-white/5">
                <div className="font-bold text-xl tracking-tight">VxMusic</div>
                <button
                    onClick={() => setIsOpen(!isOpen)}
                    className="p-2 rounded-full bg-white/10 hover:bg-white/20 transition"
                >
                    {isOpen ? <X size={24} /> : <Menu size={24} />}
                </button>
            </div>

            {/* Mobile Menu Overlay */}
            <AnimatePresence>
                {isOpen && (
                    <motion.div
                        initial={{ opacity: 0, y: -20, scale: 0.95 }}
                        animate={{ opacity: 1, y: 0, scale: 1 }}
                        exit={{ opacity: 0, y: -20, scale: 0.95 }}
                        className="absolute top-24 left-4 right-4 bg-gray-900 border border-white/10 rounded-2xl p-4 flex flex-col gap-2 shadow-2xl md:hidden z-50 overflow-hidden"
                    >
                        {links.map(link => (
                            <Link
                                key={link.href}
                                href={link.href}
                                onClick={() => setIsOpen(false)}
                                className={`px-4 py-3 rounded-xl text-lg font-medium transition-all ${pathname === link.href
                                    ? 'bg-white text-black'
                                    : 'text-gray-400 hover:text-white hover:bg-white/5'
                                    }`}
                            >
                                {link.label}
                            </Link>
                        ))}
                    </motion.div>
                )}
            </AnimatePresence>
        </nav>
    );
}
