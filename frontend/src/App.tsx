import React, {useState} from 'react';
import './App.css';
import axios from 'axios';
import toast, { Toaster } from 'react-hot-toast';
import ReactConfetti from "react-confetti";
import {useWindowSize} from "react-use";
import Spinner from "./components/Spinner";

function App() {
    const { width, height } = useWindowSize()
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

    return (
        <div className="App">
            <Toaster position={"top-center"}/>
            { backendIsWorking ? <ReactConfetti width={width} height={height}/> : "" }
            <header className="App-header">
                <div>
                    {backendIsWorking ?
                        <div>Congratz! Both the backend and frontend is working!</div> :
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
            </header>
        </div>
    );
}

export default App;
