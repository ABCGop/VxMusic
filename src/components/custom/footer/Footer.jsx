"use client";
import { Card, CardBody, CardHeader, Image, Link } from "@nextui-org/react";
import NextLink from "next/link";
import React from "react";
import { TbBrandNextjs } from "react-icons/tb";

const communities = [
  {
    title: "Discord",
    description: "Join our Discord server to get in touch with the community.",
    thumbnail:
      "https://assets-global.website-files.com/6257adef93867e50d84d30e2/636e0a69f118df70ad7828d4_icon_clyde_blurple_RGB.svg",
    url: "https://discord.gg/msHXQQGAFa",
  },
  {
    title: "GitHub",
    description: "Contribute to the project on GitHub or create an issue.",
    thumbnail:
      "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png",
    url: "https://github.com/ABCGop/VxMusic",
  },
];
const Footer = () => {
  return (
    <div className="relative isolate px-6 py-28 lg:px-8">
      <div
        className="absolute inset-x-0 -z-10 transform-gpu overflow-hidden blur-3xl"
        aria-hidden="true"
      >
        <div
          className="relative left-[50%] aspect-[3/1] w-full -translate-x-1/2 rotate-[30deg] bg-gradient-to-r from-gradientstart/60 to-gradientend/60 opacity-30"
          style={{
            clipPath:
              "polygon(45% 55%, 61% 46%, 69% 55%, 78% 66%, 61% 81%, 35% 82%, 20% 75%, 24% 49%)",
          }}
        ></div>
      </div>
      <div className="py-20">
        <h2 className="scroll-m-20 text-center text-4xl font-bold tracking-tight lg:text-5xl pb-8 bg-clip-text text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
          Community
        </h2>
        <h4 className="scroll-m-20 text-xl text-center font-semibold tracking-tight">
          Get involved in our community. Everyone is welcome!
        </h4>
      </div>
      <div className="mx-21 my-10 flex flex-wrap justify-center justify-items-center gap-12">
        {communities.map((item, key) => (
          <Card
            key={key}
            isBlurred
            isPressable
            onPress={() => window.open(item.url, "_blank")}
            className="text-center shrink py-7 px-10 hover:shadow-2xl transition-all duration-300 ease-in-out w-96"
          >
            <CardHeader className="flex gap-3">
              <Image
                alt="logo"
                height={40}
                radius="sm"
                src={item.thumbnail}
                width={40}
              />
              <div className="flex flex-col">
                <h3 className="text-center text-xl font-bold tracking-tight lg:text-2xl bg-clip-text py-2 text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
                  {item.title}
                </h3>
              </div>
            </CardHeader>
            <CardBody>
              <h3 className="text-md">{item.description}</h3>
            </CardBody>
          </Card>
        ))}
      </div>
      <div className="pt-20 grid grid-cols-1 justify-items-center gap-2">
        <p className="text-center text-gray-400/80 text-md">
          VxMusic app is FOSS and under GPL-3.0 license.
        </p>
        <p className="text-center text-gray-400/80 text-md">
          © 2023-2025 VxMusic - Made with ❤️ by Vishesh Gangwar
        </p>
        <div className="flex gap-4 items-center flex-wrap justify-center">
          <NextLink href="/privacy-policy" passHref>
            <span className="text-center text-gray-400/80 text-md hover:text-gray-300 cursor-pointer">
              Privacy Policy
            </span>
          </NextLink>
          <NextLink href="/terms" passHref>
            <span className="text-center text-gray-400/80 text-md hover:text-gray-300 cursor-pointer">
              Terms of Service
            </span>
          </NextLink>
          <Link
            className="text-center text-gray-400/80 text-md"
            isExternal
            showAnchorIcon
            href="https://nextjs.org/"
            color="foreground"
            anchorIcon={<TbBrandNextjs />}
          >
            Build with Next.js
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Footer;
