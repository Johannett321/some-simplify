import {CreateTenantForm} from "@/components/auth/CreateTenantForm.tsx";

export const CreateTenantPage = () => {
    return (
        <>
            <div className={"w-screen h-screen flex justify-center items-center"}>
                <img className={"absolute top-0 left-0 w-screen h-screen object-cover -z-10"} src={"/registerphoto.jpg"}/>
                <div className="w-full max-w-md space-y-6 bg-white dark:bg-gray-950 p-8 rounded-lg shadow-lg">
                    <h2 className="text-3xl font-bold">Opprett nytt sted</h2>
                    <p className="text-muted-foreground text-sm">Fyll inn informasjon om ditt sted</p>
                    <CreateTenantForm />
                </div>
            </div>
        </>
    )
}

export default CreateTenantPage
