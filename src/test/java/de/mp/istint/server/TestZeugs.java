package de.mp.istint.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.mp.istint.server.config.Redirects;

@SpringBootTest
public class TestZeugs {
    @Autowired
    Redirects redirects;

    @Test
    void testName() {
        System.out.println("TestZeugs.testName()" + redirects);
    }
}
