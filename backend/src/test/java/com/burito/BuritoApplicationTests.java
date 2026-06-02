package com.burito;

import com.burito.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "spring.flyway.enabled=true")
@Import(TestcontainersConfig.class)
class BuritoApplicationTests {

	@Test
	void contextLoads() {
	}

}
