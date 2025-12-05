import {useForm} from 'react-hook-form'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Link} from "react-router-dom";
import {useState} from "react";
import {AuthApi, type ErrorResponseTO} from "@/api";
import apiConfig from "@/config/ApiConfig.ts";
import {Alert, AlertTitle} from "@/components/ui/alert.tsx";
import {AlertCircleIcon} from "lucide-react";
import type {AxiosError} from "axios";

type FormValues = {
    email: string
    password: string
}

export function LoginForm() {
    const { register, handleSubmit, formState: { errors } } = useForm<FormValues>()
    const [authApi] = useState<AuthApi>(new AuthApi(apiConfig))
    const [error, setError] = useState<ErrorResponseTO | undefined>(undefined)


    const onSubmit = async (data: FormValues) => {
        authApi.login({
            email: data.email,
            password: data.password
        }).then(() => {
            window.location.href = "/"
        }).catch((error: AxiosError<ErrorResponseTO>) => {
            setError(error.response?.data)
        })
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 w-full max-w-md">
            {error && (
                <Alert variant={"destructive"}>
                    <div className={"flex flex-row items-center gap-2 h-full"}>
                        <AlertCircleIcon className={"h-4 w-4 flex-shrink-0"} />
                        <AlertTitle className={"mb-0"}>
                            {error.error}
                        </AlertTitle>
                    </div>
                </Alert>
            )}
            <div>
                <Input
                    type="email"
                    placeholder="Epost"
                    {...register('email', { required: 'Epost er påkrevd' })}
                />
                {errors.email && <p className="text-sm text-red-500">{errors.email.message}</p>}
            </div>

            <div>
                <Input
                    type="password"
                    placeholder="Passord"
                    {...register('password', { required: 'Passord er påkrevd', minLength: 6 })}
                />
                {errors.password && <p className="text-sm text-red-500">{errors.password.message}</p>}
            </div>

            <Button type="submit" className="w-full">Logg inn</Button>
            <div className={"text-sm text-muted-foreground"}>
                Har du ingen konto? <Link to={"/register"}>Registrer deg</Link>
            </div>
        </form>
    )
}
