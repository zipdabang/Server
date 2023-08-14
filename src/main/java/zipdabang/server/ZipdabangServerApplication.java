package zipdabang.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableRedisRepositories
public class ZipdabangServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZipdabangServerApplication.class, args);
	}

}
