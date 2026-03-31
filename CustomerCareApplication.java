package lk.sliit.customer_care;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class CustomerCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerCareApplication.class, args);
    }

}
