package com.jsp.ems.util;

import com.jsp.ems.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class DBConnection {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new IllegalStateException("db.properties not found on classpath");
            PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() { }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    PROPS.getProperty("db.url"),
                    PROPS.getProperty("db.username"),
                    PROPS.getProperty("db.password"));
        } catch (SQLException e) {
            throw new DataAccessException("Could not connect to database: " + e.getMessage(), e);
        }
    }

    /** Creates tables (if missing) and seeds departments by running schema.sql. */
    public static void initSchema() {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) throw new IllegalStateException("schema.sql not found on classpath");
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            StringBuilder cleaned = new StringBuilder();
            for (String line : script.split("\n")) {
                if (!line.trim().startsWith("--")) cleaned.append(line).append('\n');
            }
            try (Connection con = getConnection(); Statement st = con.createStatement()) {
                for (String sql : cleaned.toString().split(";")) {
                    if (!sql.trim().isEmpty()) st.execute(sql);
                }
            }
        } catch (IOException | SQLException e) {
            throw new DataAccessException("Schema initialisation failed: " + e.getMessage(), e);
        }
    }
}
