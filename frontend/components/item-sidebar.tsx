"use client";

import type React from "react";
import type { ItemType } from "@/lib/types";
import { ScrollArea } from "@/components/ui/scroll-area";

interface ItemSidebarProps {
    itemTypes: ItemType[];
}

export default function ItemSidebar({ itemTypes }: ItemSidebarProps) {
    return (
        <div className="w-64 border-r bg-muted/20 flex flex-col">
            <div className="p-4 border-b">
                <h2 className="font-semibold">Item Types</h2>
            </div>
            <ScrollArea className="flex-1">
                <div className="p-4 grid gap-4">
                    {itemTypes.map((itemType) => (
                        <DraggableItem key={itemType.id} itemType={itemType} />
                    ))}
                </div>
            </ScrollArea>
        </div>
    );
}

interface DraggableItemProps {
    itemType: ItemType;
}

function DraggableItem({ itemType }: DraggableItemProps) {
    const handleDragStart = (e: React.DragEvent) => {
        e.dataTransfer.setData(
            "application/json",
            JSON.stringify({
                item_id: itemType.id,
                name: itemType.name,
                color: itemType.color,
                offsets: itemType.offsets,
            }),
        );
    };

    return (
        <div className="border rounded-md p-3 bg-background cursor-grab hover:border-primary transition-colors" draggable onDragStart={handleDragStart}>
            <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded-sm flex-shrink-0" style={{ backgroundColor: itemType.color }} />
                <span className="font-medium">{itemType.name}</span>
            </div>
            <div className="mt-2">
                <ItemPreview itemType={itemType} />
            </div>
        </div>
    );
}

function ItemPreview({ itemType }: { itemType: ItemType }) {
    // Calculate the dimensions needed for the preview
    const offsets = itemType.offsets || [[0, 0]];
    const xValues = offsets.map(([x]) => x);
    const yValues = offsets.map(([, y]) => y);

    const minX = Math.min(0, ...xValues);
    const maxX = Math.max(0, ...xValues);
    const minY = Math.min(0, ...yValues);
    const maxY = Math.max(0, ...yValues);

    const width = maxX - minX + 1;
    const height = maxY - minY + 1;

    // Create a grid for preview
    const grid = Array(height)
        .fill(0)
        .map(() => Array(width).fill(false));

    // Mark cells that are part of the item
    offsets.forEach(([x, y]) => {
        const adjustedX = x - minX;
        const adjustedY = y - minY;
        grid[adjustedY][adjustedX] = true;
    });

    const cellSize = 16;

    return (
        <div className="flex justify-center">
            <div
                className="grid gap-px bg-muted"
                style={{
                    gridTemplateColumns: `repeat(${width}, ${cellSize}px)`,
                    gridTemplateRows: `repeat(${height}, ${cellSize}px)`,
                }}
            >
                {grid.flat().map((isActive, index) => (
                    <div
                        key={index}
                        className={`w-full h-full ${isActive ? "" : "bg-background"}`}
                        style={{
                            backgroundColor: isActive ? itemType.color : "",
                            width: `${cellSize}px`,
                            height: `${cellSize}px`,
                        }}
                    />
                ))}
            </div>
        </div>
    );
}
