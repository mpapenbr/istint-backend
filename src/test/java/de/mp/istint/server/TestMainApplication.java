package de.mp.istint.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TestMainApplication {

	@LocalServerPort
	private int port;
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void rootResponse() {
		String ret = this.restTemplate.getForObject("http://localhost:" + port + "/", String.class);
		System.out.println("TestMainApplication.rootResponse() " + ret);
	}
}
