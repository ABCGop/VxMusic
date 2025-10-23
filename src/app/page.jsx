import BlogSection from "@/components/custom/BlogSection";
import DescriptionSection from "@/components/custom/DescriptionSection";
import DownloadInfoSection from "@/components/custom/DownloadInfoSection";
import FeatureSection from "@/components/custom/FeatureSection";
import ModernHero from "@/components/custom/ModernHero";
import ScreenshotSection from "@/components/custom/ScreenshotSection";

export const metadata = {
  title: "VxMusic - Free Open-Source Music Streaming App",
  description: "Download VxMusic and enjoy free music streaming with a clean interface. A perfect open-source alternative to Spotify and other music apps with unlimited songs and playlists.",
  keywords: "vxmusic download, free music app, open source music player, spotify alternative, simpmusic alternative, free music streaming",
  alternates: {
    canonical: "https://vxmusic.in"
  }
};

export default function Home() {
  return (
    <>
      {/* Modern Hero Section */}
      <ModernHero />
      
      {/* Enhanced Content Sections */}
      <div className="relative z-10">
        <div className="lg:px-16 md:px-16 sm:px-10">
          <DescriptionSection />
          <FeatureSection />
        </div>
        
        <ScreenshotSection />
        
        {/* Blog Section */}
        <BlogSection />
        
        <div className="lg:px-16 md:px-16 sm:px-10">
          <DownloadInfoSection />
        </div>
      </div>
    </>
  );
}
