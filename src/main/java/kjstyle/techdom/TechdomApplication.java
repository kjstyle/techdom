package kjstyle.techdom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // event pub/sub을 위해 붙임
public class TechdomApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechdomApplication.class, args);
    }

}
