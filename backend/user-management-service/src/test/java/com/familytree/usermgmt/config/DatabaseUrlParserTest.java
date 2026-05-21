package com.familytree.usermgmt.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUrlParserTest {

    @Test
    void parsesPostgresqlUrl() {
        var parsed = DatabaseUrlParser.parse(
                "postgresql://familytree:secret@containers-us-west-123.railway.app:5432/railway");

        assertThat(parsed).isPresent();
        assertThat(parsed.get().username()).isEqualTo("familytree");
        assertThat(parsed.get().password()).isEqualTo("secret");
        assertThat(parsed.get().jdbcUrl())
                .isEqualTo("jdbc:postgresql://containers-us-west-123.railway.app:5432/railway?sslmode=require");
    }

    @Test
    void parsesPostgresSchemeAlias() {
        var parsed = DatabaseUrlParser.parse("postgres://user:pass@db.example.com/mydb?sslmode=require");

        assertThat(parsed).isPresent();
        assertThat(parsed.get().jdbcUrl())
                .isEqualTo("jdbc:postgresql://db.example.com:5432/mydb?sslmode=require");
    }

    @Test
    void skipsLocalhostWithoutSsl() {
        var parsed = DatabaseUrlParser.parse("postgresql://familytree:familytree@localhost:5433/familytree");

        assertThat(parsed).isPresent();
        assertThat(parsed.get().jdbcUrl())
                .isEqualTo("jdbc:postgresql://localhost:5433/familytree");
    }

    @Test
    void returnsEmptyForInvalidUrl() {
        assertThat(DatabaseUrlParser.parse(null)).isEmpty();
        assertThat(DatabaseUrlParser.parse("jdbc:postgresql://localhost/db")).isEmpty();
    }
}
