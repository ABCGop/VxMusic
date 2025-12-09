import type { NextConfig } from "next";

/**
 * Ensure CSS variables and class-based dark mode work well with next-themes.
 * The suppressHydrationWarning is already set in layout.tsx's <html>.
 */
const nextConfig: NextConfig = {
  experimental: {
    optimizeCss: true,
  },
  reactStrictMode: true,
  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "*",
      }
    ],
    formats: ['image/webp', 'image/avif'],
  },
  // Enable compression
  compress: true,
  // Enable poweredByHeader removal for security
  poweredByHeader: false,
  // Enable trailingSlash for consistency
  trailingSlash: false,
  // Generate static exports for better performance
  output: 'standalone',
};

export default nextConfig;
