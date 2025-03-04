"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import type { Building, Coworking, ItemType, CoworkingItem } from "@/lib/types";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import BuildingSelector from "./building-selector";
import CoworkingSelector from "./coworking-selector";
import ItemSidebar from "./item-sidebar";
import CoworkingGrid from "./coworking-grid";
import GridSizeControls from "./grid-size-controls";
import ItemTypeCreator from "./item-type-creator";
import Login from "./login";
import { useAuth } from "@/lib/auth";
import PlacesManager from "./places-manager";
import { addItemType, getItems, getItemTypes, setCoworkingItems } from "@/app/items";
import { getBuildings } from "@/app/buildings";
import { getCoworkingsByBuilding, updateCoworking } from "@/app/coworkings";

export default function CoworkingAdmin() {
    const router = useRouter();
    const { token, login, logout } = useAuth();
    const [buildings, setBuildings] = useState<Building[]>([]);
    const [selectedBuilding, setSelectedBuilding] = useState<Building | null>(null);
    const [coworkings, setCoworkings] = useState<Coworking[]>([]);
    const [selectedCoworking, setSelectedCoworking] = useState<Coworking | null>(null);
    const [itemTypes, setItemTypes] = useState<ItemType[]>([]);
    const [items, setItems] = useState<CoworkingItem[]>([]);
    const [activeTab, setActiveTab] = useState("editor");
    const [isLoading, setIsLoading] = useState(true);
    const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
    const [localChanges, setLocalChanges] = useState<{
        gridSize?: { height: number; width: number };
    }>({});

    useEffect(() => {
        if (token) {
            fetchInitialData();
        }
    }, [token]);

    useEffect(() => {
        if (selectedBuilding) {
            fetchCoworkings(selectedBuilding.id);
        }
    }, [selectedBuilding]);

    useEffect(() => {
        if (selectedCoworking) {
            fetchItems(selectedCoworking.id);
        } else {
            setItems([]);
        }
    }, [selectedCoworking]);

    const fetchInitialData = async () => {
        setIsLoading(true);
        try {
            const [buildingsData, itemTypesData] = await Promise.all([getBuildings(token as string), getItemTypes(token)]);

            setBuildings(buildingsData);
            setItemTypes(itemTypesData);

            if (buildingsData.length > 0) {
                console.log(buildingsData);
                setSelectedBuilding(buildingsData[0]);
            }
        } catch (error) {
            console.error("Error fetching initial data:", error);
        } finally {
            setIsLoading(false);
        }
    };

    const fetchCoworkings = async (buildingId: string) => {
        setIsLoading(true);
        try {
            const data = await getCoworkingsByBuilding(buildingId, token as string);
            setCoworkings(data);
            if (data.length > 0) {
                setSelectedCoworking(data[0]);
            } else {
                setSelectedCoworking(null);
            }
        } catch (error) {
            console.error("Error fetching coworkings:", error);
        } finally {
            setIsLoading(false);
        }
    };

    const fetchItems = async (coworkingId: string) => {
        setIsLoading(true);
        try {
            let building_id = buildings.filter((b) => (b.id = selectedCoworking!.building_id))[0].id;
            const data = await getItems(building_id, coworkingId, token);
            setItems(data);
        } catch (error) {
            console.error("Error fetching items:", error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleLogin = (newToken: string) => {
        login(newToken);
        router.refresh();
    };

    const handleLogout = () => {
        logout();
        setSelectedBuilding(null);
        setSelectedCoworking(null);
        setItems([]);
        router.refresh();
    };

    const handleBuildingChange = (buildingId: string) => {
        const building = buildings.find((b) => b.id === buildingId) || null;
        setSelectedBuilding(building);
    };

    const handleCoworkingChange = (coworkingId: string) => {
        const coworking = coworkings.find((c) => c.id === coworkingId) || null;
        setSelectedCoworking(coworking);
    };

    const handleGridSizeChange = (height: number, width: number) => {
        setLocalChanges((prev) => ({
            ...prev,
            gridSize: { height, width },
        }));
        setHasUnsavedChanges(true);
    };

    const handleAddItem = (item: CoworkingItem) => {
        setItems((prev) => [...prev, item]);
        setHasUnsavedChanges(true);
    };

    const handleUpdateItem = (updatedItem: CoworkingItem) => {
        setItems((prev) => prev.map((item) => (item.id === updatedItem.id ? updatedItem : item)));
        setHasUnsavedChanges(true);
    };

    const handleRemoveItem = (itemId: string) => {
        setItems((prev) => prev.filter((item) => item.id !== itemId));
        setHasUnsavedChanges(true);
    };

    const handleAddItemType = async (newItemType: ItemType) => {
        await addItemType(newItemType, token);
        setItemTypes((prev) => [...prev, newItemType]);
        setHasUnsavedChanges(true);
    };

    const loadBuildings = async () => {
        const loadedBuildings = await getBuildings(token as string);
        setBuildings(loadedBuildings);
        if (loadedBuildings.length > 0 && !selectedBuilding) {
            setSelectedBuilding(loadedBuildings[0]);
        }
    };

    const loadCoworkings = async (buildingId: string) => {
        const loadedCoworkings = await getCoworkingsByBuilding(buildingId, token);
        setCoworkings(loadedCoworkings);
        if (loadedCoworkings.length > 0) {
            setSelectedCoworking(loadedCoworkings[0]);
        } else {
            setSelectedCoworking(null);
        }
    };

    const handleSaveChanges = async () => {
        setIsLoading(true);
        try {
            let building_id = buildings.filter((b) => (b.id = selectedCoworking!.building_id)).map((b) => b.id);
            if (localChanges.gridSize && selectedCoworking) {
                await updateCoworking(
                    building_id[0],
                    selectedCoworking.id,
                    {
                        address: selectedCoworking.address,
                        height: localChanges.gridSize.height,
                        width: localChanges.gridSize.width,
                    },
                    token,
                );
                setSelectedCoworking((prev) => (prev ? { ...prev, ...localChanges.gridSize } : null));
            }

            const translatedItems: CoworkingItem[] = items.map((item) => ({
                ...item,
                position: {
                    x: item.base_point.x,
                    y: localChanges.gridSize?.height! - item.base_point.y - 1,
                },
            }));

            await setCoworkingItems(building_id[0], selectedCoworking!.id, translatedItems, token);

            setHasUnsavedChanges(false);

            if (selectedCoworking) {
                await fetchItems(selectedCoworking.id);
            }

            await fetchInitialData();
        } catch (error) {
            console.error("Error saving changes:", error);
            alert("An error occurred while saving changes. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    if (!token) {
        return <Login onLogin={handleLogin} />;
    }

    if (isLoading) {
        return <div className="flex items-center justify-center h-screen">Loading...</div>;
    }

    return (
        <div className="flex flex-col h-screen">
            <header className="bg-background border-b p-4">
                <div className="flex items-center justify-between">
                    <h1 className="text-2xl font-bold">Coworking Admin</h1>
                    <div className="flex gap-4 items-center">
                        <BuildingSelector buildings={buildings} selectedBuilding={selectedBuilding} onSelectBuilding={handleBuildingChange} />
                        <CoworkingSelector coworkings={coworkings} selectedCoworking={selectedCoworking} onSelectCoworking={handleCoworkingChange} />
                        <Button onClick={handleLogout} variant="outline">
                            Logout
                        </Button>
                    </div>
                </div>
            </header>

            <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col">
                <div className="border-b px-4">
                    <TabsList>
                        <TabsTrigger value="editor">Coworking Editor</TabsTrigger>
                        <TabsTrigger value="itemTypes">Item Types</TabsTrigger>
                        <TabsTrigger value="places">Places</TabsTrigger>
                    </TabsList>
                </div>

                <TabsContent value="editor" className={activeTab === "editor" ? "flex-1 flex overflow-hidden" : "flex overflow-hidden"}>
                    <ItemSidebar itemTypes={itemTypes} />

                    <div className="flex-1 flex flex-col">
                        <div className="p-2 border-b">
                            {selectedCoworking && (
                                <GridSizeControls
                                    rows={localChanges.gridSize?.height || selectedCoworking.height}
                                    cols={localChanges.gridSize?.width || selectedCoworking.width}
                                    onSizeChange={handleGridSizeChange}
                                />
                            )}
                        </div>

                        <div className="flex-1 relative">
                            {selectedCoworking ? (
                                <CoworkingGrid
                                    rows={localChanges.gridSize?.height || selectedCoworking.height}
                                    cols={localChanges.gridSize?.width || selectedCoworking.width}
                                    items={items}
                                    itemTypes={itemTypes}
                                    onAddItem={handleAddItem}
                                    onUpdateItem={handleUpdateItem}
                                    onRemoveItem={handleRemoveItem}
                                />
                            ) : (
                                <div className="flex items-center justify-center h-full">
                                    <p className="text-muted-foreground">Select a coworking to edit</p>
                                </div>
                            )}
                        </div>
                    </div>
                </TabsContent>

                <TabsContent value="itemTypes" className="flex-1 p-4">
                    <ItemTypeCreator onAddItemType={handleAddItemType} />
                </TabsContent>

                <TabsContent value="places" className="flex-1 p-4">
                    <PlacesManager token={token} onUpdateBuildings={loadBuildings} onUpdateCoworkings={() => loadCoworkings(selectedBuilding?.id || "")} />
                </TabsContent>
            </Tabs>

            <div className="border-t p-4 bg-background">
                <Button onClick={handleSaveChanges} disabled={!hasUnsavedChanges}>
                    Save All Changes
                </Button>
            </div>
        </div>
    );
}
