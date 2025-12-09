'use client';

import { motion } from 'framer-motion';
import { ArrowLeft, Calendar, Share2, Twitter } from 'lucide-react';
import Link from 'next/link';

interface BlogPostProps {
    post: {
        title: string;
        date: string;
        category: string;
        readTime: string;
        content: React.ReactNode;
    };
    relatedPosts: Array<{
        slug: string;
        title: string;
        category: string;
    }>;
}

export default function BlogPostClient({ post, relatedPosts }: BlogPostProps) {
    return (
        <article className="flex-1 min-h-screen pt-32 pb-20">

            {/* Progress Bar (Simulated) */}
            <motion.div
                className="fixed top-0 left-0 h-1 bg-indigo-500 z-50 origin-left"
                initial={{ scaleX: 0 }}
                animate={{ scaleX: 1 }}
                transition={{ duration: 1, ease: "easeOut" }}
            />

            <div className="container mx-auto px-6 max-w-4xl relative">

                <Link href="/blog" className="inline-flex items-center gap-2 text-gray-400 hover:text-white mb-12 transition group">
                    <ArrowLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
                    Back to Log
                </Link>

                <header className="mb-16">
                    <div className="flex items-center gap-4 text-indigo-400 font-medium tracking-widest uppercase text-sm mb-6">
                        <span>{post.category}</span>
                        <span className="w-1 h-1 rounded-full bg-indigo-400" />
                        <span>{post.readTime}</span>
                    </div>
                    <h1 className="text-4xl md:text-6xl font-bold mb-8 leading-tight title-glow">
                        {post.title}
                    </h1>
                    <div className="flex items-center justify-between border-y border-white/10 py-6">
                        <div className="flex items-center gap-2 text-gray-400">
                            <Calendar className="w-5 h-5" />
                            {post.date}
                        </div>
                        <div className="flex items-center gap-4">
                            <button className="p-2 rounded-full hover:bg-white/10 transition text-gray-400 hover:text-white">
                                <Share2 className="w-5 h-5" />
                            </button>
                            <button className="p-2 rounded-full hover:bg-white/10 transition text-gray-400 hover:text-white">
                                <Twitter className="w-5 h-5" />
                            </button>
                        </div>
                    </div>
                </header>

                <div className="prose prose-invert prose-lg max-w-none prose-headings:font-bold prose-headings:text-white prose-p:text-gray-300 prose-a:text-indigo-400 prose-blockquote:border-l-indigo-500 prose-blockquote:bg-white/5 prose-blockquote:py-2 prose-blockquote:px-6 prose-blockquote:rounded-r-lg prose-blockquote:not-italic group">
                    {post.content}
                </div>

                <div className="mt-20 pt-12 border-t border-white/10">
                    <h3 className="text-2xl font-bold mb-8">Continue Reading</h3>
                    <div className="grid md:grid-cols-2 gap-6">
                        {relatedPosts.map((p) => (
                            <Link key={p.slug} href={`/blog/${p.slug}`} className="block p-6 rounded-2xl bg-white/5 border border-white/5 hover:border-white/20 hover:bg-white/10 transition">
                                <div className="text-xs font-bold text-indigo-400 mb-2 uppercase">{p.category}</div>
                                <h4 className="text-lg font-bold">{p.title}</h4>
                            </Link>
                        ))}
                    </div>
                </div>

            </div>
        </article>
    );
}
