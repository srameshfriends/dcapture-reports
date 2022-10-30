package dcapture.reports.application;

import com.arangodb.springframework.annotation.EnableArangoRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableArangoRepositories(basePackages = {"dcapture.reports.arangodb"})
@ComponentScan(basePackages = {"dcapture.reports.*"})
public class ReportsApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ReportsApplication.class);
        springApplication.addListeners(new ApplicationPidFileWriter());
        springApplication.run(args);
    }
}
