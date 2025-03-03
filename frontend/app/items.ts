"use server";

import type { ItemType, CoworkingItem } from "@/lib/types";
import { mockItemTypes, mockItems } from "@/lib/data-store";

export async function getItemTypes(): Promise<ItemType[]> {
    return mockItemTypes;
}

export async function getItems(coworkingId: string): Promise<CoworkingItem[]> {
    return mockItems[coworkingId] || [];
}

export async function addItem(item: CoworkingItem & { coworkingId: string }): Promise<CoworkingItem> {
    const { coworkingId, ...newItem } = item;
    if (!mockItems[coworkingId]) mockItems[coworkingId] = [];
    mockItems[coworkingId].push(newItem);
    return newItem;
}

export async function updateItem(item: CoworkingItem): Promise<CoworkingItem> {
    for (const coworkingId in mockItems) {
        const index = mockItems[coworkingId].findIndex((i) => i.id === item.id);
        if (index !== -1) {
            mockItems[coworkingId][index] = item;
            return item;
        }
    }
    throw new Error("Item not found");
}

export async function removeItem(itemId: string): Promise<void> {
    for (const coworkingId in mockItems) {
        mockItems[coworkingId] = mockItems[coworkingId].filter((i) => i.id !== itemId);
    }
}

export async function addItemType(itemType: ItemType): Promise<ItemType> {
    mockItemTypes.push(itemType);
    return itemType;
}
