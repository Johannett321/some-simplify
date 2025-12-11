import {useForm} from 'react-hook-form'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Link, useNavigate} from "react-router-dom";
import {TenantApi, type ErrorResponseTO} from "@/api";
import {useState} from "react";
import apiConfig from "@/config/ApiConfig.ts";
import {Alert, AlertTitle} from "@/components/ui/alert.tsx";
import {AlertCircleIcon} from "lucide-react";
import type {AxiosError} from "axios";
import axios from "axios";

type FormValues = {
    name: string
}

export function CreateTenantForm() {
    const { register, handleSubmit, formState: { errors } } = useForm<FormValues>()
    const [tenantApi] = useState<TenantApi>(new TenantApi(apiConfig))
    const [error, setError] = useState<ErrorResponseTO | undefined>(undefined)
    const [isSubmitting, setIsSubmitting] = useState(false)
    const navigate = useNavigate()

    const onSubmit = async (data: FormValues) => {
        setIsSubmitting(true)
        setError(undefined)

        tenantApi.createTenant({
            name: data.name
        }).then((response) => {
            const tenantId = response.data
            localStorage.setItem("tenant", tenantId)
            axios.defaults.headers.common['X-Tenant-ID'] = tenantId
            window.location.href = "/"
        }).catch((error: AxiosError<ErrorResponseTO>) => {
            setError(error.response?.data)
            setIsSubmitting(false)
        })
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 w-full">
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
                    type="text"
                    placeholder="Foretak navn"
                    {...register('name', { required: 'Foretak navn er påkrevd' })}
                />
                {errors.name && <p className="text-sm text-red-500">{errors.name.message}</p>}
            </div>

            <Button type="submit" className="w-full" disabled={isSubmitting}>
                {isSubmitting ? 'Oppretter...' : 'Opprett foretak'}
            </Button>
            <div className={"text-sm text-muted-foreground"}>
                <Link to={"/"}>Tilbake til foretakvelger</Link>
            </div>
        </form>
    )
}
