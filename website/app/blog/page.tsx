'use client';

import { motion } from 'framer-motion';
import { ArrowRight, Calendar, Clock, TrendingUp } from 'lucide-react';
import Link from 'next/link';

export default function Blog() {
    const posts = [
        {
            slug: 'future-of-streaming',
            title: 'The Future of Music Streaming is Decentralized',
            excerpt: 'Why centralized servers are a bottleneck for high-fidelity audio, and how parallel networking solves the latency problem.',
            date: 'April 2, 2025',
            readTime: '5 min read',
            category: 'Technology'
        },
        {
            slug: 'why-open-source',
            title: 'Why VxMusic Went 100% Open Source',
            excerpt: 'Transparency isn\'t just a buzzword. It\'s the only way to guarantee privacy in an age of data surveillance.',
            date: 'March 28, 2025',
            readTime: '4 min read',
            category: 'Philosophy'
        },
        {
            slug: 'mastering-high-fidelity',
            title: 'Understanding Lossless Audio: FLAC vs ALAC',
            excerpt: 'A deep dive into bitrates, sample rates, and why your bluetooth headphones might be lying to you.',
            date: 'March 15, 2025',
            readTime: '8 min read',
            category: 'Audio Engineering'
        },
        {
            slug: 'youtube-integration-explained',
            title: 'How Sync Works: The Magic Behind the Bridge',
            excerpt: 'Technical breakdown of how VxMusic indexes YouTube Music playlists without compromising on UI performance.',
            date: 'March 10, 2025',
            readTime: '6 min read',
            category: 'Features'
        },
        {
            slug: 'spatial-audio-revolution',
            title: 'The Science of Spatial Audio: Beyond Stereo',
            excerpt: 'How HRTF and object-based audio are changing the way we perceive sound in a 3D space.',
            date: 'February 28, 2025',
            readTime: '7 min read',
            category: 'Audio Engineering'
        },
        {
            slug: 'kotlin-multiplatform-journey',
            title: 'Why We Chose Kotlin Multiplatform over Flutter',
            excerpt: 'A technical look at sharing 80% of code between Android, Desktop, and iOS native performance.',
            date: 'February 15, 2025',
            readTime: '9 min read',
            category: 'Technology'
        },
        {
            slug: 'digital-rights-management-explained',
            title: 'The State of Digital Rights Management (DRM)',
            excerpt: 'Analyzing the ethics of walled gardens, file ownership, and the potential of Web3 music.',
            date: 'January 10, 2025',
            readTime: '6 min read',
            category: 'Philosophy'
        },
        {
            slug: 'android-audio-latency-deep-dive',
            title: 'Optimizing Android Audio Latency',
            excerpt: 'How we bypassed the Java Garbage Collector to achieve sub-10ms round-trip audio latency on Pixel devices.',
            date: 'December 5, 2024',
            readTime: '10 min read',
            category: 'Engineering'
        },
        {
            slug: 'psychology-of-playlists',
            title: 'The Psychology of Playlists',
            excerpt: 'From mixtapes to algorithmically generated daily mixes, why do we feel the need to curate?',
            date: 'November 20, 2024',
            readTime: '5 min read',
            category: 'Culture'
        },
        {
            slug: 'vinyl-vs-digital-debate',
            title: 'Vinyl vs Digital: Is it really warmer?',
            excerpt: 'The debate rages on. Analog purists swear by the warmth of vinyl. Digital natives claim FLAC is superior. Who is right?',
            date: 'November 12, 2024',
            readTime: '6 min read',
            category: 'Audio Engineering'
        }
    ];

    return (
        <div className="flex-1 min-h-screen pt-32 pb-20">
            <div className="container mx-auto px-6 max-w-6xl">

                {/* Header */}
                <div className="text-center mb-20">
                    <motion.h1
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="text-5xl md:text-7xl font-bold mb-6 title-glow"
                    >
                        Transmission <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 to-purple-400">Log</span>
                    </motion.h1>
                    <motion.p
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        transition={{ delay: 0.2 }}
                        className="text-xl text-gray-400 max-w-2xl mx-auto"
                    >
                        Insights into audio engineering, open source philosophy, and the roadmap ahead.
                    </motion.p>
                </div>

                {/* Featured Post (First one) */}
                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.3 }}
                    className="mb-16"
                >
                    <Link href={`/blog/${posts[0].slug}`} className="group relative block p-8 md:p-12 rounded-[2rem] bg-white/5 border border-white/10 hover:border-white/20 overflow-hidden transition-all duration-500 hover:shadow-[0_0_50px_rgba(99,102,241,0.15)]">
                        <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/10 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition duration-500" />

                        <div className="relative z-10 flex flex-col md:flex-row gap-8 items-start justify-between">
                            <div className="flex-1">
                                <div className="flex items-center gap-3 mb-4 text-indigo-400 text-sm font-medium tracking-wider uppercase">
                                    <TrendingUp className="w-4 h-4" />
                                    FEATURED • {posts[0].category}
                                </div>
                                <h2 className="text-3xl md:text-5xl font-bold mb-6 group-hover:text-white transition-colors">
                                    {posts[0].title}
                                </h2>
                                <p className="text-xl text-gray-400 mb-8 max-w-2xl leading-relaxed">
                                    {posts[0].excerpt}
                                </p>
                                <div className="flex items-center gap-6 text-sm text-gray-500">
                                    <span className="flex items-center gap-2"><Calendar className="w-4 h-4" /> {posts[0].date}</span>
                                    <span className="flex items-center gap-2"><Clock className="w-4 h-4" /> {posts[0].readTime}</span>
                                </div>
                            </div>

                            <div className="hidden md:flex items-center justify-center w-32 h-32 rounded-full border border-white/10 group-hover:bg-white group-hover:text-black transition-all duration-500">
                                <ArrowRight className="w-10 h-10 -rotate-45 group-hover:rotate-0 transition-transform duration-500" />
                            </div>
                        </div>
                    </Link>
                </motion.div>

                {/* Recent Posts Grid */}
                <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
                    {posts.slice(1).map((post, index) => (
                        <motion.div
                            key={post.slug}
                            initial={{ opacity: 0, y: 20 }}
                            whileInView={{ opacity: 1, y: 0 }}
                            viewport={{ once: true }}
                            transition={{ delay: index * 0.1 }}
                        >
                            <Link href={`/blog/${post.slug}`} className="group flex flex-col h-full p-8 rounded-3xl bg-white/5 border border-white/5 hover:bg-white/10 hover:border-white/20 transition duration-300">
                                <div className="text-xs font-bold text-indigo-400 mb-4 uppercase tracking-widest">{post.category}</div>
                                <h3 className="text-2xl font-bold mb-4 group-hover:text-white transition-colors">{post.title}</h3>
                                <p className="text-gray-400 mb-8 flex-1 leading-relaxed">
                                    {post.excerpt}
                                </p>
                                <div className="mt-auto flex items-center justify-between pt-6 border-t border-white/5 text-sm text-gray-500">
                                    <span>{post.date}</span>
                                    <span className="flex items-center gap-2 group-hover:translate-x-1 transition-transform text-white">
                                        Read <ArrowRight className="w-4 h-4" />
                                    </span>
                                </div>
                            </Link>
                        </motion.div>
                    ))}
                </div>

            </div>
        </div>
    );
}
