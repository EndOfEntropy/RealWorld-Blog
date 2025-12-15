package io.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Demo {

	public static void main(String[] args) {
		SpringApplication.run(Demo.class, args);
		
//		ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(Demo.class, args);
//		UserRepository userRepository = configurableApplicationContext.getBean(UserRepository.class);
//		
//		User misterT = new User("anyemail@gmail.com", new Profile("MisterT", "I like dogs", "https://www.com", false));
//		User johnDoe = new User("john.doe@gmail.com", new Profile("johndoe", "I like apples", "https://zzz.com", false));
//		User cdn = new User("cdn@gmail.com", new Profile("cdn", "I like bananas", "https://zzz.com", false));
//		
//		cdn.followUser(johnDoe);
//		johnDoe.followUser(misterT);
//		misterT.followUser(johnDoe);
//		
//		userRepository.saveAll(Arrays.asList(misterT, johnDoe, cdn));
	}

}