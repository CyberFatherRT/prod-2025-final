"use client";

import type React from "react";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { backendDomain } from "@/lib/utils";

interface LoginProps {
    onLogin: (token: string) => void;
}

export default function Login({ onLogin }: LoginProps) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [companyDomain, setCompanyDomain] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            console.log(`${backendDomain}/user/login`);
            const response = await fetch(`${backendDomain}/user/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    domain: companyDomain,
                    email: email,
                    password,
                }),
            });

            const contentType = response.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                if (response.ok) {
                    const { jwt } = await response.json();
                    onLogin(jwt);
                } else {
                    const { error } = await response.json();
                    alert(error || "Login failed");
                }
            } else {
                // Response is not JSON
                const text = await response.text();
                console.error("Unexpected response:", text);
                alert("Received an unexpected response from the server. Please try again later.");
            }
        } catch (error) {
            console.error("Login error:", error);
            alert("An error occurred during login. Please try again.");
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-background">
            <div className="w-full max-w-md space-y-8">
                <div className="text-center">
                    <h2 className="mt-6 text-3xl font-bold tracking-tight">Sign in to your account</h2>
                    <p className="mt-2 text-sm text-muted-foreground">Enter your credentials to access the Coworking Admin</p>
                </div>
                <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
                    <div className="space-y-4 rounded-md shadow-sm">
                        <div>
                            <Label htmlFor="company-domain">Company Domain</Label>
                            <Input
                                id="company-domain"
                                name="company-domain"
                                type="text"
                                required
                                value={companyDomain}
                                onChange={(e) => setCompanyDomain(e.target.value)}
                                placeholder="Enter your company domain"
                            />
                        </div>
                        <div>
                            <Label htmlFor="email">Email</Label>
                            <Input
                                id="email"
                                name="email"
                                type="email"
                                required
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="Enter your email"
                            />
                        </div>
                        <div>
                            <Label htmlFor="password">Password</Label>
                            <Input
                                id="password"
                                name="password"
                                type="password"
                                required
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="Enter your password"
                            />
                        </div>
                    </div>

                    <div>
                        <Button type="submit" className="w-full">
                            Sign in
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
}
