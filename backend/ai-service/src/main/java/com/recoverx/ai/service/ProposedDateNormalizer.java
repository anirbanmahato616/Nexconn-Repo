package com.recoverx.ai.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProposedDateNormalizer {
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM d uuuu", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d uuuu", Locale.ENGLISH),
            new DateTimeFormatterBuilder().appendPattern("d MMMM")
                    .parseDefaulting(ChronoField.YEAR, 2000).toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().appendPattern("d MMM")
                    .parseDefaulting(ChronoField.YEAR, 2000).toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().appendPattern("MMMM d")
                    .parseDefaulting(ChronoField.YEAR, 2000).toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().appendPattern("MMM d")
                    .parseDefaulting(ChronoField.YEAR, 2000).toFormatter(Locale.ENGLISH)
    );

    private static final Pattern DAYS_FROM_NOW =
            Pattern.compile("in\\s+(\\d+)\\s+days?");
    private static final Pattern NEXT_WEEKDAY = Pattern.compile(
            "next\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)");
    private static final Pattern ORDINAL_SUFFIX =
            Pattern.compile("(?i)\\b(\\d{1,2})(st|nd|rd|th)\\b");

    public String normalize(String dateText, Instant messageTimestamp, String timezone) {
        if (dateText == null || dateText.isBlank() || messageTimestamp == null
                || timezone == null || timezone.isBlank()) {
            return null;
        }

        final ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezone);
        } catch (Exception ex) {
            return null;
        }

        LocalDate referenceDate = messageTimestamp.atZone(zoneId).toLocalDate();
        String text = dateText.trim()
                .replaceAll("(?i)\\b(on|by)\\s+", "")
                .replaceAll(",", "")
                .replaceAll("\\s+", " ")
                .trim();
        String normalizedText = text.toLowerCase(Locale.ENGLISH);

        if (normalizedText.equals("today")) return format(referenceDate);
        if (normalizedText.equals("tomorrow")) return format(referenceDate.plusDays(1));
        if (normalizedText.equals("day after tomorrow")) return format(referenceDate.plusDays(2));

        Matcher daysMatcher = DAYS_FROM_NOW.matcher(normalizedText);
        if (daysMatcher.matches()) {
            try {
                return format(referenceDate.plusDays(Long.parseLong(daysMatcher.group(1))));
            } catch (RuntimeException ex) {
                return null;
            }
        }

        Matcher weekdayMatcher = NEXT_WEEKDAY.matcher(normalizedText);
        if (weekdayMatcher.matches()) {
            try {
                DayOfWeek day = DayOfWeek.valueOf(
                        weekdayMatcher.group(1).toUpperCase(Locale.ENGLISH));
                return format(referenceDate.with(TemporalAdjusters.next(day)));
            } catch (RuntimeException ex) {
                return null;
            }
        }

        // Support ordinal dates such as "20th November 2026".
        String parseableText = ORDINAL_SUFFIX.matcher(text).replaceAll("$1");

        // First accept ISO dates, e.g. 2026-11-20.
        try {
            return format(LocalDate.parse(parseableText));
        } catch (Exception ignored) {
            // Try the natural-language formats below.
        }

        for (int i = 0; i < DATE_FORMATS.size(); i++) {
            try {
                LocalDate parsed = LocalDate.parse(parseableText, DATE_FORMATS.get(i));
                // The first four formats include a year; the last four do not.
                if (i >= 4) {
                    parsed = parsed.withYear(referenceDate.getYear());
                    if (parsed.isBefore(referenceDate)) {
                        parsed = parsed.plusYears(1);
                    }
                }
                return format(parsed);
            } catch (Exception ignored) {
                // Continue trying supported formats.
            }
        }

        return null;
    }

    private String format(LocalDate date) {
        return date.format(OUTPUT_FORMAT);
    }
}
