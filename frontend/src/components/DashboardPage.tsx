import {useTenant} from "@/hooks/useTenant.ts";

export const DashboardPage = () => {
    const {tenant} = useTenant()
    return (
        <div className={"flex flex-col"}>
            <div className={"text-2xl font-bold"}>
                Welcome to <span className={"text-accent"}>TemplateApp</span>
            </div>
            <div className={"text-base mb-2"}>
                Current tenant: {tenant?.name || 'none'}
            </div>
        </div>
    )
}

export default DashboardPage