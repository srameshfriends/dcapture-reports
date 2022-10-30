package dcapture.reports.application;

import com.arangodb.ArangoDB;
import com.arangodb.springframework.config.ArangoConfiguration;
import com.arangodb.springframework.core.ArangoOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = {"classpath:application.yml"})
public class ArangoConfig implements ArangoConfiguration {
    @Value("${spring.data.arangodb.host}")
    private String host;
    @Value("${spring.data.arangodb.port}")
    private String port;
    @Value("${spring.data.arangodb.database}")
    private String database;
    @Value("${spring.data.arangodb.username}")
    private String username;
    @Value("${spring.data.arangodb.password}")
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPortNumber() {
        if (port != null) {
            try {
                int prt = Integer.parseInt(port);
                if (prt != 0) {
                    return prt;
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        return 8529;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDatabase() {
        if (database == null) {
            return "jasper_reports";
        }
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        if (username == null) {
            return "";
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        if (password == null) {
            return "";
        }
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public ArangoDB.Builder arango() {
        ArangoOperations arangoOperations;
        return new ArangoDB.Builder().host(getHost(), getPortNumber()).user(getUsername()).password(getPassword());
    }

    @Override
    public String database() {
        return getDatabase();
    }
}
