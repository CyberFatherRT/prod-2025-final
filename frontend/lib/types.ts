export interface Position {
    x: number;
    y: number;
}

export interface ItemType {
    id: string;
    name: string;
    description: string | null;
    color: string;
    bookable: boolean;
    offsets: [number, number][];
}

export interface CoworkingItem {
    id: string;
    name: string;
    item_id: string;
    base_point: Position;
}

export interface Coworking {
    id: string;
    address: string;
    height: number;
    width: number;
    building_id: string;
    company_id: string;
}

export interface Building {
    id: string;
    address: string;
}
