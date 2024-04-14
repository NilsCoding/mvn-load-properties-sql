package io.github.nilscoding.maven.loadprops.sql;

import io.github.nilscoding.maven.loadprops.sql.utils.MavenUtils;
import io.github.nilscoding.maven.loadprops.sql.utils.SqlUtils;
import io.github.nilscoding.maven.loadprops.sql.utils.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Maven Mojo to load Maven properties via SQL.
 * @author NilsCoding
 */
@Mojo(name = "load-properties-sql", defaultPhase = LifecyclePhase.INITIALIZE)
public class LoadPropertiesSqlMojo extends AbstractMojo {

    /**
     * Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Settings.
     */
    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    private Settings settings;

    /**
     * Settings decrypter.
     */
    @Component
    private SettingsDecrypter settingsDecrypter;

    /**
     * JDBC Driver class name.
     */
    @Parameter()
    private String driverClassname;

    /**
     * JDBC connection string.
     */
    @Parameter()
    private String connectionString;

    /**
     * JDBC username.
     */
    @Parameter()
    private String sqlUsername;

    /**
     * JDBC Password.
     */
    @Parameter()
    private String sqlPassword;

    /**
     * ID of encrypted settings key to be used for retrieving username and password for JDBC connection.
     */
    @Parameter()
    private String sqlKey;

    /**
     * SQL statement to select properties with name and value.
     */
    @Parameter()
    private String selectStatement;

    /**
     * SQL pre-statements to be executed before selection.
     */
    @Parameter()
    private List<ISqlStatement> preSqlStatements;

