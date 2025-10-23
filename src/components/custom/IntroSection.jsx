import Image from "next/image";
import React from "react";
import HomeHeading from "./typo/HomeHeading";

const IntroSection = () => {
  return (
    <section className="intro-section ">
      <div className="relative isolate px-6 py-14 lg:px-8">
        <div
          className="absolute inset-x-0 -top-40 -z-10 transform-gpu overflow-hidden blur-3xl sm:-top-80"
          aria-hidden="true"
        >
          <div
            className="relative left-[calc(50%-11rem)] aspect-[1155/678] w-[36.125rem] -translate-x-1/2 rotate-[30deg] bg-gradient-to-tr from-[#3b82f6] to-[#06b6d4] opacity-25 sm:left-[calc(50%-30rem)] sm:w-[72.1875rem]"
            style={{
              clipPath:
                "polygon(74.1% 44.1%, 100% 61.6%, 97.5% 26.9%, 85.5% 0.1%, 80.7% 2%, 72.5% 32.5%, 60.2% 62.4%, 52.4% 68.1%, 47.5% 58.3%, 45.2% 34.5%, 27.5% 76.7%, 0.1% 64.9%, 17.9% 100%, 27.6% 76.8%, 76.1% 97.7%, 74.1% 44.1%)",
            }}
          ></div>
        </div>
        <div className="mx-auto place-items-center py-21 sm:py-35 lg:py-42 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2">
          <HomeHeading />
          <Image
            src="/mockup/intro.png"
            alt="intro"
            width={1200}
            height={1125}
          ></Image>
        </div>
      </div>
    </section>
  );
};

export default IntroSection;
