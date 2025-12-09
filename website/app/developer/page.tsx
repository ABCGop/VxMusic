'use client';

import { motion } from 'framer-motion';
import { Github, Mail, Code2, Cpu, Globe, Rocket, Terminal, Layers } from 'lucide-react';

export default function Developer() {
  return (
    <div className="flex-1 flex flex-col items-center justify-start min-h-screen relative overflow-x-hidden">
      
      {/* --- Hero Section --- */}
      <div className="w-full max-w-6xl px-6 pt-32 pb-20">
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="grid md:grid-cols-2 gap-12 items-center"
          >
              <div className="space-y-6 order-2 md:order-1">
                   <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-mono">
                      <span className="relative flex h-2 w-2">
                        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
                        <span className="relative inline-flex rounded-full h-2 w-2 bg-indigo-500"></span>
                      </span>
                      At VxMusic
                   </div>
                   <h1 className="text-5xl md:text-7xl font-bold tracking-tight title-glow leading-tight">
                       Architecting <br/> <span className="text-gradient">The Future.</span>
                   </h1>
                   <p className="text-xl text-gray-400 leading-relaxed max-w-lg">
                       I'm <span className="text-white font-semibold">Vishesh Gangwar</span>. A Lead Architect obsessed with zero-latency systems, distributed audio engines, and crafting "invisible" interfaces.
                   </p>
                   <div className="flex gap-4 pt-4">
                       <SocialButton icon={<Github />} label="GitHub" href="https://github.com/ABCGop" />
                       <SocialButton icon={<Mail />} label="Email" href="mailto:opabcg@gmail.com" />
                   </div>
              </div>

              <div className="order-1 md:order-2 flex justify-center">
                   <div className="relative w-72 h-72 md:w-96 md:h-96">
                       <div className="absolute inset-0 bg-gradient-to-tr from-indigo-500 to-blue-600 rounded-full blur-[100px] opacity-20 animate-pulse" />
                       <div className="relative w-full h-full rounded-[2rem] overflow-hidden border border-white/10 bg-black/50 backdrop-blur-sm rotate-3 hover:rotate-0 transition duration-500">
                           <img src="https://github.com/ABCGop.png" alt="Vishesh Gangwar" className="w-full h-full object-cover" />
                       </div>
                   </div>
              </div>
          </motion.div>
      </div>

      {/* --- Stats Section --- */}
      <div className="w-full border-y border-white/5 bg-white/[0.02]">
          <div className="max-w-6xl mx-auto px-6 py-12 grid grid-cols-2 md:grid-cols-4 gap-8">
              <StatItem number="2+" label="Years Experience" />
              <StatItem number="1" label="Projects Shipped" />
              <StatItem number="100+" label="Commits" />
              <StatItem number="2k+" label="Active Users" />
          </div>
      </div>

      {/* --- Stack & Philosophy --- */}
      <div className="w-full max-w-6xl px-6 py-24 grid md:grid-cols-3 gap-8">
          <div className="md:col-span-2 space-y-8">


               {/* Experience Timeline */}
               <SectionCard title="Journey" icon={<Rocket />}>
                   <div className="space-y-8 relative pl-8 border-l border-white/10">
                       <TimelineItem 
                           year="2025" 
                           title="Class 11th" 
                           desc="Finally launched and Hits First 1.9k downloads"
                       />
                       <TimelineItem 
                           year="2024" 
                           title="Class 10th" 
                           desc="Building The VxMusic"
                       />
                       <TimelineItem 
                           year="2023" 
                           title="Class 9th" 
                           desc="The idea of creating VxMusic"
                       />
                   </div>
               </SectionCard>
          </div>

          <div className="space-y-8">
               {/* Tech Stack */}
               <SectionCard title="Tech Arsenal" icon={<Terminal />}>
                   <div className="flex flex-wrap gap-2">
                       {['Kotlin', 'Next.js', 'React', 'Rust', 'Java', 'Python', 'AWS', 'Firebase', 'Docker', 'K8s', 'TensorFlow'].map(tech => (
                           <span key={tech} className="px-3 py-1 rounded-md bg-white/5 border border-white/5 text-sm text-gray-300 hover:bg-white/10 hover:text-white transition cursor-default">
                               {tech}
                           </span>
                       ))}
                   </div>
               </SectionCard>


          </div>
      </div>
      
      {/* --- Footer CTA --- */}
      <div className="w-full max-w-4xl px-6 pb-20 text-center">
          <div className="p-12 rounded-[3rem] bg-gradient-to-b from-white/5 to-transparent border border-white/10">
              <h2 className="text-3xl md:text-5xl font-bold mb-6">Let's build the impossible.</h2>
              <p className="text-gray-400 mb-8 max-w-xl mx-auto">Open for select opportunities and ambitious collaborations.</p>
              <a href="mailto:opabcg@gmail.com" className="inline-flex items-center gap-3 px-8 py-4 bg-white text-black rounded-full font-bold text-lg hover:bg-gray-200 transition">
                  <Mail size={20} />
                  opabcg@gmail.com
              </a>
          </div>
      </div>

    </div>
  );
}

function SocialButton({ icon, label, href }: { icon: any, label: string, href: string }) {
    return (
        <a href={href} className="flex items-center gap-2 px-5 py-2.5 rounded-full bg-white/5 border border-white/10 text-white hover:bg-white hover:text-black transition group">
            <span className="group-hover:scale-110 transition">{icon}</span>
            <span className="font-medium">{label}</span>
        </a>
    )
}

function StatItem({ number, label }: { number: string, label: string }) {
    return (
        <div className="text-center">
            <div className="text-4xl md:text-5xl font-bold text-transparent bg-clip-text bg-gradient-to-b from-white to-gray-600 mb-2">{number}</div>
            <div className="text-sm text-gray-400 uppercase tracking-widest font-medium">{label}</div>
        </div>
    )
}

function SectionCard({ title, icon, children }: { title: string, icon: any, children: React.ReactNode }) {
    return (
        <div className="p-8 rounded-3xl bg-[#0a0a0a] border border-white/10 hover:border-white/20 transition duration-500 group">
             <div className="flex items-center gap-3 mb-6">
                 <div className="p-2 rounded-lg bg-white/5 text-indigo-400 group-hover:text-white transition">{icon}</div>
                 <h2 className="text-xl font-bold">{title}</h2>
             </div>
             {children}
        </div>
    )
}

function TimelineItem({ year, title, desc }: { year: string, title: string, desc: string }) {
    return (
        <div className="relative">
            <span className="absolute -left-[41px] top-1 w-5 h-5 bg-[#0a0a0a] border-2 border-indigo-500 rounded-full" />
            <div className="text-indigo-400 text-sm font-mono mb-1">{year}</div>
            <h3 className="text-lg font-bold text-white mb-2">{title}</h3>
            <p className="text-gray-400 text-sm leading-relaxed">{desc}</p>
        </div>
    )
}
