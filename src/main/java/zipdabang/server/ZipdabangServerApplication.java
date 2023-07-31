package zipdabang.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ZipdabangServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZipdabangServerApplication.class, args);
	}

}
