import {
  Avatar,
  Button,
  Card,
  CardBody,
  CardHeader,
  Link
} from "@nextui-org/react";
import React from "react";
import { FaGithub } from "react-icons/fa";

const AboutSection = () => {
  return (
    <section className="download-section ">
      <div className="relative isolate px-6 py-24 lg:px-8">
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
        <div className="mx-auto place-items-center py-21 sm:py-35 lg:py-42 grid grid-cols-1 gap-6">
          <h2 className="scroll-m-20 text-4xl font-bold tracking-tight lg:text-5xl pb-8 bg-clip-text text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
            About us
          </h2>
          <Card className="max-w-[600px] px-10 py-10 bg-transparent">
            <CardHeader className="flex gap-3 justify-between">
              <div className="flex gap-5">
                <Avatar src="image.jpg" className="w-20 h-20 text-large" />
                <div className="flex flex-col justify-center">
                  <h3 className="text-lg font-semibold">Vishesh Gangwar</h3>
                  <p className="text-md text-default-500">
                    ABCGop
                  </p>
                </div>
              </div>
              <div className="flex justify-end gap-6">
                <Button
                  href="https://github.com/ABCGop"
                  rel="noopener noreferrer"
                  target="_blank"
                  color="default"
                  as={Link}
                  isIconOnly
                  size="md"
                  radius="lg"
                >
                  <FaGithub size={21} />
                </Button>
              </div>
            </CardHeader>
            <CardBody>
              <p className="text-md font-semibold">
               Hey! I&apos;m Vishesh Gangwar, an 11th class student studying PCM + Computer Science at KV AFS Bareilly. I have a passion for web development and App development.
              </p>
            </CardBody>
          </Card>
        </div>
      </div>
    </section>
  );
};

export default AboutSection;
