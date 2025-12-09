'use client';

import { MeshDistortMaterial, Sphere } from '@react-three/drei';
import { Canvas, useFrame } from '@react-three/fiber';
import { useRef } from 'react';
import * as THREE from 'three';

function AnimatedSphere() {
    const meshRef = useRef<THREE.Mesh>(null!);

    useFrame((state) => {
        const t = state.clock.getElapsedTime();
        if (meshRef.current) {
            meshRef.current.rotation.x = t * 0.1;
            meshRef.current.rotation.y = t * 0.15;
        }
    });

    return (
        <Sphere args={[1, 100, 200]} scale={2} ref={meshRef}>
            <MeshDistortMaterial
                color="#6366f1"
                attach="material"
                distort={0.5}
                speed={1.5}
                roughness={0.2}
                metalness={0.8}
                wireframe
            />
        </Sphere>
    );
}

export default function ThreeHero() {
    return (
        <div className="absolute inset-0 z-0 opacity-40 mix-blend-screen pointer-events-none">
            <Canvas camera={{ position: [0, 0, 5] }}>
                <ambientLight intensity={0.5} />
                <pointLight position={[10, 10, 10]} intensity={1.5} />
                <AnimatedSphere />
            </Canvas>
        </div>
    );
}
