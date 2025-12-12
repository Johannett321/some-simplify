import { type ImageTO } from '@/api'
import { ImageCard } from './ImageCard'

interface ImageGridProps {
    images: ImageTO[]
    onDelete: (id: string) => void
}

export const ImageGrid = ({ images, onDelete }: ImageGridProps) => {
    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {images.map((image) => (
                <ImageCard key={image.id} image={image} onDelete={onDelete} />
            ))}
        </div>
    )
}
