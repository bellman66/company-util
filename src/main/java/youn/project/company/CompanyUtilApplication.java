package youn.project.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableAsync
@EnableWebMvc
@SpringBootApplication
public class CompanyUtilApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyUtilApplication.class, args);
    }

}
