import {UserProvider} from "../providers/UserProvider.tsx";
import {Outlet} from "react-router-dom";
import {Sidebar} from "@/layouts/Sidebar.tsx";
import {useState} from "react";
import {TenantProvider} from "@/providers/TenantProvider.tsx";

export const MainLayout = () => {
    const [sidebarOpen, setSidebarOpen] = useState<boolean>(false)
    return (
        <UserProvider>
            <TenantProvider>
                <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
                <div className={"min-h-screen min-w-screen p-10 text-left lg:pl-72"}>
                    <div className={"container mx-auto"}>
                        <Outlet />
                    </div>
                </div>
            </TenantProvider>
        </UserProvider>
    )
}

export default MainLayout