import { Image } from "@nextui-org/react";
import Link from "next/link";
import React from "react";
const store = [
  {
    name: "Github",
    url: "https://github.com/ABCGop/music/releases",
    thumbnail:
      "https://raw.githubusercontent.com/NeoApplications/Neo-Backup/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png",
  },
  {
    name: "Play Store",
    url: "https://play.google.com/store/apps/details?id=com.abcg.music",
    thumbnail:
      "data:image/png;base64,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",
  },
];
const DownloadSection = () => {
  return (
    <section className="download-section ">
      <div className="relative isolate px-6 py-20 lg:px-8">
        <div
          className="absolute inset-x-0 -top-40 -z-10 transform-gpu overflow-hidden blur-3xl sm:-top-80"
          aria-hidden="true"
        >
          <div
            className="relative left-[calc(50%-11rem)] aspect-[1155/678] w-[36.125rem] -translate-x-1/2 rotate-[30deg] bg-gradient-to-tr from-[#80e6ff] to-[#9089fc] opacity-30 sm:left-[calc(50%-30rem)] sm:w-[72.1875rem]"
            style={{
              clipPath:
                "polygon(74.1% 44.1%, 100% 61.6%, 97.5% 26.9%, 85.5% 0.1%, 80.7% 2%, 72.5% 32.5%, 60.2% 62.4%, 52.4% 68.1%, 47.5% 58.3%, 45.2% 34.5%, 27.5% 76.7%, 0.1% 64.9%, 17.9% 100%, 27.6% 76.8%, 76.1% 97.7%, 74.1% 44.1%)",
            }}
          ></div>
        </div>
        <div className="mx-auto place-items-center py-21 sm:py-35 lg:py-42">
          <div className="text-center mb-12">
            <h2 className="scroll-m-20 text-4xl font-bold tracking-tight lg:text-5xl pb-8 bg-clip-text text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
              Get VxMusic now!
            </h2>
            <h4 className="scroll-m-20 text-xl font-semibold tracking-tight blink">
              VxMusic is available on Android and Windows. Download it now!
            </h4>
            <p className="py-4 text-sm text-gray-500/80">
              Supports Android 8.0+ and Windows 10+ (64-bit). Also supports Android Auto.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-12 max-w-4xl mx-auto">
            {/* Phone Options */}
            <div className="flex flex-col items-center">
              <h3 className="text-lg font-semibold mb-4">Phone Options</h3>
              <div className="flex flex-col gap-4">
                {store.map((item, key) => (
                  <a
                    key={key}
                    href={item.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-block"
                  >
                    <Image
                      height={111}
                      width={288}
                      alt={item.name}
                      src={item.thumbnail}
                    ></Image>
                  </a>
                ))}
              </div>
            </div>

            {/* PC Options */}
            <div className="flex flex-col items-center">
              <h3 className="text-lg font-semibold mb-4">PC Options</h3>
              <Link href="/download/windows" className="inline-flex items-center gap-2 btn-primary px-6 py-3">
                Download for Windows
              </Link>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default DownloadSection;
