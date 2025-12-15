import { useState, useEffect } from 'react'
import { PostApi, type PostTO, PostStatus } from '@/api'
import apiConfig from '@/config/ApiConfig'
import { Button } from '@/components/ui/button'
import { ChevronLeft, ChevronRight, Calendar } from 'lucide-react'
import { toast } from 'sonner'
import { useNavigate } from 'react-router-dom'

export const DashboardPage = () => {
    const navigate = useNavigate()
    const [postApi] = useState<PostApi>(new PostApi(apiConfig))
    const [posts, setPosts] = useState<PostTO[]>([])
    const [loading, setLoading] = useState(true)
    const [currentDate, setCurrentDate] = useState(new Date())

    useEffect(() => {
        loadPosts()
    }, [currentDate])

    const loadPosts = async () => {
        try {
            setLoading(true)
            const firstDay = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1)
            const lastDay = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0)

            const response = await postApi.getPosts(
                firstDay.toISOString().split('T')[0],
                lastDay.toISOString().split('T')[0]
            )
            setPosts(response.data)
        } catch (error) {
            console.error('Failed to load posts:', error)
            toast.error('Kunne ikke laste innlegg')
        } finally {
            setLoading(false)
        }
    }

    const previousMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1))
    }

    const nextMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1))
    }

    const getPostsForDate = (date: Date): PostTO[] => {
        return posts.filter(post => {
            if (!post.publishAt) return false
            const postDate = new Date(post.publishAt)
            return postDate.toDateString() === date.toDateString()
        })
    }

    const renderCalendar = () => {
        const year = currentDate.getFullYear()
        const month = currentDate.getMonth()
        const firstDay = new Date(year, month, 1)
        const lastDay = new Date(year, month + 1, 0)
        const daysInMonth = lastDay.getDate()
        const startingDayOfWeek = firstDay.getDay()

        // Adjust to make Monday the first day (0 = Sunday -> 6, 1 = Monday -> 0)
        const adjustedStartDay = startingDayOfWeek === 0 ? 6 : startingDayOfWeek - 1

        const days = []

        // Empty cells for days before the month starts
        for (let i = 0; i < adjustedStartDay; i++) {
            days.push(<div key={`empty-${i}`} className="h-32 border border-gray-200 bg-gray-50" />)
        }

        // Actual days of the month
        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(year, month, day)
            const postsForDay = getPostsForDate(date)
            const isToday = date.toDateString() === new Date().toDateString()

            days.push(
                <div
                    key={day}
                    className={`h-32 border border-gray-200 p-2 hover:bg-gray-50 cursor-pointer transition-colors ${
                        isToday ? 'bg-blue-50' : 'bg-white'
                    }`}
                    onClick={() => {
                        if (postsForDay.length === 0) {
                            navigate('/schedule-posts')
                        }
                    }}
                >
                    <div className={`text-sm font-semibold mb-1 ${isToday ? 'text-blue-600' : 'text-gray-900'}`}>
                        {day}
                    </div>
                    <div className="space-y-1 overflow-y-auto max-h-20">
                        {postsForDay.map((post) => (
                            <div
                                key={post.id}
                                className={`text-xs p-1 rounded truncate ${
                                    post.status === PostStatus.Scheduled
                                        ? 'bg-blue-100 text-blue-800'
                                        : post.status === PostStatus.Published
                                        ? 'bg-green-100 text-green-800'
                                        : 'bg-gray-100 text-gray-800'
                                }`}
                                title={post.text || 'Ingen tekst'}
                            >
                                {post.text?.substring(0, 30) || 'Ingen tekst'}
                            </div>
                        ))}
                    </div>
                </div>
            )
        }

        return days
    }

    const monthNames = [
        'Januar', 'Februar', 'Mars', 'April', 'Mai', 'Juni',
        'Juli', 'August', 'September', 'Oktober', 'November', 'Desember'
    ]

    const weekDays = ['Man', 'Tir', 'Ons', 'Tor', 'Fre', 'Lør', 'Søn']

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-[400px]">
                <div className="text-gray-500">Laster kalender...</div>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Innholdskalender</h1>
                    <p className="mt-2 text-gray-600">Planlegg og administrer dine sosiale medier innlegg</p>
                </div>
                <Button onClick={() => navigate('/schedule-posts')}>
                    <Calendar className="h-4 w-4 mr-2" />
                    Planlegg innlegg
                </Button>
            </div>

            <div className="bg-white rounded-lg shadow">
                <div className="flex items-center justify-between p-4 border-b">
                    <Button variant="outline" size="sm" onClick={previousMonth}>
                        <ChevronLeft className="h-4 w-4" />
                    </Button>
                    <h2 className="text-xl font-semibold">
                        {monthNames[currentDate.getMonth()]} {currentDate.getFullYear()}
                    </h2>
                    <Button variant="outline" size="sm" onClick={nextMonth}>
                        <ChevronRight className="h-4 w-4" />
                    </Button>
                </div>

                <div className="p-4">
                    <div className="grid grid-cols-7 gap-0 mb-2">
                        {weekDays.map(day => (
                            <div key={day} className="text-center text-sm font-semibold text-gray-600 p-2">
                                {day}
                            </div>
                        ))}
                    </div>
                    <div className="grid grid-cols-7 gap-0">
                        {renderCalendar()}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default DashboardPage