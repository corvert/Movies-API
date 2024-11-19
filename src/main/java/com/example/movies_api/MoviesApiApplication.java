package com.example.movies_api;

import com.example.movies_api.entities.ConnectDB;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;

@SpringBootApplication
public class MoviesApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviesApiApplication.class, args);
		ConnectDB db = new ConnectDB();

		Connection connection = db.getConnection();


	}


}
