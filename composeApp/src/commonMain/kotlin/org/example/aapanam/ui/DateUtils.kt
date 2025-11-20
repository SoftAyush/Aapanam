import kotlinx.datetime.*

fun formatDate(instant: Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    val year = dt.year
    val month = dt.month.name.lowercase().replaceFirstChar { it.uppercase() }  // Jan, Feb, Mar...
    val day = dt.day

    val hour12 = (dt.hour % 12).let { if (it == 0) 12 else it }
    val amPm = if (dt.hour < 12) "AM" else "PM"

    return "%04d, %s %02d at %02d:%02d %s".format(
        year,
        month.take(3), // MMM
        day,
        hour12,
        dt.minute,
        amPm
    )
}
