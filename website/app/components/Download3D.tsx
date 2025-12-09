'use client';

import { Box, Float, OrbitControls, Text } from '@react-three/drei';
import { Canvas, useFrame } from '@react-three/fiber';
import { useRef, useState } from 'react';
import * as THREE from 'three';

function FloatingCube({ position, color, icon }: { position: [number, number, number], color: string, icon?: string }) {
    const mesh = useRef<THREE.Mesh>(null!);
    const [hovered, setHover] = useState(false);

    useFrame((state, delta) => {
        if (mesh.current) {
            mesh.current.rotation.x += delta * 0.2;
            mesh.current.rotation.y += delta * 0.3;
        }
    });

    return (
        <Float speed={2} rotationIntensity={0.5} floatIntensity={1}>
            <Box
                ref={mesh}
                position={position}
                args={[1.5, 1.5, 1.5]}
                onPointerOver={() => setHover(true)}
                onPointerOut={() => setHover(false)}
                scale={hovered ? 1.1 : 1}
            >
                <meshStandardMaterial color={color} roughness={0.1} metalness={0.8} opacity={0.6} transparent />
            </Box>
        </Float>
    );
}

import { Suspense } from 'react';

// ... (FloatingCube and FloatingText stay mostly same but Text font prop removed)

function FloatingText({ text, position }: { text: string, position: [number, number, number] }) {
    return (
        <Float speed={1.5} rotationIntensity={0.2} floatIntensity={0.5}>
            <Text
                position={position}
                fontSize={0.8}
                color="white"
                anchorX="center"
                anchorY="middle"
            >
                {text}
            </Text>
        </Float>
    )
}

export default function Download3D() {
    return (
        <div className="w-full h-[400px] relative z-10">
            <Canvas camera={{ position: [0, 0, 6], fov: 45 }}>
                <ambientLight intensity={0.5} />
                <pointLight position={[10, 10, 10]} intensity={2} color="#6366f1" />
                <pointLight position={[-10, -10, -10]} intensity={1} color="#3b82f6" />

                <Suspense fallback={null}>
                    <FloatingCube position={[-2, 0, 0]} color="#1a1a1a" />
                    <FloatingText text="APK" position={[-2, 0, 0]} />

                    <FloatingCube position={[2, 0, 0]} color="#1a1a1a" />
                    <FloatingText text="EXE" position={[2, 0, 0]} />
                </Suspense>

                <OrbitControls enableZoom={false} enablePan={false} autoRotate autoRotateSpeed={0.5} />
            </Canvas>
        </div>
    );
}
