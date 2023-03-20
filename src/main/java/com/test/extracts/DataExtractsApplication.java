package com.test.extracts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;

@SpringBootApplication
public class DataExtractsApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataExtractsApplication.class, args);
	}

}
