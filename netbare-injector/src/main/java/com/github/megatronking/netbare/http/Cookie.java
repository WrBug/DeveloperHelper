/*  NetBare - An android network capture and injection library.
 *  Copyright (C) 2018-2019 Megatron King
 *  Copyright (C) 2018-2019 GuoShi
 *
 *  NetBare is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Found-
 *  ation, either version 3 of the License, or (at your option) any later version.
 *
 *  NetBare is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with NetBare.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.megatronking.netbare.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An <a href="http://tools.ietf.org/html/rfc6265">RFC 6265</a> Cookie.
 *
 * @author Megatron King
 * @since 2018/12/25 21:56
 */
public final class Cookie {

    private static final Pattern YEAR_PATTERN
            = Pattern.compile("(\\d{2,4})[^\\d]*");
    private static final Pattern MONTH_PATTERN
            = Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec).*");
    private static final Pattern DAY_OF_MONTH_PATTERN
            = Pattern.compile("(\\d{1,2})[^\\d]*");
    private static final Pattern TIME_PATTERN
            = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})[^\\d]*");

    private static final TimeZone UTC = TimeZone.getTimeZone("GMT");

    private static final long MAX_DATE = 253402300799999L;

    /**
     * Most websites serve cookies in the blessed format. Eagerly create the parser to ensure such
     * cookies are on the fast path.
     */
    private static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT =
            new ThreadLocal<DateFormat>() {
                @Override
                protected DateFormat initialValue() {
                    // Date format specified by RFC 7231 section 7.1.1.1.
                    DateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                    rfc1123.setLenient(false);
                    rfc1123.setTimeZone(UTC);
                    return rfc1123;
                }
            };

    public final String name;
    public final String value;
    public final long expiresAt;
    public final String domain;
    public final String path;
    public final String priority;
    public final boolean secure;
    public final boolean httpOnly;

    /**
     * True if 'expires' or 'max-age' is present.
     */
    public final boolean persistent;

    /**
     * True unless 'domain' is present.
     */
    public final boolean hostOnly;

    private Cookie(String name, String value, long expiresAt, String domain, String path,
                   boolean secure, boolean httpOnly, boolean hostOnly, boolean persistent,
                   String priority) {
        this.name = name;
        this.value = value;
        this.expiresAt = expiresAt;
        this.domain = domain;
        this.path = path;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.persistent = persistent;
        this.hostOnly = hostOnly;
        this.priority = priority;
    }

    private Cookie(Builder builder) {
        this.name = builder.name;
        this.value = builder.value;
        this.expiresAt = builder.expiresAt;
        this.domain = builder.domain;
        this.path = builder.path;
        this.priority = builder.priority;
        this.secure = builder.secure;
        this.httpOnly = builder.httpOnly;
        this.persistent = builder.persistent;
        this.hostOnly = builder.hostOnly;
    }

    public String expiresAt() {
        if (!persistent || expiresAt == Long.MIN_VALUE) {
            // A session date.
            return "1969-12-31T23:59:59.000Z";
        }
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setLenient(false);
        format.setTimeZone(UTC);
        return format.format(new Date(expiresAt));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(name);
        result.append('=');
        result.append(value);
        if (persistent) {
            if (expiresAt == Long.MIN_VALUE) {
                result.append("; max-age=0");
            } else {
                result.append("; expires=").append(STANDARD_DATE_FORMAT.get().format(new Date(expiresAt)));
            }
        }
        if (!hostOnly) {
            result.append("; domain=");
            result.append(domain);
        }
        result.append("; path=").append(path);

        if (secure) {
            result.append("; secure");
        }
        if (httpOnly) {
            result.append("; httponly");
        }
        if (priority != null) {
            result.append("; Priority=").append(priority);
        }
        return result.toString();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (!(other instanceof Cookie)) {
            return false;
        }
        Cookie that = (Cookie) other;
        return that.name.equals(name)
                && that.value.equals(value)
                && that.domain.equals(domain)
                && that.path.equals(path)
                && that.expiresAt == expiresAt
                && that.secure == secure
                && that.httpOnly == httpOnly
                && that.persistent == persistent
                && that.hostOnly == hostOnly
                && TextUtils.equals(that.priority, priority);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + name.hashCode();
        hash = 31 * hash + value.hashCode();
        hash = 31 * hash + domain.hashCode();
        hash = 31 * hash + path.hashCode();
        if (priority != null) {
            hash = 31 * hash + priority.hashCode();
        }
        hash = 31 * hash + (int) (expiresAt ^ (expiresAt >>> 32));
        hash = 31 * hash + (secure ? 0 : 1);
        hash = 31 * hash + (httpOnly ? 0 : 1);
        hash = 31 * hash + (persistent ? 0 : 1);
        hash = 31 * hash + (hostOnly ? 0 : 1);
        return hash;
    }

    /**
     * Builds a cookie. The {@linkplain #name(String)}  }, {@linkplain #value(String)},
     * and {@linkplain #domain(String)} values must all be set before calling {@link #build}.
     */
    public static final class Builder {

        private String name;
        private String value;
        private long expiresAt = MAX_DATE;
        private String domain;
        private String path = "/";
        private boolean secure;
        private boolean httpOnly;
        private boolean persistent;
        private boolean hostOnly;
        private String priority;

        /**
         * Set the cookie name, must not null.
         *
         * @param name Cookie name.
         * @return The same builder.
         */
        public Builder name(@NonNull String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the cookie value, must not null.
         *
         * @param value Cookie value.
         * @return The same builder.
         */
        public Builder value(@NonNull String value) {
            this.value = value;
            return this;
        }

        /**
         * Set the cookie expires time.
         *
         * @param expiresAt When the cookie expired.
         * @return The same builder.
         */
        public Builder expiresAt(long expiresAt) {
            if (expiresAt <= 0) {
                expiresAt = Long.MIN_VALUE;
            }
            if (expiresAt > MAX_DATE) {
                expiresAt = MAX_DATE;
            }
            this.expiresAt = expiresAt;
            this.persistent = true;
            return this;
        }

        /**
         * Set the domain pattern for this cookie. The cookie will match {@code domain} and all of
         * its subdomains.
         *
         * @param domain The domain pattern.
         * @return The same builder.
         */
        public Builder domain(String domain) {
            return domain(domain, false);
        }

        /**
         * Set the host-only domain for this cookie. The cookie will match {@code domain} but none
         * of its subdomains.
         *
         * @param domain The host-only domain pattern.
         * @return The same builder.
         */
        public Builder hostOnlyDomain(String domain) {
            return domain(domain, true);
        }

        private Builder domain(String domain, boolean hostOnly) {
            this.domain = domain;
            this.hostOnly = hostOnly;
            return this;
        }

        /**
         * Set the path this cookie, must start with '/'.
         *
         * @param path The path, must start with '/'.
         * @return The same builder.
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Set the cookie is secure.
         *
         * @return The same builder.
         */
        public Builder secure() {
            this.secure = true;
            return this;
        }

        /**
         * Set the cookie is http only.
         *
         * @return The same builder.
         */
        public Builder httpOnly() {
            this.httpOnly = true;
            return this;
        }

        /**
         * Set the priority for this cookie, it is a chromium extension.
         *
         * @param priority The priority, such as HIGH.
         * @return The same builder.
         */
        public Builder priority(String priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Build a new cookie instance.
         *
         * @return A Cookie.
         */
        public Cookie build() {
            return new Cookie(this);
        }

    }

    @Nullable
    public static Cookie parseSetCookie(String host, String setCookie) {
        int pos = 0;
        int limit = setCookie.length();
        int cookiePairEnd = delimiterOffset(setCookie, pos, limit, ';');

        int pairEqualsSign = delimiterOffset(setCookie, pos, cookiePairEnd, '=');
        if (pairEqualsSign == cookiePairEnd) {
            return null;
        }

        String cookieName = trimSubstring(setCookie, pos, pairEqualsSign);
        if (cookieName.isEmpty() || indexOfControlOrNonAscii(cookieName) != -1) {
            return null;
        }

        String cookieValue = trimSubstring(setCookie, pairEqualsSign + 1, cookiePairEnd);
        if (indexOfControlOrNonAscii(cookieValue) != -1) {
            return null;
        }

        long expiresAt = MAX_DATE;
        String domain = null;
        String path = null;
        long deltaSeconds = -1L;
        boolean secureOnly = false;
        boolean httpOnly = false;
        boolean hostOnly = true;
        boolean persistent = false;
        String priority = null;

        pos = cookiePairEnd + 1;
        while (pos < limit) {
            int attributePairEnd = delimiterOffset(setCookie, pos, limit, ';');

            int attributeEqualsSign = delimiterOffset(setCookie, pos, attributePairEnd, '=');
            String attributeName = trimSubstring(setCookie, pos, attributeEqualsSign);
            String attributeValue = attributeEqualsSign < attributePairEnd
                    ? trimSubstring(setCookie, attributeEqualsSign + 1, attributePairEnd)
                    : "";

            if (attributeName.equalsIgnoreCase("expires")) {
                try {
                    expiresAt = parseExpires(attributeValue, attributeValue.length());
                    persistent = true;
                } catch (IllegalArgumentException e) {
                    // Ignore this attribute, it isn't recognizable as a date.
                }
            } else if (attributeName.equalsIgnoreCase("max-age")) {
                try {
                    deltaSeconds = parseMaxAge(attributeValue);
                    persistent = true;
                } catch (NumberFormatException e) {
                    // Ignore this attribute, it isn't recognizable as a max age.
                }
            } else if (attributeName.equalsIgnoreCase("domain")) {
                domain = attributeValue;
            } else if (attributeName.equalsIgnoreCase("path")) {
                path = attributeValue;
            } else if (attributeName.equalsIgnoreCase("secure")) {
                secureOnly = true;
            } else if (attributeName.equalsIgnoreCase("httponly")) {
                httpOnly = true;
            } else if (attributeName.equalsIgnoreCase("priority")) {
                priority = attributeValue;
            }

            pos = attributePairEnd + 1;
        }

        if (TextUtils.isEmpty(domain)) {
            domain = host;
        }

        // If 'Max-Age' is present, it takes precedence over 'Expires', regardless of the order the two
        // attributes are declared in the cookie string.
        if (deltaSeconds == Long.MIN_VALUE) {
            expiresAt = Long.MIN_VALUE;
        } else if (deltaSeconds != -1L) {
            long deltaMilliseconds = deltaSeconds <= (Long.MAX_VALUE / 1000) ? deltaSeconds * 1000
                    : Long.MAX_VALUE;
            long currentTimeMillis = System.currentTimeMillis();
            expiresAt = currentTimeMillis + deltaMilliseconds;
            if (expiresAt > MAX_DATE) {
                // Handle overflow.
                expiresAt = MAX_DATE;
            }
        }
        return new Cookie(cookieName, cookieValue, expiresAt, domain, path, secureOnly, httpOnly,
                hostOnly, persistent, priority);
    }

    private static int delimiterOffset(String input, int pos, int limit, char delimiter) {
        for (int i = pos; i < limit; i++) {
            if (input.charAt(i) == delimiter) {
                return i;
            }
        }
        return limit;
    }

    private static String trimSubstring(String string, int pos, int limit) {
        int start = skipLeadingAsciiWhitespace(string, pos, limit);
        int end = skipTrailingAsciiWhitespace(string, start, limit);
        return string.substring(start, end);
    }

    private static int skipLeadingAsciiWhitespace(String input, int pos, int limit) {
        for (int i = pos; i < limit; i++) {
            switch (input.charAt(i)) {
                case '\t':
                case '\n':
                case '\f':
                case '\r':
                case ' ':
                    continue;
                default:
                    return i;
            }
        }
        return limit;
    }

    private static int skipTrailingAsciiWhitespace(String input, int pos, int limit) {
        for (int i = limit - 1; i >= pos; i--) {
            switch (input.charAt(i)) {
                case '\t':
                case '\n':
                case '\f':
                case '\r':
                case ' ':
                    continue;
                default:
                    return i + 1;
            }
        }
        return pos;
    }

    private static int indexOfControlOrNonAscii(String input) {
        for (int i = 0, length = input.length(); i < length; i++) {
            char c = input.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                return i;
            }
        }
        return -1;
    }

    private static long parseExpires(String s, int limit) {
        int pos = dateCharacterOffset(s, 0, limit, false);

        int hour = -1;
        int minute = -1;
        int second = -1;
        int dayOfMonth = -1;
        int month = -1;
        int year = -1;
        Matcher matcher = TIME_PATTERN.matcher(s);

        while (pos < limit) {
            int end = dateCharacterOffset(s, pos + 1, limit, true);
            matcher.region(pos, end);

            if (hour == -1 && matcher.usePattern(TIME_PATTERN).matches()) {
                hour = Integer.parseInt(matcher.group(1));
                minute = Integer.parseInt(matcher.group(2));
                second = Integer.parseInt(matcher.group(3));
            } else if (dayOfMonth == -1 && matcher.usePattern(DAY_OF_MONTH_PATTERN).matches()) {
                dayOfMonth = Integer.parseInt(matcher.group(1));
            } else if (month == -1 && matcher.usePattern(MONTH_PATTERN).matches()) {
                String monthString = matcher.group(1).toLowerCase(Locale.US);
                month = MONTH_PATTERN.pattern().indexOf(monthString) / 4; // Sneaky! jan=1, dec=12.
            } else if (year == -1 && matcher.usePattern(YEAR_PATTERN).matches()) {
                year = Integer.parseInt(matcher.group(1));
            }

            pos = dateCharacterOffset(s, end + 1, limit, false);
        }

        // Convert two-digit years into four-digit years. 99 becomes 1999, 15 becomes 2015.
        if (year >= 70 && year <= 99) {
            year += 1900;
        }
        if (year >= 0 && year <= 69) {
            year += 2000;
        }

        // If any partial is omitted or out of range, return -1. The date is impossible. Note that leap
        // seconds are not supported by this syntax.
        if (year < 1601) {
            throw new IllegalArgumentException();
        }
        if (month == -1) {
            throw new IllegalArgumentException();
        }
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException();
        }
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException();
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException();
        }
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException();
        }

        Calendar calendar = new GregorianCalendar(UTC);
        calendar.setLenient(false);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static int dateCharacterOffset(String input, int pos, int limit, boolean invert) {
        for (int i = pos; i < limit; i++) {
            int c = input.charAt(i);
            boolean dateCharacter = (c < ' ' && c != '\t') || (c >= '\u007f')
                    || (c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c == ':');
            if (dateCharacter == !invert) {
                return i;
            }
        }
        return limit;
    }

    private static long parseMaxAge(String s) {
        try {
            long parsed = Long.parseLong(s);
            return parsed <= 0L ? Long.MIN_VALUE : parsed;
        } catch (NumberFormatException e) {
            // Check if the value is an integer (positive or negative) that's too big for a long.
            if (s.matches("-?\\d+")) {
                return s.startsWith("-") ? Long.MIN_VALUE : Long.MAX_VALUE;
            }
            throw e;
        }
    }

}
