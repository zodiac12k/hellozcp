package com.sk.zcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class HelloZcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloZcpApplication.class, args);
	}
	
	@GetMapping("/home") 
	public String home() {
		return "Hello ZCP";
	}
  
  	@GetMapping("/error") 
	public String error() throws Exception{
		throw new Exception("Error service");
	}
  
}
