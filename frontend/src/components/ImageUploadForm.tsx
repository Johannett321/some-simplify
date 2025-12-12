import { useState, useRef } from 'react'
import {ImageApi, type ImageTO} from '@/api'
import apiConfig from '@/config/ApiConfig'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Upload, X } from 'lucide-react'
import { toast } from 'sonner'

interface ImageUploadFormProps {
    onUploadSuccess: (image: ImageTO) => void
}

const ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
const MAX_SIZE = 5 * 1024 * 1024 // 5MB

export const ImageUploadForm = ({ onUploadSuccess }: ImageUploadFormProps) => {
    const [imageApi] = useState<ImageApi>(new ImageApi(apiConfig))
    const [selectedFile, setSelectedFile] = useState<File | null>(null)
    const [preview, setPreview] = useState<string | null>(null)
    const [uploading, setUploading] = useState(false)
    const fileInputRef = useRef<HTMLInputElement>(null)

    const validateFile = (file: File): string | null => {
        if (!ALLOWED_TYPES.includes(file.type)) {
            return 'Kun JPG, PNG, GIF og WebP er tillatt'
        }
        if (file.size > MAX_SIZE) {
            return 'Filen er for stor (maks 5MB)'
        }
        return null
    }

    const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0]
        if (!file) return

        const error = validateFile(file)
        if (error) {
            toast.error(error)
            return
        }

        setSelectedFile(file)

        // Create preview
        const reader = new FileReader()
        reader.onload = (e) => {
            setPreview(e.target?.result as string)
        }
        reader.readAsDataURL(file)
    }

    const handleUpload = async () => {
        if (!selectedFile) return

        try {
            setUploading(true)
            const response = await imageApi.uploadImage(selectedFile)
            onUploadSuccess(response.data)

            // Reset form
            setSelectedFile(null)
            setPreview(null)
            if (fileInputRef.current) {
                fileInputRef.current.value = ''
            }
        } catch (error) {
            console.error('Upload failed:', error)
            toast.error('Opplasting feilet')
        } finally {
            setUploading(false)
        }
    }

    const handleCancel = () => {
        setSelectedFile(null)
        setPreview(null)
        if (fileInputRef.current) {
            fileInputRef.current.value = ''
        }
    }

    return (
        <Card className="p-6">
            <div className="space-y-4">
                {!selectedFile ? (
                    <div>
                        <label htmlFor="file-upload" className="cursor-pointer">
                            <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-gray-400 transition-colors">
                                <Upload className="mx-auto h-12 w-12 text-gray-400" />
                                <p className="mt-2 text-sm text-gray-600">
                                    Klikk for å velge bilde eller dra og slipp
                                </p>
                                <p className="mt-1 text-xs text-gray-500">
                                    JPG, PNG, GIF eller WebP (maks 5MB)
                                </p>
                            </div>
                        </label>
                        <input
                            ref={fileInputRef}
                            id="file-upload"
                            type="file"
                            className="hidden"
                            accept={ALLOWED_TYPES.join(',')}
                            onChange={handleFileSelect}
                        />
                    </div>
                ) : (
                    <div className="space-y-4">
                        <div className="relative">
                            <img
                                src={preview || ''}
                                alt="Preview"
                                className="w-full h-64 object-cover rounded-lg"
                            />
                            <Button
                                variant="ghost"
                                size="sm"
                                className="absolute top-2 right-2 bg-white"
                                onClick={handleCancel}
                            >
                                <X className="h-4 w-4" />
                            </Button>
                        </div>
                        <div className="flex items-center justify-between">
                            <div className="text-sm text-gray-600">
                                <p className="font-medium">{selectedFile.name}</p>
                                <p className="text-xs">
                                    {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
                                </p>
                            </div>
                            <div className="flex gap-2">
                                <Button variant="outline" onClick={handleCancel}>
                                    Avbryt
                                </Button>
                                <Button onClick={handleUpload} disabled={uploading}>
                                    {uploading ? 'Laster opp...' : 'Last opp'}
                                </Button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </Card>
    )
}
