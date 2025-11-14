// src/hooks/useUser.ts
import { useContext } from "react";
import {UserContext} from "../providers/UserProvider.tsx";

export const useUser = () => {
    return useContext(UserContext);
};
