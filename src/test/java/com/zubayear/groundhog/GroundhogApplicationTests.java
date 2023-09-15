package com.zubayear.groundhog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

//@SpringBootTest
class GroundhogApplicationTests {

	@Test
	void contextLoads() {
		System.out.println(LocalDate.now());
	}

}
