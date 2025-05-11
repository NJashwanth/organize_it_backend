package com.jashwanth_projects.organize_it_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.jashwanth_projects.organize_it_backend.repository")
public class OrganizeItBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrganizeItBackendApplication.class, args);
	}

}
