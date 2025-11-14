import {useContext} from "react";
import {TenantContext} from "@/providers/TenantProvider.tsx";

export const useTenant = () => {
    return useContext(TenantContext);
};
