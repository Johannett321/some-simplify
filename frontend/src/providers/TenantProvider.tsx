import {createContext, type ReactNode, useEffect, useState} from "react";
import {TenantApi, type TenantTO} from "@/api";
import apiConfig from "@/config/ApiConfig.ts";

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
    const [tenant, setTenant] = useState<TenantTO | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadTenantData()
    }, []);

    const loadTenantData = () => {
        tenantApi.getTenant().then(res => {
            setTenant(res.data)
        }).finally(() => setLoading(false))
    }

    if (loading) {
        return (
            <>
                Loading...
            </>
        )
    }

    return (
        <TenantContext.Provider value={{tenant: tenant, loading}}>
            {children}
        </TenantContext.Provider>
    );
};
