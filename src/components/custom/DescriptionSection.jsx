import Image from "next/image";
import React from "react";

const DescriptionSection = () => {
  return (
    <section className="description-section ">
      <div className="relative isolate px-6 pt-14 lg:px-8">
        <div
          className="absolute inset-x-0 -top-40 -z-10 transform-gpu overflow-hidden blur-3xl sm:-top-80"
          aria-hidden="true"
        >
          <div
            className="relative right-[calc(50%-11rem)] aspect-[1155/678] w-[36.125rem] -translate-x-1/2 rotate-[30deg] bg-gradient-to-br from-[#80e6ff] to-[#9089fc] opacity-30 sm:left-[calc(50%-30rem)] sm:w-[72.1875rem]"
            style={{
              clipPath:
                "polygon(65% 66%, 73% 77%, 84% 69%, 93% 80%, 88% 95%, 71% 97%, 57% 92%, 51% 81%, 54% 67%)",
            }}
          ></div>
        </div>
        <div className="mx-auto place-items-center py-14 sm:py-21 lg:py-28 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2">
          <Image
            src="/mockup/description.png"
            alt="description"
            width={1200}
            height={1125}
          ></Image>
          <div>
            <h2 className="scroll-m-20 text-4xl font-bold tracking-tight lg:text-5xl pb-8 bg-clip-text text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
              VxMusic
            </h2>
            <h4 className="scroll-m-20 text-xl font-semibold tracking-tight">
              Simple UI, feature-rich, ad-free, free download, and open-source.
            </h4>
            <br></br>
            <h4 className="scroll-m-20 text-xl font-semibold tracking-tight">
              Enjoy high-quality audio streaming up to 256kbps and 1080p
              video quality. Personalize your music experience with VxMusic.
              UI is inspired by modern music streaming interfaces.
            </h4>
          </div>
        </div>
      </div>
    </section>
  );
};

export default DescriptionSection;
