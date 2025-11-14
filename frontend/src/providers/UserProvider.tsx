// ✅ Fast-refresh compatible
import {createContext, type ReactNode, useEffect, useState} from "react";
import apiConfig from "@/config/ApiConfig.ts";
import {type ErrorResponseTO, UserApi, type UserTO} from "@/api";

type UserProviderContextType = {
    user: UserTO | null;
    loading: boolean;
    error: ErrorResponseTO | null;
    userApi: UserApi | undefined
};

// eslint-disable-next-line react-refresh/only-export-components
export const UserContext = createContext<UserProviderContextType>({
    user: null,
    loading: true,
    error: null,
    userApi: undefined
});

export const UserProvider = ({children}: { children: ReactNode }) => {
    const [user, setUser] = useState<UserTO | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<ErrorResponseTO | null>(null);
    const [userApi] = useState<UserApi>(new UserApi(apiConfig))

    useEffect(() => {
        userApi.getUser().then(result => {
            setUser(result.data)
        }).catch((err : ErrorResponseTO) => {
            setError(err)
            if (err.status == 403) {
                window.location.href = "/register"
            }
        }).finally(() => {
            setLoading(false)
        })
    }, []);

    return (
        <UserContext.Provider value={{user, loading, error, userApi}}>
            {children}
        </UserContext.Provider>
    );
};
