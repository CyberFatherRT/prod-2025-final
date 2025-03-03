"use client";

import { useState, useEffect } from "react";

export function useAuth() {
    const [token, setToken] = useState<string | null>(null);

    useEffect(() => {
        const storedToken = localStorage.getItem("jwtToken");
        if (storedToken) {
            setToken(storedToken);
        }
    }, []);

    const login = (newToken: string) => {
        localStorage.setItem("jwtToken", newToken);
        setToken(newToken);
    };

    const logout = () => {
        localStorage.removeItem("jwtToken");
        setToken(null);
    };

    return { token, login, logout };
}
