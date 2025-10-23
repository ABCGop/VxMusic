import { Link } from "@nextui-org/react";
import Image from "next/image";
import React from "react";

const Logo = () => {
  return (
    <Link href="/" className="flex items-center gap-2">
      <div className="relative">
        <Image
          className="mx-2 rounded-full border-2 border-primary/20 shadow-lg hover:shadow-xl transition-all duration-200 hover:scale-105"
          src="/logo.webp"
          height={40}
          width={40}
          loading="lazy"
          alt="VxMusic"
        />
        {/* Optional: Add a subtle glow effect */}
        <div className="absolute inset-0 rounded-full bg-gradient-to-r from-primary/10 to-accent/10 opacity-0 hover:opacity-100 transition-opacity duration-200 mx-2"></div>
      </div>
      <span className="font-semibold text-foreground hidden sm:block">VxMusic</span>
    </Link>
  );
};

export default Logo;
