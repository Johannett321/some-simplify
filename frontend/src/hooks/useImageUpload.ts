import { useState } from 'react'
import { ImageApi, type ImageTO } from '@/api'
import apiConfig from '@/config/ApiConfig'
import { toast } from 'sonner'

const ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
const MAX_SIZE = 5 * 1024 * 1024 // 5MB

export const useImageUpload = (onUploadSuccess: (image: ImageTO) => void) => {
    const [imageApi] = useState<ImageApi>(new ImageApi(apiConfig))
    const [selectedFile, setSelectedFile] = useState<File | null>(null)
    const [preview, setPreview] = useState<string | null>(null)
    const [uploading, setUploading] = useState(false)

    const validateFile = (file: File): string | null => {
        if (!ALLOWED_TYPES.includes(file.type)) {
            return 'Kun JPG, PNG, GIF og WebP er tillatt. Ikke ' + file.type
        }
        if (file.size > MAX_SIZE) {
            return 'Filen er for stor (maks 5MB)'
        }
        return null
    }

    const handleFileSelect = (file: File) => {
        const error = validateFile(file)
        if (error) {
            toast.error(error)
            return false
        }

        setSelectedFile(file)

        // Create preview
        const reader = new FileReader()
        reader.onload = (e) => {
            setPreview(e.target?.result as string)
        }
        reader.readAsDataURL(file)
        return true
    }

    const handleUpload = async () => {
        if (!selectedFile) return

        try {
            setUploading(true)
            const response = await imageApi.uploadImage(selectedFile)
            onUploadSuccess(response.data)
            toast.success('Bilde lastet opp!')

            // Reset form
            resetForm()
        } catch (error) {
            console.error('Upload failed:', error)
            toast.error('Opplasting feilet')
        } finally {
            setUploading(false)
        }
    }

    const resetForm = () => {
        setSelectedFile(null)
        setPreview(null)
    }

    return {
        selectedFile,
        preview,
        uploading,
        handleFileSelect,
        handleUpload,
        resetForm,
        validateFile,
    }
}
