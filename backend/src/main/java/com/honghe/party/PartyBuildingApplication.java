package com.honghe.party;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.honghe.party.mapper")
public class PartyBuildingApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartyBuildingApplication.class, args);
    }
}
