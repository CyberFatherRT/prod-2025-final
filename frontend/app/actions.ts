"use server";

import type { Building, Coworking, ItemType, CoworkingItem } from "@/lib/types";
import { mockItemTypes, mockItems } from "@/lib/data-store";
import { backendDomain } from "@/lib/utils";

export async function getBuildings(token: string | null): Promise<Building[]> {
    const response = await fetch(`${backendDomain}/place/list`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });

    const buildings = await response.json();
    return buildings;
}

export async function getCoworkings(token: string | null): Promise<Coworking[]> {
    const response = await fetch(`${backendDomain}/place/coworking/list`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });

    const coworkings = await response.json();

    return coworkings;
}

export async function getCoworkingsByBuilding(buildingId: string, token: string | null): Promise<Coworking[]> {
    const response = await fetch(`${backendDomain}/place/${buildingId}/coworking/list`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });

    const coworkings = await response.json();

    return coworkings;
}

export async function addBuilding(address: string, token: string | null): Promise<Building> {
    const response = await fetch(`${backendDomain}/place/new`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ address }),
    });

    return await response.json();
}

export async function updateBuilding(building: Building) {}
export async function deleteBuilding(building_id: string, token: string | null) {
    await fetch(`${backendDomain}/place/${building_id}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
    });
}

export async function getCoworking(id: string): Promise<Coworking> {
    return {} as Coworking;
}
export async function addCoworking(coworking: Omit<Coworking, "company_id">, token: string | null): Promise<Coworking> {
    const response = await fetch(`${backendDomain}/place/${coworking.building_id}/coworking/new`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(coworking),
    });

    return await response.json();
}
export async function updateCoworking(id: string, data: Partial<Coworking>): Promise<Coworking> {
    return {} as Coworking;
}

export async function deleteCoworking(b_id: string, c_id: string, token: string | null) {
    await fetch(`${backendDomain}/place/${b_id}/coworking/${c_id}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
    });
}

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
