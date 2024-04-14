package io.github.nilscoding.maven.loadprops.sql;

/**
 * Abstract base class for SQL statement implementations.
 * @author NilsCoding
 */
public abstract class AbstractSqlStatement implements ISqlStatement {

    /**
     * SQL statement.
     */
    protected String sqlStatement;

    /**
     * Default set method.
     * @param valueStr value to set
     */
    public void set(String valueStr) {
        this.setSqlStatement(valueStr);
    }

    /**
     * Returns the SQL statement.
     * @return SQL statement
     */
    @Override
    public String getSqlStatement() {
        return sqlStatement;
    }

    /**
     * Sets the SQL statement.
     * @param sqlStatement SQL statement to set
     */
    public void setSqlStatement(String sqlStatement) {
        this.sqlStatement = sqlStatement;
    }
}
