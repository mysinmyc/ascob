package ascob.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ascob.EnableAscobCore;
import ascob.impl.EnableAscobImpl;

@SpringBootApplication
@EnableAscobCore
@EnableAscobImpl
public class ServerMain {

	public static void main(String[] args) {
		SpringApplication.run(ServerMain.class, args);
	}
}
