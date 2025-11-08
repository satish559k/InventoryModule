package com.InventoryModule.InventoryModule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class InventoryModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryModuleApplication.class, args);
	}

}
