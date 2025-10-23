import { Button } from "@nextui-org/react";
import Image from "next/image";
import Link from "next/link";
import React from "react";

const ScreenshotSection = () => {
  return (
    <section className="screenshot-section ">
      <div className="relative isolate pt-14">
        <h1 className="text-center scroll-m-20 text-4xl font-bold tracking-tight lg:text-5xl bg-clip-text py-10 text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
          Screenshot
        </h1>
        <div className="w-full flex items-center justify-center relative group">
          <Image
            className="hover:blur-sm transition-all duration-500 ease-in-out"
            src="/mockup/screenshot.png"
            alt="screenshot"
            width={1600}
            height={1200}
            sizes="(max-width: 1600px) 100vw, 75vw"
            style={{ objectFit: "contain" }}
          />
          <Button
            className="absolute hidden group-hover:block transition-all duration-500 ease-in-out"
            color="secondary"
          >
            <Link
              rel="noopener noreferrer"
              target="_blank"
              href="https://photos.app.goo.gl/7fnn2V3vYPqEgP9X6"
            >
              Show more
            </Link>
          </Button>
        </div>
      </div>
    </section>
  );
};

export default ScreenshotSection;
