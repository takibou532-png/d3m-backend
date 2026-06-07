package com.menu.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.menu.demo.Enums.Role;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.UserRepository;



@SpringBootApplication
@EnableScheduling
public class ScPlatformBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScPlatformBackendApplication.class, args);
	}	
		
		@Bean
		CommandLineRunner initDatabase(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
			return args -> {
				// Check if user already exists to avoid duplicates
				if( !userRepository.existsByEmail("superadmin@gmail.com")) {
			 
					User admin = User.builder()
									.fullName("takidjawad")
															.role(Role.SUPER_ADMIN)
																	.password(passwordEncoder.encode("admin123!"))
																			.email("superadmin@gmail.com")
																					.build();

					userRepository.save(admin);
					System.out.println("Admin user added to database.");
				}
			};
		
	

}
}

