import {useForm} from 'react-hook-form'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Checkbox} from '@/components/ui/checkbox'
import {Link} from "react-router-dom";
import {AuthApi, type ErrorResponseTO} from "@/api";
import {useState} from "react";
import apiConfig from "@/config/ApiConfig.ts";
import {Alert, AlertTitle} from "@/components/ui/alert.tsx";
import {AlertCircleIcon} from "lucide-react";
import type {AxiosError} from "axios";

type FormValues = {
    email: string
    firstName: string
    lastName: string
    password: string
    eula: boolean
}

export function RegistrationForm() {
    const { register, handleSubmit, formState: { errors } } = useForm<FormValues>()
    const [authApi] = useState<AuthApi>(new AuthApi(apiConfig))
    const [error, setError] = useState<ErrorResponseTO | undefined>(undefined)

    const onSubmit = async (data: FormValues) => {
        authApi.register({
            email: data.email,
            firstName: data.firstName,
            lastName: data.lastName,
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
                    type="text"
                    placeholder="Fornavn"
                    {...register('firstName', { required: 'Fornavn er påkrevd' })}
                />
                {errors.firstName && <p className="text-sm text-red-500">{errors.firstName.message}</p>}
            </div>

            <div>
                <Input
                    type="text"
                    placeholder="Etternavn"
                    {...register('lastName', { required: 'Etternavn er påkrevd' })}
                />
                {errors.lastName && <p className="text-sm text-red-500">{errors.lastName.message}</p>}
            </div>

            <div>
                <Input
                    type="password"
                    placeholder="Passord"
                    {...register('password', { required: 'Passord er påkrevd', minLength: 6 })}
                />
                {errors.password && <p className="text-sm text-red-500">{errors.password.message}</p>}
            </div>

            <div className="flex items-center gap-2">
                <Checkbox id="eula" />
                <label htmlFor="eula" className="text-sm text-muted-foreground">
                    Jeg har lest og godtar <a href="#" className="underline">vilkar for bruk</a>
                </label>
            </div>
            {errors.eula && <p className="text-sm text-red-500">{errors.eula.message}</p>}

            <Button type="submit" className="w-full">Opprett konto</Button>
            <div className={"text-sm text-muted-foreground"}>
                Har du allerede en konto? <Link to={"/login"}>Logg inn</Link>
            </div>
        </form>
    )
}
