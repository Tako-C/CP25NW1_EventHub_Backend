package com.int371.eventhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
public class EventhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventhubApplication.class, args);
	}

	// BrcyptPassword delete before production
    // @Bean
    // public CommandLineRunner commandLineRunner(PasswordEncoder passwordEncoder) {
    //     return args -> {
    //         String rawPassword = "pass1234";
    //         String encodedPassword = passwordEncoder.encode(rawPassword);

    //         System.out.println("--- GENERATED HASHED PASSWORD ---");
    //         System.out.println(encodedPassword);
    //         System.out.println("---------------------------------");
    //     };
    // }

}
