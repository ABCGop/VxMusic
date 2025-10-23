"use client";
import Link from "next/link";
import React from "react";
import { MdDownload } from "react-icons/md";
import Download from "./Download";

const DownloadInfoSection = () => {
  return (
    // <section className="download-section">
    //   <div className="relative isolate pt-14">
    //     <Download />
    //     <h3 className="text-center scroll-m-20 text-4xl font-bold tracking-tight lg:text-5xl bg-clip-text py-10 text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
    //       Download VxMusic to your Android Device now to get the best
    //       experience for streaming music now!
    //     </h3>
    //   </div>
    //   </section>
    <section className="download-section bg-muted/20">
      <div className="relative isolate px-6 py-28 lg:px-8">
        <div
          className="absolute inset-x-0 -z-10 transform-gpu overflow-hidden blur-3xl"
          aria-hidden="true"
        >
          <div
            className="relative right-[calc(50%-15rem)] aspect-[1155/678] w-[36.125rem] -translate-x-1/2 rotate-[30deg] bg-gradient-to-br from-primary/30 to-accent/30 opacity-30 sm:left-[calc(50%-30rem)] sm:w-[72.1875rem]"
            style={{
              clipPath:
                "polygon(57% 25%, 70% 31%, 77% 45%, 66% 56%, 43% 55%, 35% 41%, 41% 29%)",
            }}
          ></div>
        </div>
        <div className="mx-auto place-items-center py-14 sm:py-21 lg:py-28 grid gap-7 grid-cols-1 md:grid-cols-2 lg:grid-cols-2">
          <div className="card-elevated">
            <Download />
          </div>
          <div className="flex flex-col justify-center">
            <div>
              <h2 className="scroll-m-20 text-4xl font-bold tracking-tight lg:text-5xl pb-8 gradient-text">
                Be a VxMusic-er
              </h2>
              <h4 className="scroll-m-20 text-xl font-semibold tracking-tight text-muted-foreground">
                Download VxMusic to your Android Device now to get the best
                experience for streaming music now!
              </h4>
            </div>
            <div className="pt-8">
              <Link
                href="/download"
                className="inline-flex items-center gap-2 bg-primary hover:bg-primary-hover text-primary-foreground px-8 py-3 rounded-xl font-semibold text-lg shadow-lg hover:shadow-xl transition-all duration-300 hover:-translate-y-1"
              >
                <span className="font-semibold">Download</span>
                <MdDownload className="w-5 h-5" />
              </Link>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default DownloadInfoSection;
