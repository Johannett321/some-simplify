import { useState, useEffect } from 'react'
import { PostApi, type PostTO, PostStatus } from '@/api'
import apiConfig from '@/config/ApiConfig'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Calendar, Check, X, ChevronLeft, ChevronRight, Lock } from 'lucide-react'
import { toast } from 'sonner'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { EmptyState } from './EmptyState'
import { Textarea } from '@/components/ui/textarea'
import { Alert, AlertDescription } from '@/components/ui/alert'

export const SchedulePostsPage = () => {
    const navigate = useNavigate()
    const { postId } = useParams()
    const [searchParams] = useSearchParams()
    const [postApi] = useState<PostApi>(new PostApi(apiConfig))
    const [unscheduledPosts, setUnscheduledPosts] = useState<PostTO[]>([])
    const [singlePost, setSinglePost] = useState<PostTO | null>(null)
    const [currentIndex, setCurrentIndex] = useState(0)
    const [loading, setLoading] = useState(true)
    const [suggestedDate, setSuggestedDate] = useState<string>('')
    const [selectedDate, setSelectedDate] = useState<string>('')
    const [selectedTime, setSelectedTime] = useState<string>('12:00')
    const [editedText, setEditedText] = useState<string>('')
    const [mode, setMode] = useState<'batch' | 'single'>('batch')

    useEffect(() => {
        const dateParam = searchParams.get('date')

        if (postId) {
            setMode('single')
            loadSinglePost(postId)
        } else {
            setMode('batch')
            loadUnscheduledPosts()
            if (dateParam) {
                // Use the date from query parameter
                setSelectedDate(dateParam)
                setSuggestedDate(dateParam)
            } else {
                loadSuggestedDate()
            }
        }
    }, [postId, searchParams])

    useEffect(() => {
        if (mode === 'batch' && unscheduledPosts.length > 0 && unscheduledPosts[currentIndex]) {
            setEditedText(unscheduledPosts[currentIndex].text || '')
        } else if (mode === 'single' && singlePost) {
            setEditedText(singlePost.text || '')
        }
    }, [currentIndex, unscheduledPosts, singlePost, mode])

    const loadSinglePost = async (id: string) => {
        try {
            setLoading(true)
            const response = await postApi.getPost(id)
            setSinglePost(response.data)
            setEditedText(response.data.text || '')

            if (response.data.publishAt) {
                const publishDate = new Date(response.data.publishAt)
                const dateStr = publishDate.toISOString().split('T')[0]
                const timeStr = publishDate.toTimeString().slice(0, 5)
                setSelectedDate(dateStr)
                setSelectedTime(timeStr)
                setSuggestedDate(dateStr)
            } else {
                loadSuggestedDate()
            }
        } catch (error) {
            console.error('Failed to load post:', error)
            toast.error('Kunne ikke laste innlegg')
            navigate('/')
        } finally {
            setLoading(false)
        }
    }

    const loadUnscheduledPosts = async () => {
        try {
            setLoading(true)
            const response = await postApi.getPosts(undefined, undefined, PostStatus.Draft)
            setUnscheduledPosts(response.data)
        } catch (error) {
            console.error('Failed to load unscheduled posts:', error)
            toast.error('Kunne ikke laste uplanlagte innlegg')
        } finally {
            setLoading(false)
        }
    }

    const loadSuggestedDate = async () => {
        try {
            const response = await postApi.getSuggestedPublishDate()
            const dateStr = new Date(response.data.suggestedDate!).toISOString().split('T')[0]
            setSuggestedDate(dateStr)
            setSelectedDate(dateStr)
        } catch (error) {
            console.error('Failed to load suggested date:', error)
            const tomorrow = new Date()
            tomorrow.setDate(tomorrow.getDate() + 1)
            const dateStr = tomorrow.toISOString().split('T')[0]
            setSuggestedDate(dateStr)
            setSelectedDate(dateStr)
        }
    }

    const handleApprove = async () => {
        if (!selectedDate) {
            toast.error('Velg en publiseringsdato')
            return
        }

        if (!selectedTime) {
            toast.error('Velg et klokkeslett')
            return
        }

        const currentPost = mode === 'single' ? singlePost : unscheduledPosts[currentIndex]
        if (!currentPost) return

        // Combine date and time, and create ISO string
        const dateTimeString = `${selectedDate}T${selectedTime}:00`
        const publishDate = new Date(dateTimeString)

        // Check if date is in the past
        if (publishDate < new Date()) {
            toast.error('Kan ikke planlegge innlegg tilbake i tid')
            return
        }

        try {
            await postApi.updatePost(currentPost.id!, {
                text: editedText,
                publishAt: publishDate.toISOString(),
                status: PostStatus.Scheduled
            })
            toast.success('Innlegg planlagt!')

            if (mode === 'single') {
                navigate('/')
            } else {
                // Remove the scheduled post from the list
                const newPosts = unscheduledPosts.filter((_, index) => index !== currentIndex)
                setUnscheduledPosts(newPosts)

                // Adjust current index if needed
                if (currentIndex >= newPosts.length && newPosts.length > 0) {
                    setCurrentIndex(newPosts.length - 1)
                }

                // Load new suggested date for next post
                if (newPosts.length > 0) {
                    loadSuggestedDate()
                }
            }
        } catch (error) {
            console.error('Failed to schedule post:', error)
            toast.error('Kunne ikke planlegge innlegg')
        }
    }

    const handleReject = async () => {
        const currentPost = mode === 'single' ? singlePost : unscheduledPosts[currentIndex]
        if (!currentPost) return

        try {
            await postApi.updatePost(currentPost.id!, {
                status: PostStatus.Rejected
            })
            toast.success('Innlegg avslått')

            if (mode === 'single') {
                navigate('/')
            } else {
                // Remove the rejected post from the list
                const newPosts = unscheduledPosts.filter((_, index) => index !== currentIndex)
                setUnscheduledPosts(newPosts)

                // Adjust current index if needed
                if (currentIndex >= newPosts.length && newPosts.length > 0) {
                    setCurrentIndex(newPosts.length - 1)
                }
            }
        } catch (error) {
            console.error('Failed to reject post:', error)
            toast.error('Kunne ikke avslå innlegg')
        }
    }

    const goToPrevious = () => {
        if (currentIndex > 0) {
            setCurrentIndex(currentIndex - 1)
        }
    }

    const goToNext = () => {
        if (currentIndex < unscheduledPosts.length - 1) {
            setCurrentIndex(currentIndex + 1)
        }
    }

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-[400px]">
                <div className="text-gray-500">Laster innlegg...</div>
            </div>
        )
    }

    if (mode === 'batch' && unscheduledPosts.length === 0) {
        return (
            <div className="space-y-6">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">Planlegg innlegg</h1>
                        <p className="mt-2 text-gray-600">Godkjenn og planlegg dine sosiale medier innlegg</p>
                    </div>
                    <Button variant="outline" onClick={() => navigate('/')}>
                        Tilbake til kalender
                    </Button>
                </div>
                <EmptyState
                    icon={Calendar}
                    title="Ingen uplanlagte innlegg"
                    description="Alle dine innlegg er allerede planlagt eller det finnes ingen innlegg ennå"
                    action={
                        <Button onClick={() => navigate('/mitt-innhold')}>
                            Gå til mitt innhold
                        </Button>
                    }
                />
            </div>
        )
    }

    const currentPost = mode === 'single' ? singlePost : unscheduledPosts[currentIndex]
    if (!currentPost) return null

    const isPublished = currentPost.status === PostStatus.Published
    const isReadOnly = isPublished

    return (
        <div className="space-y-6 max-w-4xl mx-auto">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">
                        {mode === 'single' ? 'Rediger innlegg' : 'Planlegg innlegg'}
                    </h1>
                    <p className="mt-2 text-gray-600">
                        {mode === 'single'
                            ? (isPublished ? 'Dette innlegget er allerede publisert' : 'Rediger og oppdater innlegget')
                            : `Innlegg ${currentIndex + 1} av ${unscheduledPosts.length}`
                        }
                    </p>
                </div>
                <Button variant="outline" onClick={() => navigate('/')}>
                    Tilbake til kalender
                </Button>
            </div>

            {isPublished && (
                <Alert>
                    <Lock className="h-4 w-4" />
                    <AlertDescription>
                        Dette innlegget er allerede publisert og kan ikke lenger redigeres.
                    </AlertDescription>
                </Alert>
            )}

            <Card>
                <CardHeader>
                    <CardTitle>Forhåndsvisning av innlegg</CardTitle>
                    <CardDescription>
                        {isReadOnly
                            ? 'Se detaljer for det publiserte innlegget'
                            : 'Gjennomgå innlegget og velg en publiseringsdato'
                        }
                    </CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                    {/* Post content */}
                    <div className="space-y-4">
                        {/* Images */}
                        {currentPost.contentFiles && currentPost.contentFiles.length > 0 && (
                            <div>
                                <h3 className="text-sm font-semibold text-gray-700 mb-2">Bilder:</h3>
                                <div className="grid grid-cols-2 gap-4">
                                    {currentPost.contentFiles.map((image) => (
                                        <div key={image.id} className="relative aspect-square">
                                            <img
                                                src={image.url}
                                                alt={image.fileName || 'Post image'}
                                                className="w-full h-full object-cover rounded-lg border border-gray-200"
                                            />
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* Editable text */}
                        <div>
                            <h3 className="text-sm font-semibold text-gray-700 mb-2">Tekst:</h3>
                            <Textarea
                                value={editedText}
                                onChange={(e) => setEditedText(e.target.value)}
                                placeholder="Skriv tekst for innlegget..."
                                className="min-h-[150px] resize-none"
                                disabled={isReadOnly}
                            />
                        </div>

                        {/* Platforms */}
                        {currentPost.platforms && currentPost.platforms.length > 0 && (
                            <div>
                                <h3 className="text-sm font-semibold text-gray-700 mb-2">Plattformer:</h3>
                                <div className="flex gap-2">
                                    {currentPost.platforms.map((platform) => (
                                        <span
                                            key={platform}
                                            className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm"
                                        >
                                            {platform}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Date and Time picker */}
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label htmlFor="publish-date" className="block text-sm font-semibold text-gray-700 mb-2">
                                Publiseringsdato:
                            </label>
                            <input
                                type="date"
                                id="publish-date"
                                value={selectedDate}
                                onChange={(e) => setSelectedDate(e.target.value)}
                                min={new Date().toISOString().split('T')[0]}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                                disabled={isReadOnly}
                            />
                            {suggestedDate && selectedDate === suggestedDate && !isReadOnly && (
                                <p className="text-sm text-gray-500 mt-1">
                                    {searchParams.get('date') ? 'Valgt dato' : 'Foreslått dato'}
                                </p>
                            )}
                        </div>
                        <div>
                            <label htmlFor="publish-time" className="block text-sm font-semibold text-gray-700 mb-2">
                                Klokkeslett:
                            </label>
                            <input
                                type="time"
                                id="publish-time"
                                value={selectedTime}
                                onChange={(e) => setSelectedTime(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                                disabled={isReadOnly}
                            />
                        </div>
                    </div>
                </CardContent>
                <CardFooter className="flex justify-between">
                    {mode === 'batch' && !isReadOnly && (
                        <div className="flex gap-2">
                            <Button
                                variant="outline"
                                onClick={goToPrevious}
                                disabled={currentIndex === 0}
                            >
                                <ChevronLeft className="h-4 w-4 mr-1" />
                                Forrige
                            </Button>
                            <Button
                                variant="outline"
                                onClick={goToNext}
                                disabled={currentIndex === unscheduledPosts.length - 1}
                            >
                                Neste
                                <ChevronRight className="h-4 w-4 ml-1" />
                            </Button>
                        </div>
                    )}
                    {mode === 'single' && isReadOnly && <div />}
                    {!isReadOnly && (
                        <div className="flex gap-2">
                            <Button variant="destructive" onClick={handleReject}>
                                <X className="h-4 w-4 mr-2" />
                                Avslå
                            </Button>
                            <Button onClick={handleApprove}>
                                <Check className="h-4 w-4 mr-2" />
                                {mode === 'single' ? 'Oppdater' : 'Godkjenn'}
                            </Button>
                        </div>
                    )}
                    {isReadOnly && (
                        <Button onClick={() => navigate('/')}>
                            Tilbake til kalender
                        </Button>
                    )}
                </CardFooter>
            </Card>
        </div>
    )
}

export default SchedulePostsPage
