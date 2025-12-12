package com.example.cafeProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.converter.json.GsonBuilderUtils;

@SpringBootApplication
public class CafeProjectApplication {

	public static void main(String[] args) {
        SpringApplication.run(CafeProjectApplication.class, args);
        System.out.println("-- cafeProject --");
	}


}
