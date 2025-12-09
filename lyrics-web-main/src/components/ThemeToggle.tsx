"use client";

import * as React from "react";
import { useTheme } from "next-themes";
import { Moon, Sun, Laptop } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";

export function ThemeToggle() {
    const { setTheme, theme, resolvedTheme } = useTheme();
    const [mounted, setMounted] = React.useState(false);

    React.useEffect(() => {
        setMounted(true);
    }, []);

    // Avoid hydration mismatch
    const current = mounted ? (theme === "system" ? `system(${resolvedTheme})` : theme) : "system";

    return (
        <>
            <DropdownMenu>
                <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon">
                        {
                            current === "light" ? <Sun className="size-4" /> : current === "dark" ? <Moon className="size-4" /> : <Laptop className="size-4" />
                        }
                    </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-fit">
                    <DropdownMenuItem asChild className="w-fit">
                        <Button
                            variant="ghost"
                            size="sm"
                            aria-label="Use light theme"
                            title="Light"
                            onClick={() => setTheme("light")}
                        >
                            <Sun className="size-4" /> Light
                        </Button>
                    </DropdownMenuItem>
                    <DropdownMenuItem asChild className="w-fit">
                        <Button
                            variant="ghost"
                            size="sm"
                            aria-label="Use dark theme"
                            title="Dark"
                            onClick={() => setTheme("dark")}
                        >
                            <Moon className="size-4" /> Dark
                        </Button>
                    </DropdownMenuItem>
                    <DropdownMenuItem asChild className="w-fit">
                        <Button
                            variant="ghost"
                            size="sm"
                            aria-label="Use system theme"
                            title="System"
                            onClick={() => setTheme("system")}
                        >
                            <Laptop className="size-4" /> System
                        </Button>
                    </DropdownMenuItem>
                </DropdownMenuContent>
            </DropdownMenu>
        </>
    );
}