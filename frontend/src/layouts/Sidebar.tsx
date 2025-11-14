import {
    Calculator,
    ChevronDown,
    ChevronRight,
    Edit3,
    Home,
    LogOut,
    PersonStandingIcon,
    PlayIcon,
    Settings,
    X,
} from 'lucide-react'
import {NavLink, useLocation} from "react-router-dom";
import {cn} from "@/lib/utils.ts";
import {Button} from "@/components/ui/button.tsx";
import {useTenant} from "@/hooks/useTenant.ts";
import {Collapsible, CollapsibleContent, CollapsibleTrigger} from "@/components/ui/collapsible.tsx";
import {useState} from "react";
import {AuthApi} from "@/api";
import apiConfig from "@/config/ApiConfig.ts";

interface SidebarProps {
    open: boolean
    onClose: () => void
}

const navigation = [
    {name: 'Hjem', href: '/', icon: Home}
]

const other = [
    {name: 'Annet1', href: '/annet1', icon: PersonStandingIcon},
    {name: 'Annet2', href: '/annet2', icon: PlayIcon}
]

export function Sidebar({open, onClose}: SidebarProps) {
    const {tenant} = useTenant()
    const location = useLocation()
    const [otherOpen, setOtherOpen] = useState(false)

    const [authApi] = useState<AuthApi>(new AuthApi(apiConfig))

    const handleSignOut = async () => {
        authApi.logout().then(() => {
            window.location.href = "/"
        })
    }

    // Check if any other route is active
    const isOtherActive = location.pathname.startsWith('/annet1') || location.pathname.startsWith('/annet2')

    return (
        <>
            {/* Mobile backdrop */}
            {open && (
                <div
                    className="fixed inset-0 bg-gray-600 bg-opacity-75 lg:hidden z-40"
                    onClick={onClose}
                />
            )}

            {/* Sidebar */}
            <div className={cn(
                "fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out",
                "lg:translate-x-0",
                open ? "translate-x-0" : "-translate-x-full lg:translate-x-0"
            )}>
                <div className="flex flex-col h-full">
                    {/* Header */}
                    <div className="flex items-center justify-between p-4 border-b border-b-gray-200">
                        <div className="flex items-center space-x-3">
                            <div className="bg-blue-600 p-2 rounded-lg">
                                <Calculator className="h-5 w-5 text-white"/>
                            </div>
                            <div>
                                <h2 className="font-bold text-gray-900 text-left">appweb.appname</h2>
                                <p className="text-xs text-gray-500">{tenant?.name}</p>
                            </div>
                        </div>
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={onClose}
                            className="lg:hidden"
                        >
                            <X className="h-4 w-4"/>
                        </Button>
                    </div>

                    {/* Navigation */}
                    <nav className="flex-1 px-4 py-4 space-y-2">
                        {/* Main navigation */}
                        {navigation.map((item) => {
                            const isActive = location.pathname === item.href
                            return (
                                <NavLink
                                    key={item.name}
                                    to={item.href}
                                    onClick={onClose}
                                    className={cn(
                                        "flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors",
                                        isActive
                                            ? "bg-blue-50 text-blue-700"
                                            : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                                    )}
                                >
                                    <item.icon className="mr-3 h-5 w-5"/>
                                    {item.name}
                                </NavLink>
                            )
                        })}

                        <Collapsible open={otherOpen} onOpenChange={setOtherOpen}>
                            <CollapsibleTrigger asChild>
                                <div
                                    className={cn(
                                        "flex items-center justify-between w-full px-3 py-2 rounded-lg text-sm font-medium transition-colors bg-transparent hover:cursor-pointer",
                                        isOtherActive
                                            ? "bg-blue-50 text-blue-700"
                                            : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                                    )}
                                >
                                    <div className="flex items-center">
                                        <Edit3 className="mr-3 h-5 w-5"/>
                                        Annet
                                    </div>
                                    {otherOpen ? (
                                        <ChevronDown className="h-4 w-4"/>
                                    ) : (
                                        <ChevronRight className="h-4 w-4"/>
                                    )}
                                </div>
                            </CollapsibleTrigger>
                            <CollapsibleContent className="space-y-1 mt-1">
                                {other.map((item) => {
                                    const isActive = location.pathname === item.href
                                    return (
                                        <NavLink
                                            key={item.name}
                                            to={item.href}
                                            onClick={onClose}
                                            className={cn(
                                                "flex items-center pl-9 pr-3 py-2 rounded-lg text-sm font-medium transition-colors",
                                                isActive
                                                    ? "bg-blue-50 text-blue-700"
                                                    : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                                            )}
                                        >
                                            <item.icon className="mr-3 h-4 w-4"/>
                                            {item.name}
                                        </NavLink>
                                    )
                                })}
                            </CollapsibleContent>
                        </Collapsible>

                        {/* Settings */}
                        <NavLink
                            to="/innstillinger"
                            onClick={onClose}
                            className={cn(
                                "flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors",
                                location.pathname === '/innstillinger'
                                    ? "bg-blue-50 text-blue-700"
                                    : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                            )}
                        >
                            <Settings className="mr-3 h-5 w-5"/>
                            Innstillinger
                        </NavLink>
                    </nav>

                    {/* Footer */}
                    <div className="p-4 border-t border-t-gray-200">
                        <Button
                            variant="ghost"
                            onClick={handleSignOut}
                            className="w-full justify-start text-gray-600 hover:text-gray-900 hover:cursor-pointer"
                        >
                            <LogOut className="mr-3 h-4 w-4"/>
                            Logg ut
                        </Button>
                    </div>
                </div>
            </div>
        </>
    )
}