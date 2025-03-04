"use client";

import type { Building } from "@/lib/types";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

interface BuildingSelectorProps {
    buildings: Building[];
    selectedBuilding: Building | null;
    onSelectBuilding: (buildingId: string) => void;
}

export default function BuildingSelector({ buildings, selectedBuilding, onSelectBuilding }: BuildingSelectorProps) {
    console.log(selectedBuilding, buildings);
    return (
        <div className="flex items-center gap-2">
            <span className="text-sm font-medium">Building:</span>
            <Select value={selectedBuilding?.id || ""} onValueChange={onSelectBuilding}>
                <SelectTrigger className="w-[180px]">
                    <SelectValue placeholder="Select building" />
                </SelectTrigger>
                <SelectContent>
                    {buildings.map((building) => (
                        <SelectItem key={building.id} value={building.id}>
                            {building.address}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
        </div>
    );
}
