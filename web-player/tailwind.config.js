/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/*.{js,ts,jsx,tsx,mdx}"
  ],
  theme: {
    extend: {
      colors: {
        background: '#030303', // Deepest black
        surface: '#0A0A0A',
        surfaceHighlight: '#1A1A1A',
        primary: '#3B82F6',
        primaryGlow: '#60A5FA',
        accent: '#8B5CF6',
      },
      animation: {
        'aurora': 'aurora 20s linear infinite',
        'float': 'float 6s ease-in-out infinite',
        'pulse-slow': 'pulse 8s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin-slow': 'spin 12s linear infinite',
      },
      keyframes: {
        aurora: {
          '0%': { backgroundPosition: '50% 50%, 50% 50%' },
          '100%': { backgroundPosition: '350% 50%, 350% 50%' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-20px)' },
        }
      },
      backgroundImage: {
        'aurora-mesh': 'radial-gradient(at top left, rgba(59, 130, 246, 0.15) 0%, transparent 50%), radial-gradient(at top right, rgba(139, 92, 246, 0.15) 0%, transparent 50%), radial-gradient(at bottom center, rgba(59, 130, 246, 0.1) 0%, transparent 70%)',
      }
    },
  },
  plugins: [],
}
