package ch.so.agi.sodata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SodataApplication {

    public static void main(String[] args) {
        SpringApplication.run(SodataApplication.class, args);
    }

}
