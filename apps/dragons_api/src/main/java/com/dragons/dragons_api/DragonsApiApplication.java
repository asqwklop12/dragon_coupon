package com.dragons.dragons_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.dragons")
public class DragonsApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(DragonsApiApplication.class, args);
  }

}
