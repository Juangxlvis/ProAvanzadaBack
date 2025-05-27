package org.uniquindio.edu.co.gpsanjuan_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;


@CrossOrigin(origins = "[http://localhost:4200]")
@SpringBootApplication
public class GpsanjuanBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GpsanjuanBackendApplication.class, args);
	}

}
