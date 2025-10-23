import { Card, CardBody, CardHeader } from "@nextui-org/react";
import React from "react";
import { FaSearch } from "react-icons/fa";
import { IoCloudOffline } from "react-icons/io5";
import { MdFeaturedPlayList, MdLibraryMusic, MdLyrics } from "react-icons/md";
import { TbAnalyze } from "react-icons/tb";
const features = [
  {
    title: "Streaming Music",
    descriptions:
      "Play music with a clean, ad-free interface and background playback support",
    image: MdLibraryMusic,
  },
  {
    title: "Browsing",
    descriptions:
      "Browse through various music categories, charts, podcasts, and genres with fast performance",
    image: FaSearch,
  },
  {
    title: "Personalized",
    descriptions:
      "Analyze your listening habits, create custom playlists, and manage your music library",
    image: TbAnalyze,
  },
  {
    title: "Offline Playing",
    descriptions: "Caching and can save data for offline playback",
    image: IoCloudOffline,
  },
  {
    title: "Synced lyrics",
    descriptions:
      "Synced lyrics from various sources including Musixmatch with translation support",
    image: MdLyrics,
  },
  {
    title: "Many more",
    descriptions:
      "Many more features like SponsorBlock, Sleep Timer, Android Auto, Video Option, etc",
    image: MdFeaturedPlayList,
  },
];
const FeatureSection = () => {
  return (
    <div className="py-4">
      <h1 className="text-center scroll-m-20 text-4xl font-bold tracking-tight lg:text-5xl bg-clip-text py-10 text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
        Feature
      </h1>
      <div className="grid grid-cols-1 mx-14 md:grid-cols-2 lg:grid-cols-3 gap-4 justify-items-start">
        {features.map((feature, index) => (
          <Card
            key={index}
            isBlurred
            className="text-center justify-self-stretch py-7 hover:shadow-2xl transition-all duration-300 ease-in-out"
          >
            <CardHeader className="flex justify-center">
              <feature.image className="py-2" size={42} />
            </CardHeader>
            <CardBody>
              <h1 className="text-center text-2xl font-bold tracking-tight lg:text-3xl bg-clip-text py-2 text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
                {feature.title}
              </h1>
            </CardBody>
            <CardBody className="lg:px-24 md:px-12 px-10 py-2">
              <p className="text-center">{feature.descriptions}</p>
            </CardBody>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default FeatureSection;
