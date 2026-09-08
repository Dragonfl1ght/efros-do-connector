package ru.company.connector.efrosdo.mapper;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

record HostAddress(String hostName, String ipv4) {

    private static final Pattern IPV4 = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$");

    static HostAddress of(String host) {
        if (!StringUtils.hasText(host)) {
            return new HostAddress(null, null);
        }
        return IPV4.matcher(host).matches()
                ? new HostAddress(null, host)
                : new HostAddress(host, null);
    }
}
