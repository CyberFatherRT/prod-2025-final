import { Coworking } from "@/lib/types";
import { backendDomain } from "@/lib/utils";

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
