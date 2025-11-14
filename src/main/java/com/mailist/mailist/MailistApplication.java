package com.mailist.mailist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MailistApplication {

	public static void main(String[] args) {
		SpringApplication.run(MailistApplication.class, args);
	}

}
