package com.dragons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DragonsApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(DragonsApiApplication.class, args);
  }

}
