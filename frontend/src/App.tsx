import './App.css'
import {BrowserRouter, Route, Routes} from "react-router-dom";
import {RegisterPage} from "./components/auth/RegisterPage.tsx";
import MainLayout from "./layouts/MainLayout.tsx";
import DashboardPage from "./components/DashboardPage.tsx";
import {LoginPage} from "@/components/auth/LoginPage.tsx";
import {CreateTenantPage} from "@/components/auth/CreateTenantPage.tsx";
import {Toaster} from "@/components/ui/sonner.tsx";
import {useState} from "react";
import SplashScreen from "@/components/SplashScreen.tsx";
import axios from "axios";
import MittInnholdPage from "@/components/MittInnholdPage.tsx";

function App() {
    axios.defaults.withCredentials = true
    const [showSplash, setShowSplash] = useState(true)

    return (
        <>
            <Toaster />
            <BrowserRouter>
                <SplashScreen show={showSplash} onFinish={() => setShowSplash(false)} />
                <Routes>
                    <Route path={"/"} element={< MainLayout/>} >
                        <Route path={"/"} element={<DashboardPage />} />
                        <Route path={"/mitt-innhold"} element={<MittInnholdPage />} />
                    </Route>
                    <Route path={"/register"} element={<RegisterPage />} />
                    <Route path={"/login"} element={<LoginPage />} />
                    <Route path={"/opprett-foretak"} element={<CreateTenantPage />} />
                </Routes>
            </BrowserRouter>
        </>
    )
}

export default App
