package com.familytree.usermgmt.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Converts Railway/Heroku-style {@code DATABASE_URL} values
 * ({@code postgresql://user:pass@host:port/db}) into JDBC datasource properties.
 */
final class DatabaseUrlParser {

    record ParsedDatasource(String jdbcUrl, String username, String password) {}

    private DatabaseUrlParser() {}

    static Optional<ParsedDatasource> parse(String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return Optional.empty();
        }

        String normalized = databaseUrl.trim();
        if (normalized.startsWith("postgres://")) {
            normalized = "postgresql://" + normalized.substring("postgres://".length());
        }
        if (!normalized.startsWith("postgresql://")) {
            return Optional.empty();
        }

        String withoutScheme = normalized.substring("postgresql://".length());
        int at = withoutScheme.indexOf('@');
        if (at < 0) {
            return Optional.empty();
        }

        String userInfo = withoutScheme.substring(0, at);
        String hostAndDatabase = withoutScheme.substring(at + 1);

        int colon = userInfo.indexOf(':');
        if (colon < 0) {
            return Optional.empty();
        }

        String username = decode(userInfo.substring(0, colon));
        String password = decode(userInfo.substring(colon + 1));

        int slash = hostAndDatabase.indexOf('/');
        if (slash < 0) {
            return Optional.empty();
        }

        String hostPort = hostAndDatabase.substring(0, slash);
        String databaseAndQuery = hostAndDatabase.substring(slash + 1);

        String[] hostPortParts = hostPort.split(":", 2);
        String host = hostPortParts[0];
        String port = hostPortParts.length > 1 ? hostPortParts[1] : "5432";

        String database;
        String query = "";
        int queryStart = databaseAndQuery.indexOf('?');
        if (queryStart >= 0) {
            database = databaseAndQuery.substring(0, queryStart);
            query = databaseAndQuery.substring(queryStart);
        } else {
            database = databaseAndQuery;
        }

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        if (query.isEmpty() && !isLocalHost(host)) {
            jdbcUrl += "?sslmode=require";
        } else if (!query.isEmpty()) {
            jdbcUrl += query;
        }

        return Optional.of(new ParsedDatasource(jdbcUrl, username, password));
    }

    private static boolean isLocalHost(String host) {
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
