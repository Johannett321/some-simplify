package com.appweb.application.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormatUtils {

    public static String convertToCamelCase(String str) {
        String[] parts = str.split(" ");
        StringBuilder camelCaseString = new StringBuilder();
        for (String part : parts) {
            camelCaseString.append(toProperCase(part));
        }
        return camelCaseString.toString();
    }

    private static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }

    public static String removeHTTPProtocolAndTrailingSlash(String url) {
        // Remove the "http://" or "https://" prefix if present
        if (url.startsWith("http://")) {
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
        }

        // Remove any trailing slashes
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    public static String formatDate(LocalDateTime dateTime) {
        // Define the Norwegian date-time format
        DateTimeFormatter norwegianFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'kl.' HH:mm");

        // Format the provided LocalDateTime object
        return dateTime.format(norwegianFormatter);
    }

    public static String maskString(String input) {
        if (input == null) {
            return null;
        }
        return maskString(input, input.length() > 8 ? 4 : 0, input.length() > 12 ? 4 : 0);
    }

    public static String maskString(String input, int visiblePrefix, int visibleSuffix) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (visiblePrefix < 0) visiblePrefix = 0;
        if (visibleSuffix < 0) visibleSuffix = 0;

        int length = input.length();
        if (length <= visiblePrefix + visibleSuffix) {
            return "*".repeat(length);
        }

        String prefix = input.substring(0, visiblePrefix);
        String suffix = input.substring(length - visibleSuffix);
        String masked = "*".repeat(length - (visiblePrefix + visibleSuffix));

        return prefix + masked + suffix;
    }
}
