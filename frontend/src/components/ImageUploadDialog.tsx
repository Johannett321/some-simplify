import { useRef, useState, useEffect } from 'react'
import { ImageApi, type ImageTO } from '@/api'
import apiConfig from '@/config/ApiConfig'
import { Button } from '@/components/ui/button'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import { Upload, X, CheckCircle2, Loader2 } from 'lucide-react'
import { toast } from 'sonner'

const ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
const MAX_SIZE = 5 * 1024 * 1024 // 5MB

interface ImageUploadDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    onUploadSuccess: (images: ImageTO[]) => void
    initialFiles?: File[] | null
}

interface FileWithPreview {
    file: File
    preview: string
    status: 'pending' | 'uploading' | 'success' | 'error'
    error?: string
}

export const ImageUploadDialog = ({ open, onOpenChange, onUploadSuccess, initialFiles }: ImageUploadDialogProps) => {
    const fileInputRef = useRef<HTMLInputElement>(null)
    const [isDragging, setIsDragging] = useState(false)
    const [files, setFiles] = useState<FileWithPreview[]>([])
    const [uploading, setUploading] = useState(false)
    const [imageApi] = useState<ImageApi>(new ImageApi(apiConfig))

    // Handle initial files when dialog opens
    useEffect(() => {
        if (open && initialFiles && initialFiles.length > 0) {
            handleFilesSelect(initialFiles)
        } else if (!open) {
            // Reset when dialog closes
            resetForm()
        }
    }, [open, initialFiles])

    const validateFile = (file: File): string | null => {
        if (!ALLOWED_TYPES.includes(file.type)) {
            return 'Kun JPG, PNG, GIF og WebP er tillatt. Ikke ' + file.type
        }
        if (file.size > MAX_SIZE) {
            return 'Filen er for stor (maks 5MB)'
        }
        return null
    }

    const handleFilesSelect = (selectedFiles: File[] | FileList) => {
        const filesArray = Array.from(selectedFiles)
        const validFiles: FileWithPreview[] = []

        filesArray.forEach(file => {
            const error = validateFile(file)
            if (error) {
                toast.error(`${file.name}: ${error}`)
                return
            }

            const reader = new FileReader()
            reader.onload = (e) => {
                validFiles.push({
                    file,
                    preview: e.target?.result as string,
                    status: 'pending'
                })

                if (validFiles.length === filesArray.filter(f => !validateFile(f)).length) {
                    setFiles(validFiles)
                }
            }
            reader.readAsDataURL(file)
        })
    }

    const handleFileInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        if (event.target.files && event.target.files.length > 0) {
            handleFilesSelect(event.target.files)
        }
    }

    const handleDragOver = (e: React.DragEvent) => {
        e.preventDefault()
        e.stopPropagation()
        setIsDragging(true)
    }

    const handleDragLeave = (e: React.DragEvent) => {
        e.preventDefault()
        e.stopPropagation()
        setIsDragging(false)
    }

    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault()
        e.stopPropagation()
        setIsDragging(false)

        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            handleFilesSelect(e.dataTransfer.files)
        }
    }

    const removeFile = (index: number) => {
        setFiles(files.filter((_, i) => i !== index))
    }

    const handleUpload = async () => {
        if (files.length === 0) return

        try {
            setUploading(true)
            const uploadedImages: ImageTO[] = []

            for (let i = 0; i < files.length; i++) {
                const fileWithPreview = files[i]

                // Update status to uploading
                setFiles(prev => prev.map((f, idx) =>
                    idx === i ? { ...f, status: 'uploading' as const } : f
                ))

                try {
                    const response = await imageApi.uploadImage(fileWithPreview.file)
                    uploadedImages.push(response.data)

                    // Update status to success
                    setFiles(prev => prev.map((f, idx) =>
                        idx === i ? { ...f, status: 'success' as const } : f
                    ))
                } catch (error) {
                    console.error('Upload failed:', error)

                    // Update status to error
                    setFiles(prev => prev.map((f, idx) =>
                        idx === i ? {
                            ...f,
                            status: 'error' as const,
                            error: 'Opplasting feilet'
                        } : f
                    ))
                }
            }

            if (uploadedImages.length > 0) {
                onUploadSuccess(uploadedImages)
                toast.success(`${uploadedImages.length} ${uploadedImages.length === 1 ? 'bilde' : 'bilder'} lastet opp!`)
            }

            if (uploadedImages.length === files.length) {
                // All files uploaded successfully, close dialog
                onOpenChange(false)
            } else {
                // Some files failed, keep dialog open to show errors
                toast.error(`${files.length - uploadedImages.length} ${files.length - uploadedImages.length === 1 ? 'bilde' : 'bilder'} feilet`)
            }
        } finally {
            setUploading(false)
        }
    }

    const resetForm = () => {
        setFiles([])
        if (fileInputRef.current) {
            fileInputRef.current.value = ''
        }
    }

    const handleClose = () => {
        if (!uploading) {
            resetForm()
            onOpenChange(false)
        }
    }

    return (
        <Dialog open={open} onOpenChange={handleClose}>
            <DialogContent className="sm:max-w-[600px] max-h-[80vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle>Last opp bilder</DialogTitle>
                    <DialogDescription>
                        Dra og slipp bilder her, eller klikk for å velge flere.
                    </DialogDescription>
                </DialogHeader>

                <div className="space-y-4">
                    {files.length === 0 ? (
                        <div>
                            <label htmlFor="file-upload-dialog" className="cursor-pointer">
                                <div
                                    className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
                                        isDragging
                                            ? 'border-primary bg-primary/5'
                                            : 'border-gray-300 hover:border-gray-400'
                                    }`}
                                    onDragOver={handleDragOver}
                                    onDragLeave={handleDragLeave}
                                    onDrop={handleDrop}
                                >
                                    <Upload className="mx-auto h-12 w-12 text-gray-400" />
                                    <p className="mt-2 text-sm text-gray-600">
                                        Dra og slipp bilder her
                                    </p>
                                    <p className="mt-1 text-xs text-gray-500">
                                        eller klikk for å velge filer
                                    </p>
                                    <p className="mt-2 text-xs text-gray-500">
                                        JPG, PNG, GIF eller WebP (maks 5MB per fil)
                                    </p>
                                </div>
                            </label>
                            <input
                                ref={fileInputRef}
                                id="file-upload-dialog"
                                type="file"
                                className="hidden"
                                accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                                onChange={handleFileInputChange}
                                multiple
                            />
                        </div>
                    ) : (
                        <div className="space-y-4">
                            <div className="grid grid-cols-2 gap-4 max-h-96 overflow-y-auto">
                                {files.map((fileWithPreview, index) => (
                                    <div key={index} className="relative group">
                                        <img
                                            src={fileWithPreview.preview}
                                            alt={fileWithPreview.file.name}
                                            className="w-full h-32 object-cover rounded-lg"
                                        />
                                        {!uploading && fileWithPreview.status === 'pending' && (
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                className="absolute top-1 right-1 bg-white opacity-0 group-hover:opacity-100 transition-opacity"
                                                onClick={() => removeFile(index)}
                                            >
                                                <X className="h-4 w-4" />
                                            </Button>
                                        )}
                                        {fileWithPreview.status === 'uploading' && (
                                            <div className="absolute inset-0 bg-black/50 rounded-lg flex items-center justify-center">
                                                <Loader2 className="h-8 w-8 text-white animate-spin" />
                                            </div>
                                        )}
                                        {fileWithPreview.status === 'success' && (
                                            <div className="absolute inset-0 bg-green-500/20 rounded-lg flex items-center justify-center">
                                                <CheckCircle2 className="h-8 w-8 text-green-600" />
                                            </div>
                                        )}
                                        {fileWithPreview.status === 'error' && (
                                            <div className="absolute inset-0 bg-red-500/20 rounded-lg flex items-center justify-center">
                                                <X className="h-8 w-8 text-red-600" />
                                            </div>
                                        )}
                                        <p className="text-xs text-gray-600 mt-1 truncate" title={fileWithPreview.file.name}>
                                            {fileWithPreview.file.name}
                                        </p>
                                    </div>
                                ))}
                            </div>
                            <div className="flex items-center justify-between pt-2 border-t">
                                <div className="text-sm text-gray-600">
                                    <p>{files.length} {files.length === 1 ? 'fil' : 'filer'} valgt</p>
                                </div>
                                <div className="flex gap-2">
                                    <Button variant="outline" onClick={handleClose} disabled={uploading}>
                                        {uploading ? 'Lukk' : 'Avbryt'}
                                    </Button>
                                    <Button onClick={handleUpload} disabled={uploading || files.length === 0}>
                                        {uploading ? 'Laster opp...' : 'Last opp alle'}
                                    </Button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </DialogContent>
        </Dialog>
    )
}
