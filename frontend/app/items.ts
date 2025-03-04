"use server";

import type { ItemType, CoworkingItem } from "@/lib/types";
import { backendDomain } from "@/lib/utils";

type ItemTypeResponse = {
    id: string;
    name: string;
    description: string | null;
    icon: string | null;
    color: string;
    bookable: boolean;
    offsets: { x: number; y: number }[];
};

export async function getItemTypes(token: string | null): Promise<ItemType[]> {
    const response = await fetch(`${backendDomain}/items`, {
        method: "GET",
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });

    const itemTypes: ItemTypeResponse[] = await response.json();
    return itemTypes.map((itemType) => ({ ...itemType, offsets: [...itemType.offsets.map(({ x, y }) => [x, y] as [number, number])] }));
}

export async function getItems(b_id: string, c_id: string, token: string | null): Promise<CoworkingItem[]> {
    const response = await fetch(`${backendDomain}/place/${b_id}/coworking/${c_id}/items`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
    return await response.json();
}

export async function setCoworkingItems(b_id: string, c_id: string, items: CoworkingItem[], token: string | null): Promise<void> {
    console.log(items);
    await fetch(`${backendDomain}/place/${b_id}/coworking/${c_id}/items/put`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(items),
    });
}

export async function addItemType(itemType: ItemType, token: string | null) {
    const foo = {
        ...itemType,
        id: undefined,
        offsets: [...itemType.offsets.map(([x, y]) => ({ x, y }))],
    };

    const requestBody = new FormData();
    requestBody.append("json", JSON.stringify(foo));

    await fetch(`${backendDomain}/items/new`, {
        method: "POST",
        headers: {
            Authorization: `Bearer ${token}`,
        },
        body: requestBody,
    });
}
