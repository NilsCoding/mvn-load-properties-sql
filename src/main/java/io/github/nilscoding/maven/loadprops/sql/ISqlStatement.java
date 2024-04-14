package io.github.nilscoding.maven.loadprops.sql;

/**
 * SQL statement.
 * @author NilsCoding
 */
public interface ISqlStatement {

    /**
     * Returns the SQL statement.
     * @return SQL statement.
     */
    String getSqlStatement();

    /**
     * Default setter for value.
     * @param valueStr value to set
     */
    void set(String valueStr);

}
