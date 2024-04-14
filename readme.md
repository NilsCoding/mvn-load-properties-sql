# mvn-load-properties-sql

**mvn-load-properties-sql** is a custom Maven Mojo to load Maven build properties via SQL.

Optionally, SQL update statements and/or procedure calls can be done to change values before selecting them, e.g. to increase values.

The Mojo will be executed in the `initialize` phase of the Maven build.

## pre-requisits
As **mvn-load-properties-sql** is currently not available on Maven Central, you need to make sure that it is available to whatever system you're going to use it in your build.

### local builds
You need to clone the repository of **mvn-load-properties-sql** and then install it locally by `mvn clean install`.

### server-side builds (with custom Maven hosting)
First, you need to clone and locally build **mvn-load-properties-sql**, then you need to deploy it to your custom Maven hosting (e.g. Nexus).

While you might have configured your custom Maven hosting to be accessible via your Maven configuration, this might not include a custom Maven hosting location for plugins. So, make sure that `pluginRepositories` in your Maven configuration file includes your custom Maven hosting.

## usage

To execute the Mojo in your build process, it needs to be configured in the `build`/`plugins` section, like this:
```xml
<plugin>
    <groupId>io.github.nilscoding.maven</groupId>
    <artifactId>mvn-load-properties-sql</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>load-properties-sql</goal>
            </goals>
            <configuration>
                <driverClassname>driver.class.name</driverClassname>
                <connectionString>jdbc-connection-string</connectionString>
                <sqlUsername>username</sqlUsername>
                <sqlPassword>password</sqlPassword>
                <selectStatement>SELECT PROP_NAME,PROP_VALUE FROM SOME_TABLE WHERE PROP_GROUPID='${project.groupId}' AND PROP_ARTIFACTID='${project.artifactId}'</selectStatement>
                <preSqlStatements>
                    <procedureSqlStatement>{ call incSomeValue }</procedureSqlStatement>
                    <dmlSqlStatement>UPDATE A_TABLE SET VAL = 'Y' WHERE NAME = '${project.artifactId}'</dmlSqlStatement>
                </preSqlStatements>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>database.driver.group-id</groupId>
            <artifactId>driver-artifact</artifactId>
            <version>driver-version</version>
        </dependency>
    </dependencies>    
</plugin>
```

The following configuration properties are required:
* driverClassname
* connectionString
* sqlUsername
* sqlPassword
* selectStatement
* section with JDBC driver dependency

## configuration option
All configuration options can contain placeholders for project properties like `${project.groupId}` and `${project.artifactId}`

**Important note on SQL execution**:
Both pre-SQL statements and main selection will be executed in same SQL transaction (with manual commit-mode enabled during execution), so concurrent executions on different machines might block due to database locks.

### JDBC driver dependency
You need to specify the JDBC driver as a dependency because **mvn-load-properties-sql** does not reference any JDBC driver itself.

### driverClassname
Classname of the JDBC driver. Will be initialized by Mojo via `Class.forName(...)`

### connectionString
Full JDBC connection string. Syntax and parameters depend on JDBC driver.

### sqlUsername
Username for connecting to the database.

### sqlPassword
Password for connecting to the database. Must be provided in clear-text.

### sqlKey
Instead of providing `sqlUsername` and `sqlPassword` you can specify `sqlKey` which references a server entry in `settings.xml`.
Username and password are then taken from that server entry and if the password was encoded using Maven password encryption, it will be decoded.

**Important note:** When using `sqlKey` then the server entry in `settings.xml` _must_ exist on every machine where the build is executed.

The `sqlKey` entry will be preferred over `sqlUsername` and `sqlPassword`. However, if the username / password could not be decrypted from server entry (either because the entry does not exist or due to other problems) then `sqlUsername` and `sqlPassword` will be used.

### selectStatement
SQL statement to select both property name and value.

First column of selection is expected to contain the property name, second column must contain the property value.
An empty property name or `null` will be ignored, but `null` / empty string as a value is allowed.

SQL statement can contain placeholders, e.g. for `${project.groupId}` or `${project.artifactId}`

### preSqlStatements (optional)
The `preSqlStatements` block is optional and can contain both `dmlSqlStatement` and `procedureSqlStatement` entries (each zero, one or more).

Statements can contain placeholders, e.g. for `${project.groupId}` or `${project.artifactId}`

#### dmlSqlStatement (optional)
`UPDATE`, `INSERT` or `DELETE` SQL statement to be executed.

#### procedureSqlStatement (optional)
SQL statement for procedure call. Syntax depends on SQL dialect.

## copyright / license

**mvn-load-properties-sql** is licensed under the MIT License, for more details see license.md
