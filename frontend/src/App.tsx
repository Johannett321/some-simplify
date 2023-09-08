import React, {useState} from 'react';
import './App.css';
import axios from 'axios';
import toast, { Toaster } from 'react-hot-toast';
import ReactConfetti from "react-confetti";
import {useWindowSize} from "react-use";
import Spinner from "./components/Spinner";
import {ClerkProvider, SignIn, SignedOut, RedirectToSignIn, SignedIn, SignInButton} from "@clerk/clerk-react";


{/* Delete this if you don't want Clerk */}
if (!process.env.REACT_APP_CLERK_PUBLISHABLE_KEY) {
    throw new Error("Missing Publishable Key")
}

{/* Delete this if you don't want Clerk */}
const clerkPubKey = process.env.REACT_APP_CLERK_PUBLISHABLE_KEY;

function App() {
    axios.defaults.withCredentials = true

    const { width, height } = useWindowSize()

    {/* --------------------- YOU CAN DELETE EVERYTHING IN BETWEEN HERE --------------------- */}
    const [backendIsWorking, setBackendIsWorking] = useState(false)
    const [waitingForBackend, setWaitingForBackend] = useState(false)

    const testBackend = () => {
        setWaitingForBackend(true)
        axios.get("http://localhost:8080/api/v1/test/").then(resp => {
            if (resp.status == 200) {
                setBackendIsWorking(true)
                toast.success("Backend is working!")
            }
        }).catch(() => {
            toast.error("Backend is not working")
        }).finally(() => {
            setWaitingForBackend(false)
        })
    }

    const testAuthorization = () => {
        axios.get("http://localhost:8080/api/v1/test/authorization").then(resp => {
            if (resp.status == 200) {
                toast.success("You are authorized in backend too!")
            }
        }).catch(() => {
            toast.error("Unfortunately, you are not authorized in backend")
        })
    }

    {/* ------------------- END YOU CAN DELETE EVERYTHING IN BETWEEN HERE ------------------- */}

    return (
        <ClerkProvider publishableKey={clerkPubKey}> {/* DELETE THIS IF YOU DON'T WANT CLERK */}
            <div className="App">
                {/* --------------------- YOU CAN DELETE EVERYTHING IN BETWEEN HERE --------------------- */}

                <Toaster position={"top-center"}/>
                { backendIsWorking ? <ReactConfetti width={width} height={height}/> : "" }
                <header className="App-header">
                    {/* This section can be removed if you don't want Clerk */}
                    <SignedIn>
                        <ReactConfetti width={width} height={height}/>
                        <div className={"text-5xl"}>Seems like things are going great! 🎉</div>
                        <div className={"text-xl mt-3"}>You are logged into frontend</div>
                        <button onClick={testAuthorization} className={"text-lg mt-5 px-5 py-2 bg-blue-500"}>Check if backend see you as logged in</button>
                    </SignedIn>
                    {/* Section end */}
                    <SignedOut> {/* Delete this if you don't want Clerk */}
                        <div>
                            {backendIsWorking ?
                                <div>
                                    <div className={"text-emerald-400 text-5xl"}>Congratz!</div>
                                    <div className={"text-xl mt-3"}>Both the frontend and backend is working! 🎉</div>
                                    <SignInButton/> {/* Delete this if you don't want Clerk */}
                                </div> :
                                waitingForBackend ?
                                    <div className={"grid"}>
                                        <div>Your frontend is working</div>
                                        <Spinner className={"mt-5 justify-self-center"}/>
                                    </div> :
                                    <div className={"content-center"}>
                                        <div>Your frontend is working</div>
                                        <button onClick={testBackend} className={"text-lg px-5 py-2 bg-blue-500"}>Click to test backend & DB</button>
                                    </div>
                            }
                        </div>
                    </SignedOut> {/* Delete this if you don't want Clerk */}
                </header>

                {/* ------------------- END YOU CAN DELETE EVERYTHING IN BETWEEN HERE ------------------- */}
            </div>
        </ClerkProvider>
    );
}

export default App;
