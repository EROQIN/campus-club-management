package com.erokin.campusclubmanagement;

import com.erokin.campusclubmanagement.config.properties.AppSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppSecurityProperties.class)
public class CampusClubManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusClubManagementApplication.class, args);
    }
}
