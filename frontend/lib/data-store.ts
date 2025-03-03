import type { ItemType, CoworkingItem } from "./types";

// This file now contains only type definitions and mock data
// The actual data fetching will be done in server components

export const mockItemTypes: ItemType[] = [
    {
        id: "1",
        name: "Desk",
        color: "#4f46e5",
        shape: "rectangle",
        offsets: [
            [0, 0],
            [1, 0],
        ],
    },
    {
        id: "2",
        name: "Chair",
        color: "#10b981",
        shape: "square",
        offsets: [[0, 0]],
    },
    // Add more mock item types as needed
];

export const mockItems: Record<string, CoworkingItem[]> = {
    "1": [
        { id: "1", typeId: "1", position: { x: 2, y: 3 } },
        { id: "2", typeId: "2", position: { x: 4, y: 3 } },
    ],
    "2": [{ id: "3", typeId: "1", position: { x: 1, y: 2 } }],
    "3": [{ id: "4", typeId: "2", position: { x: 3, y: 2 } }],
};
