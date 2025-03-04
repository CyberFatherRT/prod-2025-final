"use client";

import type { Coworking } from "@/lib/types";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";

interface CoworkingSelectorProps {
    coworkings: Coworking[];
    selectedCoworking: Coworking | null;
    onSelectCoworking: (coworkingId: string) => void;
}

export default function CoworkingSelector({
    coworkings,
    selectedCoworking,
    onSelectCoworking,
}: CoworkingSelectorProps) {
    return (
        <div className="flex items-center gap-2">
            <span className="text-sm font-medium">Coworking:</span>
            <Select
                value={selectedCoworking?.id || ""}
                onValueChange={onSelectCoworking}
            >
                <SelectTrigger className="w-[180px]">
                    <SelectValue placeholder="Select coworking" />
                </SelectTrigger>
                <SelectContent>
                    {coworkings.map((coworking) => (
                        <SelectItem key={coworking.id} value={coworking.id}>
                            {coworking.address}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
        </div>
    );
}
