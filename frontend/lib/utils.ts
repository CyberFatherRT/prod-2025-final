import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export const backendDomain = "http://localhost:8000";

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}
