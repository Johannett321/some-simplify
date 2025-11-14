import { DivideIcon as LucideIcon } from 'lucide-react'

interface EmptyStateProps {
    icon: typeof LucideIcon
    title: string
    description: string
    action?: React.ReactNode
}

export function EmptyState({ icon: Icon, title, description, action }: EmptyStateProps) {
    return (
        <div className="text-center py-12">
            <div className="mx-auto h-12 w-12 text-gray-400 mb-4">
                <Icon className="h-12 w-12" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">{title}</h3>
            <p className="text-gray-600 mb-6 max-w-md mx-auto">{description}</p>
            {action && <div>{action}</div>}
        </div>
    )
}