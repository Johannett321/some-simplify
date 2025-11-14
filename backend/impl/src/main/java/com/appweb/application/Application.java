package appweb.groupid.appweb.lowerappname;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
public class appweb.appnameApplication {

	public static void main(String[] args) {
		SpringApplication.run(appweb.appnameApplication.class, args);
	}

}
