import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export const backendDomain = "https://prod-team-13-cltnksuj.final.prodcontest.ru";

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}
