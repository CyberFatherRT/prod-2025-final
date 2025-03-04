"use client";

import { useEffect, useState } from "react";
import type { Building, Coworking } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Trash2, Edit, Plus } from "lucide-react";
import { addCoworking, deleteCoworking, getCoworking, getCoworkings, updateCoworking } from "@/app/coworkings";
import { addBuilding, deleteBuilding, getBuildings, updateBuilding } from "@/app/buildings";

interface PlacesManagerProps {
    token: string | null;
    onUpdateBuildings: () => void;
    onUpdateCoworkings: () => void;
}

export default function PlacesManager({ onUpdateBuildings, onUpdateCoworkings, token }: PlacesManagerProps) {
    const [buildings, setBuildings] = useState<Building[]>([]);
    const [coworkings, setCoworkings] = useState<Coworking[]>([]);
    const [newBuildingAddress, setNewBuildingName] = useState("");
    const [editBuildingId, setEditBuildingId] = useState<string | null>(null);
    const [editBuildingAddress, setEditBuildingAddress] = useState("");
    const [newCoworkingName, setNewCoworkingName] = useState("");
    const [newCoworkingRows, setNewCoworkingRows] = useState(10);
    const [newCoworkingCols, setNewCoworkingCols] = useState(10);
    const [editCoworkingId, setEditCoworkingId] = useState<string | null>(null);
    const [editCoworkingName, setEditCoworkingAddress] = useState("");
    const [editCoworkingRows, setEditCoworkingRows] = useState(10);
    const [editCoworkingCols, setEditCoworkingCols] = useState(10);

    useEffect(() => {
        const fetchPlaces = async () => {
            await updateCoworkings();
            await updateBuildings();
        };

        fetchPlaces().catch((error) => {
            console.error("Error fetching challenge info:", error);
        });
    }, []);

    const updateCoworkings = async () => {
        const coworkingList = await getCoworkings(token);
        setCoworkings(coworkingList);
    };

    const updateBuildings = async () => {
        const buildingList = await getBuildings(token);
        setBuildings(buildingList);
    };

    const handleCreateBuilding = async () => {
        if (newBuildingAddress.trim()) {
            await addBuilding(newBuildingAddress.trim(), token);
            setNewBuildingName("");
            updateBuildings();
            onUpdateBuildings();
        }
    };

    const handleUpdateBuilding = async () => {
        if (editBuildingId && editBuildingAddress.trim()) {
            await updateBuilding(editBuildingId, editBuildingAddress.trim(), token);
            setEditBuildingId(null);
            setEditBuildingAddress("");
            onUpdateBuildings();
        }
    };

    const handleDeleteBuilding = async (building_id: string) => {
        if (confirm("Are you sure you want to delete this building and all its coworkings?")) {
            await deleteBuilding(building_id, token);
            onUpdateBuildings();
            onUpdateCoworkings();
        }
    };

    const handleCreateCoworking = async (building_id: string) => {
        if (newCoworkingName.trim()) {
            await addCoworking(
                {
                    id: crypto.randomUUID(),
                    address: newCoworkingName.trim(),
                    building_id,
                    height: newCoworkingRows,
                    width: newCoworkingCols,
                },
                token,
            );
            setNewCoworkingName("");
            setNewCoworkingRows(10);
            setNewCoworkingCols(10);
            onUpdateCoworkings();
        }
    };

    const handleUpdateCoworking = async () => {
        if (editCoworkingId && editCoworkingName.trim()) {
            const building_id = coworkings.filter((coworking) => coworking.id === editCoworkingId).map((coworking) => coworking.building_id);
            const coworking = await getCoworking(building_id[0], editCoworkingId, token);
            if (coworking) {
                await updateCoworking(
                    building_id[0],
                    coworking.id,
                    {
                        address: editCoworkingName.trim(),
                        height: editCoworkingRows,
                        width: editCoworkingCols,
                    },
                    token,
                );
                setEditCoworkingId(null);
                setEditCoworkingAddress("");
                setEditCoworkingRows(10);
                setEditCoworkingCols(10);
                onUpdateCoworkings();
            }
        }
    };

    const handleDeleteCoworking = async (coworkingId: string) => {
        if (confirm("Are you sure you want to delete this coworking?")) {
            const building_id = coworkings.filter((coworking) => coworking.id === coworkingId).map((coworking) => coworking.building_id);
            await deleteCoworking(building_id[0], coworkingId, token);
            updateCoworkings();
            onUpdateCoworkings();
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold">Buildings</h2>
                <Dialog>
                    <DialogTrigger asChild>
                        <Button>
                            <Plus className="mr-2 h-4 w-4" /> Add Building
                        </Button>
                    </DialogTrigger>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Add New Building</DialogTitle>
                            <DialogDescription>Enter the name for the new building.</DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="new-building-name" className="text-right">
                                    Name
                                </Label>
                                <Input
                                    id="new-building-name"
                                    value={newBuildingAddress}
                                    onChange={(e) => setNewBuildingName(e.target.value)}
                                    className="col-span-3"
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button onClick={handleCreateBuilding}>Create Building</Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            </div>

            {buildings.length === 0 ? (
                <p className="text-muted-foreground">No buildings created yet.</p>
            ) : (
                <Accordion type="single" collapsible className="w-full">
                    {buildings.map((building) => (
                        <AccordionItem key={building.id} value={building.id}>
                            <AccordionTrigger className="flex justify-between items-center">
                                <span>{building.address}</span>
                                <div className="flex items-center space-x-2" onClick={(e) => e.stopPropagation()}>
                                    <Dialog>
                                        <DialogTrigger asChild>
                                            <Button
                                                variant="outline"
                                                size="icon"
                                                onClick={() => {
                                                    setEditBuildingId(building.id);
                                                    setEditBuildingAddress(building.address);
                                                }}
                                            >
                                                <Edit className="h-4 w-4" />
                                            </Button>
                                        </DialogTrigger>
                                        <DialogContent>
                                            <DialogHeader>
                                                <DialogTitle>Edit Building</DialogTitle>
                                                <DialogDescription>Update the building name.</DialogDescription>
                                            </DialogHeader>
                                            <div className="grid gap-4 py-4">
                                                <div className="grid grid-cols-4 items-center gap-4">
                                                    <Label htmlFor="edit-building-name" className="text-right">
                                                        Name
                                                    </Label>
                                                    <Input
                                                        id="edit-building-name"
                                                        value={editBuildingAddress}
                                                        onChange={(e) => setEditBuildingAddress(e.target.value)}
                                                        className="col-span-3"
                                                    />
                                                </div>
                                            </div>
                                            <DialogFooter>
                                                <Button
                                                    onClick={() => {
                                                        setEditBuildingId(building.id);
                                                        setEditBuildingAddress(building.address);
                                                        handleUpdateBuilding();
                                                    }}
                                                >
                                                    Update Building
                                                </Button>
                                            </DialogFooter>
                                        </DialogContent>
                                    </Dialog>
                                    <Button variant="outline" size="icon" onClick={() => handleDeleteBuilding(building.id)}>
                                        <Trash2 className="h-4 w-4" />
                                    </Button>
                                </div>
                            </AccordionTrigger>
                            <AccordionContent>
                                <div className="space-y-4">
                                    <div className="flex justify-between items-center">
                                        <h3 className="text-lg font-semibold">Coworkings</h3>
                                        <Dialog>
                                            <DialogTrigger asChild>
                                                <Button variant="outline">
                                                    <Plus className="mr-2 h-4 w-4" /> Add Coworking
                                                </Button>
                                            </DialogTrigger>
                                            <DialogContent>
                                                <DialogHeader>
                                                    <DialogTitle>Add New Coworking</DialogTitle>
                                                    <DialogDescription>Enter the details for the new coworking space.</DialogDescription>
                                                </DialogHeader>
                                                <div className="grid gap-4 py-4">
                                                    <div className="grid grid-cols-4 items-center gap-4">
                                                        <Label htmlFor="new-coworking-name" className="text-right">
                                                            Name
                                                        </Label>
                                                        <Input
                                                            id="new-coworking-name"
                                                            value={newCoworkingName}
                                                            onChange={(e) => setNewCoworkingName(e.target.value)}
                                                            className="col-span-3"
                                                        />
                                                    </div>
                                                    <div className="grid grid-cols-4 items-center gap-4">
                                                        <Label htmlFor="new-coworking-rows" className="text-right">
                                                            Rows
                                                        </Label>
                                                        <Input
                                                            id="new-coworking-rows"
                                                            type="number"
                                                            value={newCoworkingRows}
                                                            onChange={(e) => setNewCoworkingRows(Number.parseInt(e.target.value))}
                                                            className="col-span-3"
                                                        />
                                                    </div>
                                                    <div className="grid grid-cols-4 items-center gap-4">
                                                        <Label htmlFor="new-coworking-cols" className="text-right">
                                                            Columns
                                                        </Label>
                                                        <Input
                                                            id="new-coworking-cols"
                                                            type="number"
                                                            value={newCoworkingCols}
                                                            onChange={(e) => setNewCoworkingCols(Number.parseInt(e.target.value))}
                                                            className="col-span-3"
                                                        />
                                                    </div>
                                                </div>
                                                <DialogFooter>
                                                    <Button onClick={() => handleCreateCoworking(building.id)}>Create Coworking</Button>
                                                </DialogFooter>
                                            </DialogContent>
                                        </Dialog>
                                    </div>
                                    {coworkings.filter((c) => c.building_id === building.id).length === 0 ? (
                                        <p className="text-muted-foreground">No coworkings in this building yet.</p>
                                    ) : (
                                        <ul className="space-y-2">
                                            {coworkings
                                                .filter((c) => c.building_id === building.id)
                                                .map((coworking) => (
                                                    <li key={coworking.id} className="flex justify-between items-center">
                                                        <span>{coworking.address}</span>
                                                        <div className="flex items-center space-x-2">
                                                            <Dialog>
                                                                <DialogTrigger asChild>
                                                                    <Button
                                                                        variant="outline"
                                                                        size="sm"
                                                                        onClick={() => {
                                                                            setEditCoworkingId(coworking.id);
                                                                            setEditCoworkingAddress(coworking.address);
                                                                            setEditCoworkingRows(coworking.height);
                                                                            setEditCoworkingCols(coworking.width);
                                                                        }}
                                                                    >
                                                                        <Edit className="mr-2 h-4 w-4" /> Edit
                                                                    </Button>
                                                                </DialogTrigger>
                                                                <DialogContent>
                                                                    <DialogHeader>
                                                                        <DialogTitle>Edit Coworking</DialogTitle>
                                                                        <DialogDescription>Update the coworking details.</DialogDescription>
                                                                    </DialogHeader>
                                                                    <div className="grid gap-4 py-4">
                                                                        <div className="grid grid-cols-4 items-center gap-4">
                                                                            <Label htmlFor="edit-coworking-name" className="text-right">
                                                                                Name
                                                                            </Label>
                                                                            <Input
                                                                                id="edit-coworking-name"
                                                                                value={editCoworkingName}
                                                                                onChange={(e) => setEditCoworkingAddress(e.target.value)}
                                                                                className="col-span-3"
                                                                            />
                                                                        </div>
                                                                        <div className="grid grid-cols-4 items-center gap-4">
                                                                            <Label htmlFor="edit-coworking-rows" className="text-right">
                                                                                Rows
                                                                            </Label>
                                                                            <Input
                                                                                id="edit-coworking-rows"
                                                                                type="number"
                                                                                value={editCoworkingRows}
                                                                                onChange={(e) => setEditCoworkingRows(Number.parseInt(e.target.value))}
                                                                                className="col-span-3"
                                                                            />
                                                                        </div>
                                                                        <div className="grid grid-cols-4 items-center gap-4">
                                                                            <Label htmlFor="edit-coworking-cols" className="text-right">
                                                                                Columns
                                                                            </Label>
                                                                            <Input
                                                                                id="edit-coworking-cols"
                                                                                type="number"
                                                                                value={editCoworkingCols}
                                                                                onChange={(e) => setEditCoworkingCols(Number.parseInt(e.target.value))}
                                                                                className="col-span-3"
                                                                            />
                                                                        </div>
                                                                    </div>
                                                                    <DialogFooter>
                                                                        <Button
                                                                            onClick={() => {
                                                                                setEditCoworkingId(coworking.id);
                                                                                setEditCoworkingAddress(coworking.address);
                                                                                setEditCoworkingRows(coworking.height);
                                                                                setEditCoworkingCols(coworking.width);
                                                                                handleUpdateCoworking();
                                                                            }}
                                                                        >
                                                                            Update Coworking
                                                                        </Button>
                                                                    </DialogFooter>
                                                                </DialogContent>
                                                            </Dialog>
                                                            <Button variant="outline" size="sm" onClick={() => handleDeleteCoworking(coworking.id)}>
                                                                <Trash2 className="mr-2 h-4 w-4" /> Delete
                                                            </Button>
                                                        </div>
                                                    </li>
                                                ))}
                                        </ul>
                                    )}
                                </div>
                            </AccordionContent>
                        </AccordionItem>
                    ))}
                </Accordion>
            )}
        </div>
    );
}
