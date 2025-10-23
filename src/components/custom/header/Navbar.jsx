"use client";
import {
  Button,
  Link,
  NavbarBrand,
  NavbarContent,
  NavbarItem,
  NavbarMenu,
  NavbarMenuItem,
  NavbarMenuToggle,
  Navbar as NavbarNext,
} from "@nextui-org/react";
import { usePathname } from "next/navigation";
import * as React from "react";
import { useEffect, useState } from "react";
import ThemeSwitcher from "../ThemeSwitcher";
import Logo from "./Logo";

async function fetcher() {
  const res = await fetch(
    "https://api.github.com/repos/ABCGop/VxMusic?page=1&per_page=1"
  );
  if (!res.ok) {
    throw new Error(res.statusText);
  }
  return res.json();
}
let formatter = Intl.NumberFormat('en', { notation: 'compact' });

export default function Navbar() {
  const [starCount, setStarCount] = useState("N/A");
  const [isMenuOpen, setIsMenuOpen] = React.useState(false);
  const [scroll, setScroll] = React.useState(false);
  const pathName = usePathname();

  useEffect(() => {
    fetcher().then((data) => {
      setStarCount(data.stargazers_count);
    });
  }, []);

  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 50) {
        setScroll(true);
      } else {
        setScroll(false);
      }
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const menus = [
    { title: "Home", path: "/" },
    { title: "Blog", path: "/blog" },
    { title: "Download", path: "/download" },
    { title: "About Us", path: "/about" },
  ];

  return (
    <NavbarNext
      isBordered={false}
      isMenuOpen={isMenuOpen}
      maxWidth="2xl"
      className={scroll ? "" : "bg-transparent"}
      onMenuOpenChange={setIsMenuOpen}
    >
      <NavbarContent className="md:hidden" justify="start">
        <NavbarMenuToggle
          aria-label={isMenuOpen ? "Close menu" : "Open menu"}
        />
      </NavbarContent>

      <NavbarContent className="md:hidden pr-3" justify="center">
        <NavbarBrand>
          <Logo />
        </NavbarBrand>
      </NavbarContent>

      <NavbarContent className="hidden md:flex gap-4" justify="end">
        <NavbarBrand className="" justify="start">
          <Logo />
        </NavbarBrand>
        {menus.map((menu, index) => (
          <NavbarItem key={index} isActive={pathName === menu.path}>
            <Button
              color="default"
              variant={pathName === menu.path ? "solid" : "light"}
              as={Link}
              size="md"
              href={menu.path}
            >
              <p className="font-semibold">{menu.title}</p>
            </Button>
          </NavbarItem>
        ))}
        <NavbarItem>
        </NavbarItem>
        <NavbarItem>
          <ThemeSwitcher></ThemeSwitcher>
        </NavbarItem>
      </NavbarContent>

      <NavbarMenu>
        {menus.map((item, index) => (
          <NavbarMenuItem
            key={`${item}-${index}`}
            isActive={pathName === item.path}
          >
            <Link
              className="w-full"
              color={"foreground"}
              href={item.path}
              size="lg"
            >
              <p
                className={
                  pathName === item.path ? "font-bold" : "font-semibold"
                }
              >
                {item.title}
              </p>
            </Link>
          </NavbarMenuItem>
        ))}
      </NavbarMenu>
    </NavbarNext>
  );

  //   return (
  //     <nav
  //       className={`w-full fixed top-0 z-10 border-b md:border-0 ${
  //         scroll ? "bg-navcolor" : "bg-transparent"
  //       }`}
  //     >
  //       <div className="items-center px-4 max-w-screen-xl mx-auto md:flex md:px-8">
  //         <div className="flex items-center justify-between py-3 md:py-5 md:block">
  //           <div className="flex flex-row">
  //             <div className="mr-3">
  //               <Logo />
  //             </div>
  //             <Link href="/">
  //               <h1 className="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-gradientstart/60 to-50% to-gradientend/60">
  //                 VxMusic
  //               </h1>
  //             </Link>
  //           </div>
  //           <div className="md:hidden">
  //             <button
  //               className="text-gray-700 outline-none p-2 rounded-md focus:border-gray-400 focus:border"
  //               onClick={() => {
  //                 setState(!state);
  //               }}
  //             >
  //               <Menu />
  //             </button>
  //           </div>
  //         </div>
  //         <div
  //           className={`flex-1 justify-self-center pb-3 mt-8 md:block md:pb-0 md:mt-0 ${
  //             state ? "block" : "hidden"
  //           }`}
  //         >
  //           <ul
  //             className={`items-center space-y-8 md:flex md:space-x-6 md:space-y-0 ${
  //               state ? "" : "float-right"
  //             }`}
  //           >
  //             {menus.map((menu, index) => (
  //               <li
  //                 key={index}
  //                 className={`${state ? "flex items-center justify-center" : ""}`}
  //               >
  //                 <Button variant="ghost" asChild>
  //                   <Link href={menu.path}>{menu.title}</Link>
  //                 </Button>
  //               </li>
  //             ))}
  //             <li
  //               className={`${state ? "flex items-center justify-center" : ""}`}
  //             >

  //             </li>
  //           </ul>
  //         </div>
  //       </div>
  //     </nav>
  //   );
  // }
}
