package com.example.istint.isintserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import lombok.AllArgsConstructor;
import lombok.Data; 
import lombok.NoArgsConstructor;


@SpringBootApplication

public class DemoApplication {

	public static void main(String[] args) {		
		SpringApplication.run(DemoApplication.class, args);
	}
	
	
}

@RestController
class DemoController {
	@GetMapping(path="/hello")
	public String getMethodName() {
		return "HAHA";
	}
	
	@GetMapping(path="/")
	public String root() {
		return "Ich bin Root!";
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class SomeData {
	@Id
	String id;
}
