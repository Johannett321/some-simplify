import {useForm} from 'react-hook-form'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Link} from "react-router-dom";
import {useState} from "react";
import {AuthApi} from "@/api";
import apiConfig from "@/config/ApiConfig.ts";

type FormValues = {
    email: string
    password: string
}

export function LoginForm() {
    const { register, handleSubmit, formState: { errors } } = useForm<FormValues>()
    const [authApi] = useState<AuthApi>(new AuthApi(apiConfig))


    const onSubmit = async (data: FormValues) => {
        authApi.login({
            email: data.email,
            password: data.password
        }).then(() => {
            window.location.href = "/"
        })
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 w-full max-w-md">
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
