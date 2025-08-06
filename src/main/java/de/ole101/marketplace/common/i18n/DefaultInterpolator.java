package de.ole101.marketplace.common.i18n;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultInterpolator implements Interpolator {

    private static final Pattern PLURAL_PATTERN = Pattern.compile(
            "\\{\\{\\s*plural:(\\w+),\\s*one\\s*\\{([^}]+)}\\s*other\\s*\\{([^}]+)}(?:\\s*zero\\s*\\{([^}]+)})?\\s*}}");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*(.+?)\\s*}}");
    private final Map<Locale, NumberFormat> numberFormatCache = new ConcurrentHashMap<>();
    private final Map<Locale, DateTimeFormatter> dateTimeFormatCache = new ConcurrentHashMap<>();
    private final Map<Locale, DateTimeFormatter> dateFormatCache = new ConcurrentHashMap<>();
    private final MiniMessage miniMessage;

    @Override
    public String interpolate(String message, TranslationContext context, Locale locale) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        // Process plural tokens first
        message = processPluralTokens(message, context, locale);

        // Then process standard variable tokens
        return processVariableTokens(message, context, locale);
    }

    private String processPluralTokens(String message, TranslationContext context, Locale locale) {
        Matcher pluralMatcher = PLURAL_PATTERN.matcher(message);
        if (!pluralMatcher.find()) {
            return message; // No plural tokens, return original
        }

        StringBuilder pluralBuilder = new StringBuilder();
        do {
            String countKey = pluralMatcher.group(1);
            String singularForm = pluralMatcher.group(2);
            String pluralForm = pluralMatcher.group(3);
            String zeroForm = pluralMatcher.groupCount() > 3 ? pluralMatcher.group(4) : null;

            int count = extractCount(context.getArgument(countKey));

            String chosen;
            if (count == 0 && zeroForm != null) {
                chosen = zeroForm;
            } else if (count == 1) {
                chosen = singularForm;
            } else {
                chosen = pluralForm;
            }

            chosen = chosen.replace("#", String.valueOf(count));
            // Recursively interpolate nested tokens
            String replacement = interpolate(chosen, context, locale);
            pluralMatcher.appendReplacement(pluralBuilder, Matcher.quoteReplacement(replacement));
        } while (pluralMatcher.find());

        pluralMatcher.appendTail(pluralBuilder);
        return pluralBuilder.toString();
    }

    private String processVariableTokens(String message, TranslationContext context, Locale locale) {
        Matcher matcher = VARIABLE_PATTERN.matcher(message);
        if (!matcher.find()) {
            return message; // No variable tokens, return original
        }

        StringBuilder builder = new StringBuilder();
        do {
            String token = matcher.group(1).trim();
            String key = token;
            String defaultValue = "";

            if (token.contains(":")) {
                String[] parts = token.split(":", 2);
                key = parts[0].trim();
                defaultValue = parts[1].trim();
            }

            Object value = context.getArgument(key);
            String replacement = (value == null) ? defaultValue : formatValue(value, locale);
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        } while (matcher.find());

        matcher.appendTail(builder);
        return builder.toString();
    }

    private int extractCount(Object countObj) {
        if (countObj == null) {
            return 0;
        }

        if (countObj instanceof Number) {
            return ((Number) countObj).intValue();
        }

        try {
            return Integer.parseInt(countObj.toString());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String formatValue(Object value, Locale locale) {
        return switch (value) {
            case null -> "";
            case Component component -> this.miniMessage.serialize(component);
            case Number number -> getNumberFormat(locale).format(value);
            case LocalDateTime localDateTime -> getDateTimeFormatter(locale).format(localDateTime);
            case LocalDate localDate -> getDateFormatter(locale).format(localDate);
            case Duration duration -> formatDuration(duration, locale);
            case List<?> list -> formatList(list, locale);
            default -> value.toString();
        };
    }

    private NumberFormat getNumberFormat(Locale locale) {
        return this.numberFormatCache.computeIfAbsent(locale, NumberFormat::getNumberInstance);
    }

    private DateTimeFormatter getDateTimeFormatter(Locale locale) {
        return this.dateTimeFormatCache.computeIfAbsent(
                locale, l -> DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(l));
    }

    private DateTimeFormatter getDateFormatter(Locale locale) {
        return this.dateFormatCache.computeIfAbsent(
                locale, l -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(l));
    }

    private String formatDuration(Duration duration, Locale locale) {
        long seconds = Math.abs(duration.getSeconds());
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        // Simple formatting - could be enhanced with translations for time units
        if (days > 0) {
            return formatTimeValue(days, "day", "days", duration.isNegative(), locale);
        } else if (hours > 0) {
            return formatTimeValue(hours, "hour", "hours", duration.isNegative(), locale);
        } else if (minutes > 0) {
            return formatTimeValue(minutes, "minute", "minutes", duration.isNegative(), locale);
        } else {
            return formatTimeValue(seconds, "second", "seconds", duration.isNegative(), locale);
        }
    }

    private String formatTimeValue(long value, String singular, String plural, boolean isPast, Locale locale) {
        String unit = value == 1 ? singular : plural;
        String format = getNumberFormat(locale).format(value);
        return isPast
                ? format + " " + unit + " ago"
                : "in " + format + " " + unit;
    }

    private String formatList(List<?> list, Locale locale) {
        return list.stream()
                .map(item -> item == null ? "" : formatValue(item, locale))
                .collect(Collectors.joining(", "));
    }
}
