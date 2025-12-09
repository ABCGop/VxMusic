'use client';

import { motion, AnimatePresence } from 'framer-motion';
import { useEffect, useState } from 'react';

export default function SplashScreen() {
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Lock scroll
    document.body.style.overflow = 'hidden';

    // Timer to finish loading
    const timer = setTimeout(() => {
      setIsLoading(false);
      document.body.style.overflow = 'unset';
    }, 2500); // 2.5 seconds duration

    return () => {
        clearTimeout(timer);
        document.body.style.overflow = 'unset';
    };
  }, []);

  return (
    <AnimatePresence>
      {isLoading && (
        <motion.div
          initial={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.8, ease: "easeInOut" }}
          className="fixed inset-0 z-[9999] flex items-center justify-center bg-[#030014] overflow-hidden"
        >
          <div className="relative z-10">
              <motion.h1
                initial={{ opacity: 0, scale: 0.8, filter: "blur(10px)" }}
                animate={{ opacity: 1, scale: 1, filter: "blur(0px)" }}
                transition={{ duration: 1, ease: "easeOut" }}
                className="text-6xl md:text-9xl font-bold tracking-tighter text-white title-glow"
              >
                VxMusic
              </motion.h1>
              
              <motion.div 
                initial={{ width: 0 }}
                animate={{ width: "100%" }}
                transition={{ duration: 2, ease: "easeInOut" }}
                className="h-1 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 mt-4 rounded-full"
              />
          </div>

          {/* Background Ambient Effect */}
          <div className="absolute inset-0 z-0">
             <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-indigo-600/20 rounded-full blur-[120px] animate-pulse" />
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
