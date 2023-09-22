package cat.politecnicllevant.gestsuitereserves;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class GestsuiteReservesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestsuiteReservesApplication.class, args);
	}

}
