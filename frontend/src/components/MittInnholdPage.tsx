import { useState, useEffect } from 'react'
import { ImageApi, type ImageTO } from '@/api'
import apiConfig from '@/config/ApiConfig'
import { ImageUploadForm } from './ImageUploadForm'
import { ImageGrid } from './ImageGrid'
import { EmptyState } from './EmptyState'
import { Images } from 'lucide-react'
import { toast } from 'sonner'

export const MittInnholdPage = () => {
    const [imageApi] = useState<ImageApi>(new ImageApi(apiConfig))
    const [images, setImages] = useState<ImageTO[]>([])
    const [loading, setLoading] = useState(true)

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

    const handleUploadSuccess = (newImage: ImageTO) => {
        setImages([newImage, ...images])
        toast.success('Bilde lastet opp!')
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

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-[400px]">
                <div className="text-gray-500">Laster bilder...</div>
            </div>
        )
    }

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-bold text-gray-900">Mitt innhold</h1>
                <p className="mt-2 text-gray-600">Last opp og administrer dine bilder</p>
            </div>

            <ImageUploadForm onUploadSuccess={handleUploadSuccess} />

            {images.length === 0 ? (
                <EmptyState
                    icon={Images}
                    title="Ingen bilder ennå"
                    description="Last opp ditt første bilde for å komme i gang"
                />
            ) : (
                <ImageGrid images={images} onDelete={handleDelete} />
            )}
        </div>
    )
}

export default MittInnholdPage
