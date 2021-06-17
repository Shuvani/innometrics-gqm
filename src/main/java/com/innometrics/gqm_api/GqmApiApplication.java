package com.innometrics.gqm_api;

import com.innometrics.gqm_api.settings.FileGenerationConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@SpringBootApplication
@EnableSwagger2
@EnableConfigurationProperties({FileGenerationConfiguration.class})
public class GqmApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GqmApiApplication.class, args);
	}

	@Bean
	public Docket swaggerConfiguration() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.innometrics.gqm_api"))
				.build()
				.apiInfo(apiDetails());
	}

	private ApiInfo apiDetails() {
		return new ApiInfo(
				"Innometrics recommendation service API",
				"This is the API to work with goals, questions and metrics of the Innometrics users",
				"v1",
				"For Innometrics dev team",
				new springfox.documentation.service.Contact(
						"Anna Gorb",
						"",
						"a.gorb@innopolis.university"
				),
				"License of API",
				"API license URL",
				Collections.emptyList()
		);
	}
}