    /**
     * Executes the Maven Mojo.
     * @throws MojoExecutionException Mojo execution exception
     * @throws MojoFailureException   Mojo failure exception
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        log.info("LoadPropertiesSql Mojo at work...");

        if (StringUtils.allNotEmpty(this.driverClassname,
                this.connectionString,
                this.selectStatement) == false) {
            log.warn("Cannot load properties: all sql-related parameters are required");
            throw new MojoExecutionException("All SQL-related parameters are required.");
        }

        if (StringUtils.isEmpty(this.sqlKey)) {
            if (StringUtils.allNotEmpty(this.sqlUsername, this.sqlPassword) == false) {
                log.warn("Either sqlKey or sqlUsername and sqlPassword must be set");
                throw new MojoExecutionException("Either sqlKey or sqlUsername and sqlPassword must be set.");
            }
        }

        try {
            Class.forName(this.driverClassname);
        } catch (Throwable th) {
            log.error("Could not load database driver: " + th);
            throw new MojoExecutionException("JDBC database driver '" + this.driverClassname
                    + "' could not be initialized.");
        }

        String username = null;
        String password = null;

        // prefer sqlKey
        if (StringUtils.isEmpty(this.sqlKey) == false) {
            Server decryptedServer = MavenUtils.decryptServer(this.sqlKey, this.settings, this.settingsDecrypter);
            if (decryptedServer != null) {
                username = decryptedServer.getUsername();
                password = decryptedServer.getPassword();
            }
            if (StringUtils.allNotEmpty(username, password) == false) {
                log.error("Could not decode username / password from sqlKey");
                throw new MojoExecutionException("sqlKey is invalid.");
            }
        }
        if ((username == null) && (password == null)) {
            username = this.sqlUsername;
            password = this.sqlPassword;
        }

        if (StringUtils.allNotEmpty(username, password) == false) {
            log.error("Username and password must not be empty");
            throw new MojoExecutionException("SQL username and password must be set.");
        }

        Connection connection;
        try {
            connection = DriverManager.getConnection(this.connectionString, username, password);
        } catch (Exception ex) {
            log.error("Cannot load properties: no database connection - " + ex);
            throw new MojoExecutionException("Could not create JDBC connection. (" + ex + ")");
        }

        // remember previous auto-commit state
        boolean prevAutoCommit = SqlUtils.getAutoCommitState(connection);
        if (prevAutoCommit == true) {
            SqlUtils.changeAutoCommit(connection, false);
        }
        // execute pre-SQL statements if needed
        boolean preSqlSuccess = true;
        if ((this.preSqlStatements != null) && (this.preSqlStatements.isEmpty() == false)) {
            for (ISqlStatement onePreSqlStatement : this.preSqlStatements) {
                if (onePreSqlStatement == null) {
                    continue;
                }
                if (onePreSqlStatement instanceof DmlSqlStatement) {
                    String sqlStr = onePreSqlStatement.getSqlStatement();
                    if (StringUtils.isEmpty(sqlStr) == false) {
                        sqlStr = sqlStr.trim();
                        PreparedStatement preStmt = null;
                        try {
                            preStmt = connection.prepareStatement(sqlStr);
                            int affectedRows = preStmt.executeUpdate();
                        } catch (Exception ex) {
                            log.error("Failed to execute PRE DML statement '" + sqlStr + "': " + ex);
                            preSqlSuccess = false;
                        }
                        SqlUtils.close(null, preStmt);
                    }
                } else if (onePreSqlStatement instanceof ProcedureSqlStatement) {
                    String sqlStr = onePreSqlStatement.getSqlStatement();
                    if (StringUtils.isEmpty(sqlStr) == false) {
                        sqlStr = sqlStr.trim();
                        CallableStatement preStmt = null;
                        try {
                            preStmt = connection.prepareCall(sqlStr);
                            preStmt.execute();
                        } catch (Exception ex) {
                            log.error("Failed to execute PRE CALL statement '" + sqlStr + "': " + ex);
                            preSqlSuccess = false;
                        }
                        SqlUtils.close(null, preStmt);
                    }
                }
            }
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, String> foundProperties = new LinkedHashMap<>();
        if (preSqlSuccess == true) {
            try {
                stmt = connection.prepareStatement(this.selectStatement);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    if (rsmd.getColumnCount() >= 2) {
                        do {
                            Object propNameObj = rs.getObject(1);
                            if (propNameObj == null) {
                                continue;
                            }
                            Object propValueObj = rs.getObject(2);
                            String propName = String.format("%s", propNameObj);
                            String propValue = (propValueObj == null) ? null : String.format("%s", propValueObj);
                            if (StringUtils.isEmpty(propName) == false) {
                                foundProperties.put(propName, propValue);
                            }
                        } while (rs.next());
                    } else {
                        log.error("Cannot load properties: not enough columns selected (need at least two)");
                    }
                } else {
                    log.info("No properties found");
                }
            } catch (Exception ex) {
                log.error("Cannot load properties: loading failed - " + ex);
            }
        } else {
            log.info("PRE statements failed, so no properties will be selected");
        }

        if (preSqlSuccess == true) {
            boolean success = true;
            try {
                connection.commit();
            } catch (Exception ex) {
                log.error("Failed to commit transaction: " + ex);
                success = false;
            } finally {
                SqlUtils.changeAutoCommit(connection, true);
            }
            if (success == false) {
                throw new MojoExecutionException("Could not commit transaction.");
            }
        } else {
            try {
                connection.rollback();
            } catch (Exception ex) {
                log.error("Failed to rollback transaction: " + ex);
            }
            throw new MojoExecutionException("PRE statements failed.");
        }

        // change back to auto-commit mode if needed
        if (prevAutoCommit == false) {
            SqlUtils.changeAutoCommit(connection, false);
        }

        SqlUtils.close(rs, stmt);
        SqlUtils.close(connection);

        if (foundProperties.isEmpty() == false) {
            Properties projectProperties = this.project.getProperties();
            foundProperties.forEach(projectProperties::setProperty);
            log.info("Loaded " + foundProperties.size() + " "
                    + StringUtils.singularPlural(foundProperties, "property", "properties"));
        }

    }

}
