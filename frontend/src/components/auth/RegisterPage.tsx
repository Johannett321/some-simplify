import {RegistrationForm} from "@/components/auth/RegistrationForm.tsx";

export const RegisterPage = () => {

    return (
        <>
            <div className="min-h-screen grid grid-cols-1 md:grid-cols-2 w-screen">
                <div className="hidden md:block bg-cover bg-center" style={{ backgroundImage: 'url(/registerphoto.jpg)' }} />
                <div className="flex items-center justify-center p-8 bg-white dark:bg-gray-950">
                    <div className="w-full max-w-md space-y-6">
                        <h2 className="text-3xl font-bold">Opprett en konto</h2>
                        <p className="text-muted-foreground text-sm">Start din gratis prøveperiode i dag</p>
                        <RegistrationForm />
                    </div>
                </div>
            </div>
        </>
    )
}