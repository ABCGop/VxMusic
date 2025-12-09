'use client';

import React, { useEffect, useRef } from 'react';

const Globe = () => {
    const canvasRef = useRef<HTMLCanvasElement>(null);

    useEffect(() => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        let animationId: number;
        let rotation = 0;
        
        const resize = () => {
            canvas.width = window.innerWidth;
            canvas.height = window.innerHeight;
        };
        resize(); // Call immediately

        const GLOBE_RADIUS = window.innerWidth < 600 ? 180 : 300;
        const DOT_RADIUS = 2;
        const DOT_COUNT = 800;
        const dots: { theta: number; phi: number }[] = [];

        // Distribute dots on sphere
        for (let i = 0; i < DOT_COUNT; i++) {
            const theta = Math.acos(1 - 2 * (i + 0.5) / DOT_COUNT);
            const phi = Math.PI * (1 + Math.sqrt(5)) * (i + 0.5);
            dots.push({ theta, phi });
        }

        const project = (x: number, y: number, z: number) => {
            const scale = 800 / (800 + z); // Perspective projection
            return {
                x: x * scale + canvas.width / 2,
                y: y * scale + canvas.height / 2,
                scale: scale
            };
        };

        const draw = () => {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            // ctx.fillStyle = '#000';
            // ctx.fillRect(0, 0, canvas.width, canvas.height);

            rotation += 0.003;

            dots.forEach(dot => {
                const x = GLOBE_RADIUS * Math.sin(dot.theta) * Math.cos(dot.phi + rotation);
                const y = GLOBE_RADIUS * Math.sin(dot.theta) * Math.sin(dot.phi + rotation);
                const z = GLOBE_RADIUS * Math.cos(dot.theta);

                // Rotate around X axis for tilt
                const tiltedY = y * Math.cos(0.4) - z * Math.sin(0.4);
                const tiltedZ = y * Math.sin(0.4) + z * Math.cos(0.4);

                const proj = project(x, tiltedY, tiltedZ);

                const alpha = (tiltedZ + GLOBE_RADIUS) / (2 * GLOBE_RADIUS); // Fade out back dots
                ctx.globalAlpha = Math.max(0.1, alpha);
                ctx.fillStyle = '#4f46e5'; // Indigo-600
                
                ctx.beginPath();
                ctx.arc(proj.x, proj.y, DOT_RADIUS * proj.scale, 0, Math.PI * 2);
                ctx.fill();
            });

            animationId = requestAnimationFrame(draw);
        };

        window.addEventListener('resize', resize);
        resize();
        draw();

        return () => {
            window.removeEventListener('resize', resize);
            cancelAnimationFrame(animationId);
        };
    }, []);

    return <canvas ref={canvasRef} className="fixed inset-0 z-[-1] opacity-50 bg-black" />;
};

export default Globe;
