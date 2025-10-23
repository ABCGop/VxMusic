import { ArrowLeftIcon, ArrowRightIcon, CalendarIcon, ClockIcon } from "@heroicons/react/24/outline";
import Link from "next/link";
import React from "react";

export const metadata = {
  title: "Blog | VxMusic - Free Music Streaming App",
  description: "Read the latest updates, tutorials, and insights about VxMusic. Learn how to get the most out of your free music streaming experience.",
  keywords: "vxmusic blog, music streaming tips, android music app, open source music alternative"
};

export default function BlogPage() {
  const blogPosts = [
    {
      id: 1,
      title: "How VxMusic is Revolutionizing Free Music Streaming",
      excerpt: "Discover how VxMusic brings you unlimited music streaming without the premium price tag. Learn about our innovative approach to music discovery.",
      date: "September 15, 2025",
      readTime: "5 min read",
      category: "Product",
      slug: "vxmusic-revolutionizing-free-streaming"
    },
    {
      id: 2,
      title: "Android Auto Integration: Music on the Road",
      excerpt: "Experience seamless music streaming in your car with VxMusic's Android Auto integration. Safe, hands-free, and feature-rich.",
      date: "September 10, 2025",
      readTime: "3 min read",
      category: "Features",
      slug: "android-auto-integration"
    },
    {
      id: 3,
      title: "Why Choose VxMusic Over Spotify Premium?",
      excerpt: "Compare VxMusic's free features with premium streaming services. Get the same quality without the monthly subscription fees.",
      date: "September 5, 2025",
      readTime: "7 min read",
      category: "Comparison",
      slug: "vxmusic-vs-spotify-premium"
    },
    {
      id: 4,
      title: "Getting Started with VxMusic: A Complete Guide",
      excerpt: "New to VxMusic? This comprehensive guide will help you set up and master all the features of your new favorite music app.",
      date: "August 30, 2025",
      readTime: "6 min read",
      category: "Tutorial",
      slug: "getting-started-complete-guide"
    },
    {
      id: 5,
      title: "Top 10 Hidden Features in VxMusic You Should Know",
      excerpt: "Unlock the full potential of VxMusic with these amazing hidden features and pro tips that will enhance your music streaming experience.",
      date: "September 20, 2025",
      readTime: "4 min read",
      category: "Tips & Tricks",
      slug: "top-10-hidden-features"
    },
    {
      id: 6,
      title: "VxMusic vs Other Music Apps: Feature Comparison",
      excerpt: "Detailed comparison between VxMusic and other music streaming services. Discover why VxMusic offers better value and features for music lovers.",
      date: "September 18, 2025",
      readTime: "8 min read",
      category: "Comparison",
      slug: "vxmusic-vs-other-music-apps"
    }
  ];

  const categoryColors = {
    "Product": "bg-primary/10 text-primary",
    "Features": "bg-accent/10 text-accent",
    "Comparison": "bg-yellow-500/10 text-yellow-600",
    "Tutorial": "bg-green-500/10 text-green-600"
  };

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-6 py-24">
        {/* Back to Home */}
        <Link 
          href="/"
          className="inline-flex items-center gap-2 text-muted-foreground hover:text-foreground mb-8 transition-colors"
        >
          <ArrowLeftIcon className="w-4 h-4" />
          Back to Home
        </Link>

        {/* Page Header */}
        <div className="text-center mb-16">
          <h1 className="text-hero gradient-text mb-6">
            VxMusic Blog
          </h1>
          <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
            Discover the latest features, tips, and insights about VxMusic. 
            Stay updated with everything happening in the world of free music streaming.
          </p>
        </div>

        {/* Blog Posts Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-8 max-w-6xl mx-auto">
          {blogPosts.map((post) => (
            <article 
              key={post.id}
              className="group card-elevated hover:scale-105 transition-all duration-300 overflow-hidden"
            >
              {/* Blog Image */}
              <div className="relative h-48 overflow-hidden bg-gradient-to-br from-primary/20 to-accent/20">
                <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
                <div className="absolute top-4 left-4">
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${categoryColors[post.category]}`}>
                    {post.category}
                  </span>
                </div>
                {/* Placeholder for actual image */}
                <div className="w-full h-full flex items-center justify-center text-6xl">
                  🎵
                </div>
              </div>

              {/* Blog Content */}
              <div className="p-6">
                {/* Meta Information */}
                <div className="flex items-center gap-4 text-sm text-muted-foreground mb-3">
                  <div className="flex items-center gap-1">
                    <CalendarIcon className="w-4 h-4" />
                    <span>{post.date}</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <ClockIcon className="w-4 h-4" />
                    <span>{post.readTime}</span>
                  </div>
                </div>

                {/* Title */}
                <h3 className="text-xl font-semibold mb-3 group-hover:text-primary transition-colors duration-200">
                  {post.title}
                </h3>

                {/* Excerpt */}
                <p className="text-muted-foreground mb-4 line-clamp-3">
                  {post.excerpt}
                </p>

                {/* Read More Link */}
                <Link 
                  href={`/blog/${post.slug}`}
                  className="inline-flex items-center gap-2 text-primary font-medium hover:gap-3 transition-all duration-200"
                >
                  Read More
                  <ArrowRightIcon className="w-4 h-4" />
                </Link>
              </div>
            </article>
          ))}
        </div>

        {/* Call to Action */}
        <div className="text-center mt-16">
          <div className="card-elevated max-w-2xl mx-auto p-8">
            <h2 className="text-2xl font-semibold mb-4">Ready to Experience VxMusic?</h2>
            <p className="text-muted-foreground mb-6">
              Join thousands of users who have already discovered the freedom of unlimited music streaming.
            </p>
            <Link 
              href="/download"
              className="btn-primary inline-block"
            >
              Download VxMusic Now
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}