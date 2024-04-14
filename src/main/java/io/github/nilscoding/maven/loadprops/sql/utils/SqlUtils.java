package io.github.nilscoding.maven.loadprops.sql.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * SQL utility functions.
 * @author NilsCoding
 */
public final class SqlUtils {

    private SqlUtils() {
    }

    /**
     * Closes the connection silently.
     * @param conn connection to close.
     */
    public static void close(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (Exception ex) {
            // ignore exception here
        }
    }

    /**
     * Closes the ResultSet and/or Statement silently.
     * @param rs   ResultSet to close
     * @param stmt Statement to close
     */
    public static void close(ResultSet rs, Statement stmt) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ex) {
                // ignore exception here
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception ex) {
                // ignore exception here
            }
        }
    }

    /**
     * Returns the auto-commit state of the given connection.
     * @param conn connection
     * @return auto-commit state
     */
    public static boolean getAutoCommitState(Connection conn) {
        if (conn == null) {
            return true;
        }
        try {
            return conn.getAutoCommit();
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * Sets the auto-commit state of the given connection to given value.
     * @param conn       connection
     * @param autoCommit auto-commit state
     */
    public static void changeAutoCommit(Connection conn, boolean autoCommit) {
        if (conn == null) {
            return;
        }
        try {
            conn.setAutoCommit(autoCommit);
        } catch (Exception ex) {
            // ignore exception here
        }
    }

}
