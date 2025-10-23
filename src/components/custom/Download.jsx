"use client";
import { animate } from "framer-motion";
import React, { useEffect, useRef } from "react";

export default function Download({ downloadCount = 1354 }) {
  const nodeRef = useRef();

  useEffect(() => {
    const node = nodeRef.current;

    const controls = animate(0, downloadCount, {
      duration: 1,
      onUpdate(value) {
        node.textContent = Intl.NumberFormat("en-US").format(
          value.toFixed(1)
        );
      },
    });

    return () => controls.stop();
  }, [downloadCount]);

  return (
    <div className="text-center">
      <h1
        className="scroll-m-20 text-4xl font-bold tracking-tight lg:text-5xl gradient-text py-10"
        ref={nodeRef}
      />
      <h1 className="text-center text-2xl font-semibold tracking-tight lg:text-3xl gradient-text py-2">
        Downloads
      </h1>
    </div>
  );
}
