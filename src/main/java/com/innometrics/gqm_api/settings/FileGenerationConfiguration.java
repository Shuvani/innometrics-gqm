package com.innometrics.gqm_api.settings;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "metrics-generation")
public class FileGenerationConfiguration {

    private String mulanInputFilesDirectory;

}
