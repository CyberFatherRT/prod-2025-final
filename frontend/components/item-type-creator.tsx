"use client";

import { useState } from "react";
import { v4 as uuidv4 } from "uuid";
import type { ItemType } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "./ui/checkbox";

interface ItemTypeCreatorProps {
    onAddItemType: (itemType: ItemType) => void;
}

export default function ItemTypeCreator({ onAddItemType }: ItemTypeCreatorProps) {
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [bookable, setBookable] = useState(true);
    const [color, setColor] = useState("#4f46e5");
    const [gridSize, setGridSize] = useState({ rows: 5, cols: 5 });
    const [selectedCells, setSelectedCells] = useState<[number, number][]>([[0, 0]]);

    const calculateOffsets = (): [number, number][] => {
        if (selectedCells.length === 0) return [[0, 0]];

        const minX = Math.min(...selectedCells.map(([x]) => x));
        const minY = Math.min(...selectedCells.map(([, y]) => y));

        return selectedCells.map(([x, y]) => [x - minX, y - minY]);
    };

    const handleCellClick = (row: number, col: number) => {
        const cellCoord: [number, number] = [col, row];
        const cellIndex = selectedCells.findIndex(([x, y]) => x === cellCoord[0] && y === cellCoord[1]);

        if (cellIndex >= 0) {
            const newSelectedCells = [...selectedCells];
            newSelectedCells.splice(cellIndex, 1);
            setSelectedCells(newSelectedCells.length > 0 ? newSelectedCells : [[0, 0]]);
        } else {
            setSelectedCells([...selectedCells, cellCoord]);
        }
    };

    const handleSave = () => {
        if (!name.trim()) {
            alert("Please enter a name for the item type");
            return;
        }

        if (selectedCells.length === 0) {
            alert("Please select at least one cell");
            return;
        }

        const newItemType: ItemType = {
            id: uuidv4(),
            name: name.trim(),
            color,
            bookable,
            description: description.trim(),
            offsets: calculateOffsets(),
        };

        onAddItemType(newItemType);

        setName("");
        setColor("#4f46e5");
        setGridSize({ rows: 5, cols: 5 });
        setSelectedCells([[0, 0]]);
    };

    const handleGridSizeChange = (dimension: "rows" | "cols", value: string) => {
        const numValue = Number.parseInt(value, 10);
        if (isNaN(numValue) || numValue < 1) return;

        setGridSize((prev) => ({ ...prev, [dimension]: numValue }));

        setSelectedCells((prev) => prev.filter(([x, y]) => x < numValue && y < (dimension === "rows" ? numValue : gridSize.rows)));
    };

    return (
        <div className="grid md:grid-cols-2 gap-8">
            <div>
                <div className="space-y-4">
                    <h2 className="text-xl font-bold">Create New Item Type</h2>
                    <p className="text-muted-foreground">Define a new item type by selecting cells in the grid and providing details.</p>
                </div>

                <div className="space-y-6">
                    <div className="space-y-4 pt-4">
                        <div className="grid gap-2">
                            <Label htmlFor="name">Name</Label>
                            <Input id="name" maxLength={10} value={name} onChange={(e) => setName(e.target.value)} placeholder="Enter item type name" />
                        </div>

                        <div className="grid gap-2">
                            <Label htmlFor="description">Description</Label>
                            <Input
                                id="description"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                placeholder="Enter item type description"
                            />
                        </div>

                        <div className="grid gap-2">
                            <Label htmlFor="color">Color</Label>
                            <div className="flex gap-2">
                                <Input id="color" type="color" value={color} onChange={(e) => setColor(e.target.value)} className="w-16 h-10 p-1" />
                                <Input value={color} onChange={(e) => setColor(e.target.value)} placeholder="#RRGGBB" className="flex-1" />
                            </div>
                        </div>

                        <div className="grid gap-2">
                            <Label>Grid Size</Label>
                            <div className="flex items-center gap-4">
                                <div className="flex-1">
                                    <Label htmlFor="grid-width" className="text-sm">
                                        Width
                                    </Label>
                                    <Input
                                        id="grid-width"
                                        type="number"
                                        min="1"
                                        value={gridSize.cols}
                                        onChange={(e) => handleGridSizeChange("cols", e.target.value)}
                                    />
                                </div>
                                <div className="flex-1">
                                    <Label htmlFor="grid-height" className="text-sm">
                                        Height
                                    </Label>
                                    <Input
                                        id="grid-height"
                                        type="number"
                                        min="1"
                                        value={gridSize.rows}
                                        onChange={(e) => handleGridSizeChange("rows", e.target.value)}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="flex items-center space-x-2">
                        <Checkbox id="terms" checked={bookable} onCheckedChange={() => setBookable(!bookable)} />
                        <label htmlFor="terms" className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                            Is bookable
                        </label>
                    </div>

                    <div className="space-y-4">
                        <div>
                            <Label>Shape</Label>
                            <p className="text-sm text-muted-foreground mt-1">Click on cells in the grid to define the shape of the item.</p>
                        </div>

                        <div className="border rounded-md p-4 bg-muted/20 overflow-auto max-h-[60vh]">
                            <div
                                className="grid gap-px bg-muted"
                                style={{
                                    gridTemplateColumns: `repeat(${gridSize.cols}, minmax(0, 1fr))`,
                                    width: `${gridSize.cols * 2}rem`,
                                }}
                            >
                                {Array.from({ length: gridSize.rows }).map((_, rowIndex) =>
                                    Array.from({
                                        length: gridSize.cols,
                                    }).map((_, colIndex) => {
                                        const isSelected = selectedCells.some(([x, y]) => x === colIndex && y === rowIndex);

                                        return (
                                            <div
                                                key={`${rowIndex}-${colIndex}`}
                                                className={` w-8 h-8 cursor-pointer flex items-center justify-center transition-colors border border-muted-foreground/20 ${isSelected ? "bg-primary" : "hover:bg-muted"} `}
                                                style={{
                                                    backgroundColor: isSelected ? color : "",
                                                }}
                                                onClick={() => handleCellClick(rowIndex, colIndex)}
                                            >
                                                {isSelected && (
                                                    <span className="text-xs font-mono text-primary-foreground">
                                                        {selectedCells.findIndex(([x, y]) => x === colIndex && y === rowIndex)}
                                                    </span>
                                                )}
                                            </div>
                                        );
                                    }),
                                )}
                            </div>
                        </div>
                    </div>

                    <Button onClick={handleSave} disabled={!name.trim() || selectedCells.length === 0}>
                        Save Item Type
                    </Button>
                </div>
            </div>

            <div className="space-y-6">
                <div>
                    <h3 className="text-lg font-semibold">Preview</h3>
                    <p className="text-sm text-muted-foreground">This is how your item will look in the coworking editor.</p>
                </div>

                <div className="border rounded-md p-6 bg-muted/20">
                    <div className="flex flex-col items-center gap-4">
                        <div className="text-center">
                            <h4 className="font-medium">{name || "New Item Type"}</h4>
                            <p className="text-sm text-muted-foreground">
                                {selectedCells.length} cell
                                {selectedCells.length !== 1 ? "s" : ""}
                            </p>
                        </div>

                        <ItemPreview color={color} offsets={calculateOffsets()} />
                    </div>
                </div>
            </div>
        </div>
    );
}

interface ItemPreviewProps {
    color: string;
    offsets: [number, number][];
}

function ItemPreview({ color, offsets }: ItemPreviewProps) {
    const xValues = offsets.map(([x]) => x);
    const yValues = offsets.map(([, y]) => y);

    const minX = Math.min(0, ...xValues);
    const maxX = Math.max(0, ...xValues);
    const minY = Math.min(0, ...yValues);
    const maxY = Math.max(0, ...yValues);

    const width = maxX - minX + 1;
    const height = maxY - minY + 1;

    const grid = Array(height)
        .fill(0)
        .map(() => Array(width).fill(false));

    offsets.forEach(([x, y]) => {
        const adjustedX = x - minX;
        const adjustedY = y - minY;
        grid[adjustedY][adjustedX] = true;
    });

    const cellSize = 32;

    return (
        <div className="flex justify-center p-4 bg-background rounded-md border">
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
                            backgroundColor: isActive ? color : "",
                            width: `${cellSize}px`,
                            height: `${cellSize}px`,
                        }}
                    />
                ))}
            </div>
        </div>
    );
}
