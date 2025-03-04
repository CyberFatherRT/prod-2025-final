"use client";

import type React from "react";

import { useRef, useState, useEffect, useCallback } from "react";
import { v4 as uuidv4 } from "uuid";
import type { ItemType, CoworkingItem, Position } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Trash2 } from "lucide-react";

interface CoworkingGridProps {
    rows: number;
    cols: number;
    items: CoworkingItem[];
    itemTypes: ItemType[];
    onAddItem: (item: CoworkingItem) => void;
    onUpdateItem: (item: CoworkingItem) => void;
    onRemoveItem: (itemId: string) => void;
}

export default function CoworkingGrid({ rows, cols, items, itemTypes, onAddItem, onUpdateItem, onRemoveItem }: CoworkingGridProps) {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const containerRef = useRef<HTMLDivElement>(null);

    const [scale, setScale] = useState(1);
    const [offset, setOffset] = useState({ x: 0, y: 0 });
    const [isDraggingView, setIsDraggingView] = useState(false);
    const [dragStart, setDragStart] = useState({ x: 0, y: 0 });

    const [draggedItem, setDraggedItem] = useState<{
        item_id: string;
        position: Position;
        isNew: boolean;
        itemId?: string;
    } | null>(null);

    const [hoveredCell, setHoveredCell] = useState<Position | null>(null);
    const [cellSize, setCellSize] = useState(50);

    const [selectedItem, setSelectedItem] = useState<CoworkingItem | null>(null);
    const [contextMenuPosition, setContextMenuPosition] = useState<{
        x: number;
        y: number;
    } | null>(null);

    const gridWidth = cols * cellSize;
    const gridHeight = rows * cellSize;

    // Draw the grid and items
    const drawGrid = useCallback(() => {
        const canvas = canvasRef.current;
        if (!canvas) return;

        const ctx = canvas.getContext("2d");
        if (!ctx) return;

        // Clear the canvas
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Apply transformations for pan and zoom
        ctx.save();
        ctx.translate(offset.x, offset.y);
        ctx.scale(scale, scale);

        // Draw grid background
        ctx.fillStyle = "#f9fafb"; // Light gray background
        ctx.fillRect(0, 0, gridWidth, gridHeight);

        // Draw grid lines
        ctx.strokeStyle = "#e5e7eb"; // Light gray lines
        ctx.lineWidth = 1 / scale; // Adjust line width for zoom

        // Draw vertical lines
        for (let x = 0; x <= cols; x++) {
            ctx.beginPath();
            ctx.moveTo(x * cellSize, 0);
            ctx.lineTo(x * cellSize, gridHeight);
            ctx.stroke();
        }

        // Draw horizontal lines
        for (let y = 0; y <= rows; y++) {
            ctx.beginPath();
            ctx.moveTo(0, y * cellSize);
            ctx.lineTo(gridWidth, y * cellSize);
            ctx.stroke();
        }

        // Draw placed items
        items.forEach((item) => {
            const itemType = itemTypes.find((type) => type.id === item.item_id);
            if (!itemType) return;

            drawItem(ctx, item.base_point, itemType, item.id === draggedItem?.itemId ? 0.5 : 1);
        });

        // Draw currently dragged item if any
        if (draggedItem && hoveredCell) {
            const itemType = itemTypes.find((type) => type.id === draggedItem.item_id);
            if (itemType) {
                drawItem(ctx, hoveredCell, itemType, 0.7);
            }
        }

        // Highlight selected item
        if (selectedItem) {
            const itemType = itemTypes.find((type) => type.id === selectedItem.item_id);
            if (itemType) {
                ctx.strokeStyle = "red";
                ctx.lineWidth = 2 / scale;
                const { x, y } = selectedItem.base_point;
                itemType.offsets.forEach(([offsetX, offsetY]) => {
                    const cellX = (x + offsetX) * cellSize;
                    const cellY = (y + offsetY) * cellSize;
                    ctx.strokeRect(cellX, cellY, cellSize, cellSize);
                });
            }
        }

        ctx.restore();
    }, [rows, cols, items, itemTypes, scale, offset, draggedItem, hoveredCell, cellSize, gridWidth, gridHeight, selectedItem]);

    // Draw a single item
    const drawItem = (ctx: CanvasRenderingContext2D, position: Position, itemType: ItemType, opacity = 1) => {
        const { x, y } = position;
        const offsets = itemType.offsets || [[0, 0]];

        ctx.fillStyle = itemType.color;
        ctx.globalAlpha = opacity;

        offsets.forEach(([offsetX, offsetY]) => {
            const cellX = (x + offsetX) * cellSize;
            const cellY = (y + offsetY) * cellSize;

            // Check if cell is within grid bounds
            if (x + offsetX >= 0 && x + offsetX < cols && y + offsetY >= 0 && y + offsetY < rows) {
                ctx.fillRect(cellX, cellY, cellSize, cellSize);

                // Draw cell border
                ctx.strokeStyle = "rgba(0, 0, 0, 0.2)";
                ctx.lineWidth = 1 / scale;
                ctx.strokeRect(cellX, cellY, cellSize, cellSize);
            }
        });

        ctx.globalAlpha = 1;
    };

    // Check if an item can be placed at a position
    const canPlaceItem = (position: Position, item_id: string, excludeItemId?: string): boolean => {
        const itemType = itemTypes.find((type) => type.id === item_id);
        if (!itemType) return false;

        const offsets = itemType.offsets || [[0, 0]];

        // Check if all cells are within grid bounds
        const allCellsInBounds = offsets.every(([offsetX, offsetY]) => {
            const x = position.x + offsetX;
            const y = position.y + offsetY;
            return x >= 0 && x < cols && y >= 0 && y < rows;
        });

        if (!allCellsInBounds) return false;

        // Check for collisions with other items
        const occupiedCells = new Set<string>();

        items.forEach((item) => {
            // Skip the item being dragged if it's an existing item
            if (excludeItemId && item.id === excludeItemId) return;

            const itemTypeForPlaced = itemTypes.find((type) => type.id === item.item_id);
            if (!itemTypeForPlaced) return;

            const offsetsForPlaced = itemTypeForPlaced.offsets || [[0, 0]];

            offsetsForPlaced.forEach(([offsetX, offsetY]) => {
                const x = item.base_point.x + offsetX;
                const y = item.base_point.y + offsetY;
                occupiedCells.add(`${x},${y}`);
            });
        });

        // Check if any cell of the new item is already occupied
        return !offsets.some(([offsetX, offsetY]) => {
            const x = position.x + offsetX;
            const y = position.y + offsetY;
            return occupiedCells.has(`${x},${y}`);
        });
    };

    // Convert screen coordinates to grid coordinates
    const screenToGrid = (screenX: number, screenY: number): Position => {
        const x = Math.floor((screenX - offset.x) / (cellSize * scale));
        const y = Math.floor((screenY - offset.y) / (cellSize * scale));
        return { x, y };
    };

    // Handle mouse wheel for zooming
    const handleWheel = (e: React.WheelEvent) => {
        e.preventDefault();

        const delta = -e.deltaY;
        const factor = delta > 0 ? 1.1 : 0.9;

        // Get mouse position relative to canvas
        const rect = canvasRef.current?.getBoundingClientRect();
        if (!rect) return;

        const mouseX = e.clientX - rect.left;
        const mouseY = e.clientY - rect.top;

        // Calculate new scale
        const newScale = Math.max(0.2, Math.min(3, scale * factor));

        // Adjust offset to zoom toward mouse position
        const newOffsetX = mouseX - (mouseX - offset.x) * (newScale / scale);
        const newOffsetY = mouseY - (mouseY - offset.y) * (newScale / scale);

        setScale(newScale);
        setOffset({ x: newOffsetX, y: newOffsetY });
    };

    // Handle mouse down for panning or item selection
    const handleMouseDown = (e: React.MouseEvent) => {
        const canvas = canvasRef.current;
        if (!canvas) return;

        const rect = canvas.getBoundingClientRect();
        const mouseX = e.clientX - rect.left;
        const mouseY = e.clientY - rect.top;

        // Check if clicking on an item
        const gridPos = screenToGrid(mouseX, mouseY);
        const clickedItem = findItemAtPosition(gridPos);

        if (clickedItem) {
            if (e.button === 2) {
                // Right-click
                e.preventDefault();
                setSelectedItem(clickedItem);
                setContextMenuPosition({ x: e.clientX, y: e.clientY });
            } else {
                // Start dragging existing item
                setDraggedItem({
                    item_id: clickedItem.item_id,
                    position: clickedItem.base_point,
                    isNew: false,
                    itemId: clickedItem.id,
                });
            }
        } else {
            // Start panning the view
            setIsDraggingView(true);
            setDragStart({ x: mouseX - offset.x, y: mouseY - offset.y });
        }
    };

    // Find an item at a specific grid position
    const findItemAtPosition = (position: Position): CoworkingItem | null => {
        for (const item of items) {
            const itemType = itemTypes.find((type) => type.id === item.item_id);
            if (!itemType) continue;

            const offsets = itemType.offsets || [[0, 0]];

            for (const [offsetX, offsetY] of offsets) {
                if (item.base_point.x + offsetX === position.x && item.base_point.y + offsetY === position.y) {
                    return item;
                }
            }
        }

        return null;
    };

    // Handle mouse move for panning or item dragging
    const handleMouseMove = (e: React.MouseEvent) => {
        const canvas = canvasRef.current;
        if (!canvas) return;

        const rect = canvas.getBoundingClientRect();
        const mouseX = e.clientX - rect.left;
        const mouseY = e.clientY - rect.top;

        // Update hovered cell
        const gridPos = screenToGrid(mouseX, mouseY);
        setHoveredCell(gridPos);

        if (isDraggingView) {
            // Pan the view
            setOffset({
                x: mouseX - dragStart.x,
                y: mouseY - dragStart.y,
            });
        }
    };

    // Handle mouse up to finish panning or place item
    const handleMouseUp = () => {
        if (isDraggingView) {
            setIsDraggingView(false);
        }

        if (draggedItem && hoveredCell) {
            // Try to place the item
            const canPlace = canPlaceItem(hoveredCell, draggedItem.item_id, draggedItem.isNew ? undefined : draggedItem.itemId);
            const foo = itemTypes.filter((itemType) => itemType.id === draggedItem.item_id)[0];

            if (canPlace) {
                if (draggedItem.isNew) {
                    // Add new item
                    const bar = {
                        name: foo.name,
                        id: uuidv4(),
                        item_id: draggedItem.item_id,
                        base_point: hoveredCell,
                    };
                    onAddItem(bar);
                } else if (draggedItem.itemId) {
                    // Update existing item position
                    const item = items.find((i) => i.id === draggedItem.itemId);
                    if (item) {
                        onUpdateItem({
                            ...item,
                            base_point: hoveredCell,
                        });
                    }
                }
            }
        }

        // Reset dragging state
        setDraggedItem(null);
    };

    // Handle drag over for item placement preview
    const handleDragOver = (e: React.DragEvent) => {
        e.preventDefault();

        const canvas = canvasRef.current;
        if (!canvas) return;

        const rect = canvas.getBoundingClientRect();
        const mouseX = e.clientX - rect.left;
        const mouseY = e.clientY - rect.top;

        // Update hovered cell
        const gridPos = screenToGrid(mouseX, mouseY);
        setHoveredCell(gridPos);
    };

    // Handle drop for placing new items
    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault();

        const canvas = canvasRef.current;
        if (!canvas) return;

        try {
            const data = JSON.parse(e.dataTransfer.getData("application/json"));
            const { item_id } = data;

            const rect = canvas.getBoundingClientRect();
            const mouseX = e.clientX - rect.left;
            const mouseY = e.clientY - rect.top;

            const gridPos = screenToGrid(mouseX, mouseY);

            const name = itemTypes.filter((itemType) => itemType.id === item_id)[0].name;

            // Check if we can place the item
            if (canPlaceItem(gridPos, item_id)) {
                onAddItem({
                    id: uuidv4(),
                    item_id,
                    name,
                    base_point: gridPos,
                });
            }
        } catch (error) {
            console.error("Error parsing drag data:", error);
        }

        setHoveredCell(null);
    };

    // Handle drag enter
    const handleDragEnter = (e: React.DragEvent) => {
        e.preventDefault();

        try {
            const data = JSON.parse(e.dataTransfer.getData("application/json"));
            const { item_id } = data;

            // Set dragged item for preview
            setDraggedItem({
                item_id,
                position: { x: 0, y: 0 },
                isNew: true,
            });
        } catch (error) {
            console.error("Error parsing drag data:", error);
        }
    };

    // Handle drag leave
    const handleDragLeave = () => {
        setHoveredCell(null);
    };

    // Handle key press for deleting items
    const handleKeyDown = useCallback(
        (e: KeyboardEvent) => {
            if (e.key === "Delete" || e.key === "Backspace") {
                if (selectedItem) {
                    onRemoveItem(selectedItem.id);
                    setSelectedItem(null);
                }
            }
            if (e.key === "Escape") {
                setSelectedItem(null);
                setContextMenuPosition(null);
            }
        },
        [selectedItem, onRemoveItem],
    );

    // Resize canvas when container size changes
    useEffect(() => {
        const resizeCanvas = () => {
            const canvas = canvasRef.current;
            const container = containerRef.current;
            if (!canvas || !container) return;

            canvas.width = container.clientWidth;
            canvas.height = container.clientHeight;

            drawGrid();
        };

        resizeCanvas();
        window.addEventListener("resize", resizeCanvas);

        return () => {
            window.removeEventListener("resize", resizeCanvas);
        };
    }, [drawGrid]);

    // Add keyboard event listeners
    useEffect(() => {
        window.addEventListener("keydown", handleKeyDown);

        return () => {
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [handleKeyDown]);

    // Redraw grid when relevant state changes
    useEffect(() => {
        drawGrid();
    }, [drawGrid]);

    // Handle context menu
    const handleContextMenu = (e: React.MouseEvent) => {
        e.preventDefault();
        setContextMenuPosition(null);
    };

    // Handle remove item
    const handleRemoveItem = () => {
        if (selectedItem) {
            onRemoveItem(selectedItem.id);
            setSelectedItem(null);
            setContextMenuPosition(null);
        }
    };

    return (
        <div ref={containerRef} className="w-full h-full relative" onContextMenu={handleContextMenu}>
            <canvas
                ref={canvasRef}
                className="w-full h-full cursor-grab"
                onWheel={handleWheel}
                onMouseDown={handleMouseDown}
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
                onMouseLeave={handleMouseUp}
                onDragOver={handleDragOver}
                onDrop={handleDrop}
                onDragEnter={handleDragEnter}
                onDragLeave={handleDragLeave}
            />
            {contextMenuPosition && (
                <div
                    className="absolute bg-white border rounded shadow-md p-2"
                    style={{
                        left: contextMenuPosition.x,
                        top: contextMenuPosition.y,
                    }}
                >
                    <Button variant="ghost" className="w-full justify-start" onClick={handleRemoveItem}>
                        <Trash2 className="mr-2 h-4 w-4" />
                        Remove Item
                    </Button>
                </div>
            )}
            <div className="absolute bottom-4 right-4 bg-background border rounded-md p-2 shadow-sm flex gap-2">
                <Button variant="outline" size="sm" onClick={() => setScale((prev) => Math.min(3, prev * 1.2))}>
                    +
                </Button>
                <Button variant="outline" size="sm" onClick={() => setScale((prev) => Math.max(0.2, prev / 1.2))}>
                    -
                </Button>
                <Button
                    variant="outline"
                    size="sm"
                    onClick={() => {
                        setScale(1);
                        setOffset({ x: 0, y: 0 });
                    }}
                >
                    Reset
                </Button>
            </div>
        </div>
    );
}
