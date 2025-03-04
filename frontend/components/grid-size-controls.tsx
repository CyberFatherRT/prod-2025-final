"use client";

import type React from "react";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Plus, Minus } from "lucide-react";

interface GridSizeControlsProps {
    rows: number;
    cols: number;
    onSizeChange: (rows: number, cols: number) => void;
}

export default function GridSizeControls({ rows, cols, onSizeChange }: GridSizeControlsProps) {
    const [localRows, setLocalRows] = useState(rows);
    const [localCols, setLocalCols] = useState(cols);

    const handleRowsChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = Number.parseInt(e.target.value);
        if (!isNaN(value) && value > 0) {
            setLocalRows(value);
        }
    };

    const handleColsChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = Number.parseInt(e.target.value);
        if (!isNaN(value) && value > 0) {
            setLocalCols(value);
        }
    };

    const handleApply = () => {
        onSizeChange(localRows, localCols);
    };

    useEffect(handleApply, [localRows, localCols]);

    const incrementRows = () => setLocalRows((prev) => Math.min(100, prev + 1));
    const decrementRows = () => setLocalRows((prev) => Math.max(1, prev - 1));
    const incrementCols = () => setLocalCols((prev) => Math.min(100, prev + 1));
    const decrementCols = () => setLocalCols((prev) => Math.max(1, prev - 1));

    return (
        <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
                <span className="text-sm font-medium">Rows:</span>
                <div className="flex items-center">
                    <Button variant="outline" size="icon" className="h-8 w-8 rounded-r-none" onClick={decrementRows}>
                        <Minus className="h-4 w-4" />
                    </Button>
                    <Input
                        type="number"
                        value={localRows}
                        onChange={handleRowsChange}
                        className="h-8 w-16 rounded-none text-center appearance-none"
                        min="1"
                        max="100"
                    />
                    <Button variant="outline" size="icon" className="h-8 w-8 rounded-l-none" onClick={incrementRows}>
                        <Plus className="h-4 w-4" />
                    </Button>
                </div>
            </div>

            <div className="flex items-center gap-2">
                <span className="text-sm font-medium">Columns:</span>
                <div className="flex items-center">
                    <Button variant="outline" size="icon" className="h-8 w-8 rounded-r-none" onClick={decrementCols}>
                        <Minus className="h-4 w-4" />
                    </Button>
                    <Input
                        type="number"
                        value={localCols}
                        onChange={handleColsChange}
                        className="h-8 w-16 rounded-none text-center appearance-none"
                        min="1"
                        max="100"
                    />
                    <Button variant="outline" size="icon" className="h-8 w-8 rounded-l-none" onClick={incrementCols}>
                        <Plus className="h-4 w-4" />
                    </Button>
                </div>
            </div>
        </div>
    );
}
