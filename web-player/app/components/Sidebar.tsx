'use client';

import { Home, Music, Search } from 'lucide-react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function Sidebar() {
    const pathname = usePathname();

    return (
        <aside className="w-64 flex-col gap-2 p-6 hidden md:flex h-screen sticky top-0 border-r border-white/5 bg-black/20 backdrop-blur-xl z-50">
            <div className="flex items-center gap-2 mb-8 px-2">
                <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-blue-500 to-purple-600 flex items-center justify-center">
                    <Music className="text-white w-5 h-5" />
                </div>
                <span className="font-bold text-xl tracking-tight">VxMusic</span>
            </div>

            <div className="space-y-2">
                <NavItem href="/" icon={<Home size={24} />} label="Home" active={pathname === '/'} />
                <NavItem href="/search" icon={<Search size={24} />} label="Search" active={pathname === '/search'} />
            </div>

            <div className="mt-6 px-2">
                <div className="p-4 bg-gradient-to-br from-blue-600/20 to-purple-600/20 rounded-xl border border-blue-500/20">
                    <h3 className="text-sm font-bold text-white mb-1">Get the App</h3>
                    <p className="text-xs text-gray-400 mb-3">Best experience on mobile.</p>
                    <a href="#" className="block w-full py-2 bg-blue-600 text-white text-center rounded-lg text-xs font-bold hover:bg-blue-500 transition shadow-lg shadow-blue-500/20">
                        Download APK
                    </a>
                </div>
            </div>
        </aside>
    );
}

function NavItem({ href, icon, label, active }: { href: string; icon: React.ReactNode; label: string; active: boolean }) {
    return (
        <Link
            href={href}
            className={`flex items-center gap-4 px-4 py-3 rounded-lg transition font-medium ${active ? 'bg-white/10 text-white' : 'text-gray-400 hover:text-white hover:bg-white/5'}`}
        >
            {icon}
            <span>{label}</span>
        </Link>
    )
}
