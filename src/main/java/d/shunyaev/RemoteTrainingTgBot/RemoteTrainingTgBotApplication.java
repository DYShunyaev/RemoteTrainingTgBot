package d.shunyaev.RemoteTrainingTgBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RemoteTrainingTgBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(RemoteTrainingTgBotApplication.class, args);
	}

}
