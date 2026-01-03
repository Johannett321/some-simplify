import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {Item, ItemActions, ItemContent, ItemTitle} from "@/components/ui/item.tsx";
import {ChevronRightIcon} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import {Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyTitle} from "@/components/ui/empty.tsx";
import {useNavigate} from "react-router-dom";
import {type TenantTO} from "@/api";

interface TenantPickerPageProps {
    tenants: TenantTO[]
    onSelectTenant: (tenant: TenantTO) => void
}

export const TenantPickerPage = ({tenants, onSelectTenant}: TenantPickerPageProps) => {
    const navigate = useNavigate()
    return (
        <div className={"w-screen h-screen flex justify-center items-center"}>
            <img className={"absolute top-0 left-0 w-screen h-screen object-cover -z-10"} src={"/registerphoto.jpg"}/>
            <Card className={"w-[400px] h-[400px]"}>
                {tenants.length > 0 ? (
                    <>
                        <CardHeader>
                            <CardTitle>Velg ditt foretak</CardTitle>
                            <CardDescription>Velg foretaket du vil logge inn på</CardDescription>
                        </CardHeader>
                        <CardContent className={"overflow-y-auto space-y-2"}>
                            {tenants.map(o => (
                                <Item
                                    key={o.id}
                                    className={"hover:bg-stone-50 transition-all cursor-pointer"}
                                    onClick={() => onSelectTenant(o)}
                                    variant={"outline"}
                                >
                                    <ItemContent>
                                        <ItemTitle>{o.name}</ItemTitle>
                                    </ItemContent>
                                    <ItemActions>
                                        <ChevronRightIcon className="size-4" />
                                    </ItemActions>
                                </Item>
                            ))}
                        </CardContent>
                        <CardFooter className={"flex justify-between"}>
                            <Button variant={"destructive"}>
                                Logg ut
                            </Button>
                            <Button onClick={() => navigate("/opprett-foretak")} variant={"default"}>
                                Opprett nytt foretak
                            </Button>
                        </CardFooter>
                    </>
                ) : (
                    <Empty>
                        <EmptyHeader>
                            <EmptyTitle>Opprett ditt første sted</EmptyTitle>
                            <EmptyDescription>
                                Du har ingen sted. Kom i gang ved å lage ditt første sted
                            </EmptyDescription>
                        </EmptyHeader>
                        <EmptyContent>
                            <Button onClick={() => navigate("/opprett-foretak")}>
                                Opprett sted
                            </Button>
                        </EmptyContent>
                    </Empty>
                )}
            </Card>
        </div>
    )
}

export default TenantPickerPage