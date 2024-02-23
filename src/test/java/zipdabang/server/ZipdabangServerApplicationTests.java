package zipdabang.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ZipdabangServerApplicationTests {

	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	public void testControllerApi() throws Exception {
		//mockMvc.perform();
	}

}
