import {createContext, type ReactNode, useEffect, useState} from "react";
import {TenantApi, type TenantTO} from "@/api";
import apiConfig from "@/config/ApiConfig.ts";
import TenantPickerPage from "@/components/auth/TenantPickerPage.tsx";
import axios from "axios";


type TenantProviderContextType = {
    tenant: TenantTO | null;
    loading: boolean;
};

// eslint-disable-next-line react-refresh/only-export-components
export const TenantContext = createContext<TenantProviderContextType>({
    tenant: null,
    loading: true
});

export const TenantProvider = ({children}: { children: ReactNode }) => {
    const [tenantApi] = useState<TenantApi>(new TenantApi(apiConfig))

    const [allTenants, setAllTenants] = useState<TenantTO[] | null>(null)
    const [tenant, setTenant] = useState<TenantTO | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        reloadTenants()
    }, []);

    const reloadTenants = async () => {
        const tenantsResponse = await tenantApi.getTenants()
        const tenants = tenantsResponse.data
        setAllTenants(tenants)
        if (!tenants) {
            setLoading(false);
            return
        }

        const tenantId = localStorage.getItem("tenant");
        const tenant = tenants?.find(o => o.id == tenantId)
        if (!tenant) {
            localStorage.removeItem("tenant");
            setLoading(false);
            return
        }

        setTenant(tenant)

        setLoading(false)
    }

    const onSelectTenant = async (tenant: TenantTO) => {
        localStorage.setItem("tenant", tenant.id!)
        axios.defaults.headers.common['X-Tenant-ID'] = localStorage.getItem("tenant");
        setTenant(tenant)
    }

    if (loading || !allTenants) {
        return (
            <>
                Loading...
            </>
        )
    }

    if (!tenant) {
        return (
            <>
                <TenantPickerPage tenants={allTenants} onSelectTenant={onSelectTenant} />
            </>
        )
    }

    return (
        <TenantContext.Provider value={{tenant: tenant, loading}}>
            {children}
        </TenantContext.Provider>
    );
};
