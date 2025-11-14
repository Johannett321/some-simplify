import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover.tsx";
import {Button} from "@/components/ui/button.tsx";
import {cn} from "@/lib/utils.ts";
import {CalendarIcon} from "lucide-react";
import {Calendar} from "@/components/ui/calendar.tsx";
import {format} from "date-fns";
import {useState} from "react";
import {nb} from "date-fns/locale";

export const DatePickerComponent = ({ value, onChange, disabled }: {
    value?: string
    onChange: (val: string) => void
    disabled?: boolean
}) => {
    const [open, setOpen] = useState(false)

    return (
        <Popover open={open} onOpenChange={setOpen}>
            <PopoverTrigger asChild>
                <Button
                    variant="outline"
                    disabled={disabled}
                    className={cn(
                        "justify-start text-left font-normal max-w-max",
                        !value && "text-muted-foreground"
                    )}
                >
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {value
                        ? format(new Date(value), "dd.MM.yyyy", { locale: nb })
                        : <span>Velg dato</span>}
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0">
                <Calendar
                    mode="single"
                    selected={value ? new Date(value) : undefined}
                    onSelect={(date) => {
                        if (date) {
                            onChange(format(date, "yyyy-MM-dd"))
                            setOpen(false) // auto-close
                        }
                    }}
                    initialFocus
                />
            </PopoverContent>
        </Popover>
    )
}

export default DatePickerComponent