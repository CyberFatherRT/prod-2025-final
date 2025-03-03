import { Building } from "@/lib/types";
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

export async function updateBuilding(id: string, address: string, token: string | null): Promise<Building> {
    const response = await fetch(`${backendDomain}/place/${id}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ address }),
    });

    return await response.json();
}

export async function deleteBuilding(building_id: string, token: string | null) {
    await fetch(`${backendDomain}/place/${building_id}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
    });
}
