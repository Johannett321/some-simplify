import {useEffect} from "react"
import {AnimatePresence, motion} from "framer-motion"

type Props = {
    show: boolean
    onFinish: () => void
    minDurationMs?: number
}

export default function SplashScreen({ show, onFinish, minDurationMs = 1200 }: Props) {
    useEffect(() => {
        if (show) {
            const timer = setTimeout(() => onFinish(), minDurationMs)
            return () => clearTimeout(timer)
        }
    }, [show, minDurationMs, onFinish])

    return (
        <AnimatePresence>
            {show && (
                <motion.div
                    key="splashRoot"
                    className="fixed inset-0 z-[100] flex flex-col items-center justify-center bg-white"
                    initial={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 0.8, ease: "easeInOut" }}
                >
                    {/* Logo */}
                    <motion.div
                        className="text-5xl font-semibold tracking-tight text-gray-900"
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -20 }}
                        transition={{ duration: 0.8, ease: "easeOut" }}
                    >
                        appweb.appname
                    </motion.div>

                    {/* Progress line */}
                    <motion.div
                        className="mt-8 h-0.5 w-32 bg-gray-200 overflow-hidden rounded-full"
                        initial={{ scaleX: 0 }}
                        animate={{ scaleX: 1 }}
                        exit={{ opacity: 0 }}
                        transition={{ duration: 0.8, ease: "easeInOut" }}
                        style={{ originX: 0 }}
                    >
                        <motion.div
                            className="h-full bg-gray-900"
                            initial={{ x: "-100%" }}
                            animate={{ x: "100%" }}
                            transition={{ duration: 1.3, repeat: Infinity, ease: "easeInOut" }}
                        />
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    )
}
