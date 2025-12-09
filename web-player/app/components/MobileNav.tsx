'use client';

import Link from 'next/link';
import { Home, Search, Music } from 'lucide-react';
import { usePathname } from 'next/navigation';

export default function MobileNav() {
  const pathname = usePathname();

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 h-16 bg-black border-t border-white/10 flex items-center justify-around z-[40] pb-safe">
      <NavItem href="/" icon={<Home size={24} />} label="Home" active={pathname === '/'} />
      <NavItem href="/search" icon={<Search size={24} />} label="Search" active={pathname === '/search'} />
      {/* Add more items if needed */}
    </nav>
  );
}

function NavItem({ href, icon, label, active }: { href: string; icon: React.ReactNode; label: string; active: boolean }) {
    return (
        <Link 
            href={href} 
            className={`flex flex-col items-center justify-center gap-1 w-full h-full transition ${active ? 'text-white' : 'text-gray-500 hover:text-gray-300'}`}
        >
            {icon}
            <span className="text-[10px] font-medium">{label}</span>
        </Link>
    )
}
