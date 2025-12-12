import { useState } from 'react'
import { type ImageTO } from '@/api'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import { Trash2, Download } from 'lucide-react'

interface ImageCardProps {
    image: ImageTO
    onDelete: (id: string) => void
}

export const ImageCard = ({ image, onDelete }: ImageCardProps) => {
    const [imageLoading, setImageLoading] = useState(true)

    const handleDownload = () => {
        window.open(image.url, '_blank')
    }

    const formatFileSize = (bytes: number) => {
        if (bytes < 1024) return bytes + ' B'
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
        return (bytes / 1024 / 1024).toFixed(1) + ' MB'
    }

    const formatDate = (dateString: string) => {
        const date = new Date(dateString)
        return date.toLocaleDateString('nb-NO', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
        })
    }

    return (
        <Card className="overflow-hidden hover:shadow-lg transition-shadow">
            <CardContent className="p-0">
                <div className="relative aspect-square bg-gray-100">
                    {imageLoading && (
                        <div className="absolute inset-0 flex items-center justify-center">
                            <div className="text-gray-400">Laster...</div>
                        </div>
                    )}
                    <img
                        src={image.url}
                        alt={image.fileName}
                        className="w-full h-full object-cover"
                        onLoad={() => setImageLoading(false)}
                        onError={() => setImageLoading(false)}
                    />
                </div>
            </CardContent>
            <CardFooter className="flex flex-col items-start gap-3 p-4">
                <div className="w-full">
                    <p className="font-medium text-sm truncate" title={image.fileName}>
                        {image.fileName}
                    </p>
                    <div className="flex items-center justify-between mt-1 text-xs text-gray-500">
                        <span>{formatFileSize(image.fileSize)}</span>
                        <span>{formatDate(image.createdAt)}</span>
                    </div>
                </div>
                <div className="flex gap-2 w-full">
                    <Button
                        variant="outline"
                        size="sm"
                        className="flex-1"
                        onClick={handleDownload}
                    >
                        <Download className="h-4 w-4 mr-1" />
                        Last ned
                    </Button>
                    <AlertDialog>
                        <AlertDialogTrigger asChild>
                            <Button variant="outline" size="sm">
                                <Trash2 className="h-4 w-4" />
                            </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                            <AlertDialogHeader>
                                <AlertDialogTitle>Slett bilde?</AlertDialogTitle>
                                <AlertDialogDescription>
                                    Er du sikker på at du vil slette dette bildet? Denne handlingen kan ikke angres.
                                </AlertDialogDescription>
                            </AlertDialogHeader>
                            <AlertDialogFooter>
                                <AlertDialogCancel>Avbryt</AlertDialogCancel>
                                <AlertDialogAction onClick={() => onDelete(image.id)}>
                                    Slett
                                </AlertDialogAction>
                            </AlertDialogFooter>
                        </AlertDialogContent>
                    </AlertDialog>
                </div>
            </CardFooter>
        </Card>
    )
}
