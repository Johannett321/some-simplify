import {LoginForm} from "@/components/auth/LoginForm.tsx";

export const LoginPage = () => {

    return (
        <>
            <div className="min-h-screen grid grid-cols-1 md:grid-cols-2 w-screen">
                <div className="hidden md:block bg-cover bg-center" style={{ backgroundImage: 'url(/registerphoto.jpg)' }} />
                <div className="flex items-center justify-center p-8 bg-white dark:bg-gray-950">
                    <div className="w-full max-w-md space-y-6">
                        <h2 className="text-3xl font-bold">Logg inn</h2>
                        <p className="text-muted-foreground text-sm">Logg inn på din TemplateApp konto</p>
                        <LoginForm />
                    </div>
                </div>
            </div>
        </>
    )
}