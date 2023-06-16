package pnu.cse.studyhub.signaling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SignalingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SignalingApplication.class, args);
	}

}
