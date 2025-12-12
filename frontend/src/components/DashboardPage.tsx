import {useTenant} from "@/hooks/useTenant.ts";
import {useUser} from "@/hooks/useUser.ts";

export const DashboardPage = () => {
    const {tenant} = useTenant()
    const {user} = useUser()
    return (
        <div className={"flex flex-col"}>
            <div className={"text-2xl font-bold"}>
                Velkommen <span className={"text-accent"}>{user?.firstName}</span>
            </div>
            <div className={"text-base mb-2"}>
                Current tenant: {tenant?.name || 'none'}
            </div>
        </div>
    )
}

export default DashboardPage