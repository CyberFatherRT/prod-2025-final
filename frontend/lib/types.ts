export interface Position {
    x: number;
    y: number;
}

export interface ItemType {
    id: string;
    name: string;
    color: string;
    shape: string;
    offsets: [number, number][]; // Array of [x, y] offsets from base position
}

export interface CoworkingItem {
    id: string;
    typeId: string;
    position: Position;
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
