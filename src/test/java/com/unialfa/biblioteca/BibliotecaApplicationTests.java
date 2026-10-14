package com.unialfa.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "api.security.token.secret=chave-somente-para-testes-0123456789abcdef0123456789")
class BibliotecaApplicationTests {

	@Test
	void contextLoads() {
	}

}
