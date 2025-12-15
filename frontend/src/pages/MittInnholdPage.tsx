import { useState, useEffect } from 'react'
import { ImageApi, type ImageTO } from '@/api'
import apiConfig from '@/config/ApiConfig.ts'
import { ImageUploadDialog } from '../components/ImageUploadDialog.tsx'
import { ImageGrid } from '../components/ImageGrid.tsx'
import { EmptyState } from '../components/EmptyState.tsx'
import { Button } from '@/components/ui/button.tsx'
import { Images, Upload } from 'lucide-react'
import { toast } from 'sonner'

export const MittInnholdPage = () => {
    const [imageApi] = useState<ImageApi>(new ImageApi(apiConfig))
    const [images, setImages] = useState<ImageTO[]>([])
    const [loading, setLoading] = useState(true)
    const [dialogOpen, setDialogOpen] = useState(false)
    const [isDragging, setIsDragging] = useState(false)
    const [droppedFiles, setDroppedFiles] = useState<File[] | null>(null)

    useEffect(() => {
        loadImages()
    }, [])

    const loadImages = async () => {
        try {
            setLoading(true)
            const response = await imageApi.getUserImages()
            setImages(response.data)
        } catch (error) {
            console.error('Failed to load images:', error)
            toast.error('Kunne ikke laste bilder')
        } finally {
            setLoading(false)
        }
    }

    function handleUploadSuccess(newImages: ImageTO[]) {
        setImages([...newImages, ...images])
    }

    const handleDelete = async (id: string) => {
        try {
            await imageApi.deleteImage(id)
            setImages(images.filter(img => img.id !== id))
            toast.success('Bilde slettet')
        } catch (error) {
            console.error('Failed to delete image:', error)
            toast.error('Kunne ikke slette bilde')
        }
    }

    const handleDialogOpenChange = (open: boolean) => {
        setDialogOpen(open)
        if (!open) {
            setDroppedFiles(null)
        }
    }

    // Page-level drag and drop handlers
    const handlePageDragOver = (e: React.DragEvent) => {
        e.preventDefault()
        e.stopPropagation()

        // Check if dragging files
        if (e.dataTransfer.types.includes('Files')) {
            setIsDragging(true)
        }
    }

    const handlePageDragLeave = (e: React.DragEvent) => {
        e.preventDefault()
        e.stopPropagation()
        // Only hide if leaving the page container
        if (e.currentTarget === e.target) {
            setIsDragging(false)
        }
    }

    const handlePageDrop = (e: React.DragEvent) => {
        e.preventDefault()
        e.stopPropagation()
        setIsDragging(false)

        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            const files = Array.from(e.dataTransfer.files)
            const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
            const maxSize = 5 * 1024 * 1024 // 5MB

            const validFiles: File[] = []

            files.forEach(file => {
                if (!allowedTypes.includes(file.type)) {
                    toast.error(`${file.name}: Kun JPG, PNG, GIF og WebP er tillatt. Ikke ` + file.type)
                    return
                }

                if (file.size > maxSize) {
                    toast.error(`${file.name}: Filen er for stor (maks 5MB)`)
                    return
                }

                validFiles.push(file)
            })

            if (validFiles.length > 0) {
                setDroppedFiles(validFiles)
                setDialogOpen(true)
            }
        }
    }

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-[400px]">
                <div className="text-gray-500">Laster bilder...</div>
            </div>
        )
    }

    return (
        <div
            className="space-y-8 relative"
            onDragOver={handlePageDragOver}
            onDragLeave={handlePageDragLeave}
            onDrop={handlePageDrop}
        >
            {/* Drag overlay */}
            {isDragging && (
                <div className="fixed inset-0 z-50 bg-primary/10 backdrop-blur-sm flex items-center justify-center pointer-events-none">
                    <div className="bg-white rounded-lg shadow-lg p-8 text-center border-2 border-dashed border-primary">
                        <Upload className="mx-auto h-16 w-16 text-primary mb-4" />
                        <p className="text-lg font-semibold text-gray-900">
                            Slipp for å laste opp bilder
                        </p>
                        <p className="text-sm text-gray-500 mt-2">
                            JPG, PNG, GIF eller WebP (maks 5MB per fil)
                        </p>
                    </div>
                </div>
            )}

            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Mitt innhold</h1>
                    <p className="mt-2 text-gray-600">Last opp og administrer dine bilder</p>
                </div>
                <Button onClick={() => setDialogOpen(true)}>
                    <Upload className="h-4 w-4 mr-2" />
                    Last opp bilder
                </Button>
            </div>

            {images.length === 0 ? (
                <EmptyState
                    icon={Images}
                    title="Ingen bilder ennå"
                    description="Last opp dine første bilder for å komme i gang"
                    action={
                        <Button onClick={() => setDialogOpen(true)}>
                            <Upload className="h-4 w-4 mr-2" />
                            Last opp bilder
                        </Button>
                    }
                />
            ) : (
                <ImageGrid images={images} onDelete={handleDelete} />
            )}

            <ImageUploadDialog
                open={dialogOpen}
                onOpenChange={handleDialogOpenChange}
                onUploadSuccess={handleUploadSuccess}
                initialFiles={droppedFiles}
            />
        </div>
    )
}

export default MittInnholdPage
