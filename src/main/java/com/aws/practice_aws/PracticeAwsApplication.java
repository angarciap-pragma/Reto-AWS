package com.aws.practice_aws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PracticeAwsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PracticeAwsApplication.class, args);
	}

}
