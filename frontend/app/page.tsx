import { Suspense } from "react";
import CoworkingAdmin from "@/components/coworking-admin";

export default function Home() {
    return (
        <main className="flex min-h-screen flex-col">
            <Suspense fallback={<div>Loading...</div>}>
                <CoworkingAdmin />
            </Suspense>
        </main>
    );
}
