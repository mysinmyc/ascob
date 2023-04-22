package ascob.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import ascob.EnableAscobCore;
import ascob.impl.EnableAscobImpl;

@SpringBootApplication
@EnableAscobCore
@EnableAscobImpl
@EnableScheduling
@EnableAsync
public class ServerMain {

	public static void main(String[] args) {
		SpringApplication.run(ServerMain.class, args);
	}
}
