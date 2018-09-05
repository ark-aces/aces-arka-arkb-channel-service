package com.arkaces.arka_arkb_channel_service;

import ark_java_client.*;
import com.arkaces.ApiClient;
import com.arkaces.aces_listener_api.AcesListenerApi;
import com.arkaces.aces_server.aces_service.config.AcesServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;

@Configuration
@EnableScheduling
@Import({AcesServiceConfig.class})
@EnableJpaRepositories
@EntityScan
@Slf4j
public class ApplicationConfig {

    @Bean
    public ArkClient arkaClient(Environment environment) {
        ArkNetworkFactory arkaNetworkFactory = new ArkNetworkFactory();
        String arkaNetworkConfigPath = environment.getProperty("arkaNetworkConfigPath");
        ArkNetwork arkaNetwork = arkaNetworkFactory.createFromYml(arkaNetworkConfigPath);

        log.info("Bootstrapping Arka client with network peers");
        HttpArkClientFactory httpArkaClientFactory = new HttpArkClientFactory();
        return httpArkaClientFactory.create(arkaNetwork);
    }

    @Bean
    public ArkClient arkbClient(Environment environment) {
        ArkNetworkFactory arkbNetworkFactory = new ArkNetworkFactory();
        String arkbNetworkConfigPath = environment.getProperty("arkbNetworkConfigPath");
        ArkNetwork arkbNetwork = arkbNetworkFactory.createFromYml(arkbNetworkConfigPath);

        log.info("Bootstrapping Arkb client with network peers");
        HttpArkClientFactory httpArkbClientFactory = new HttpArkClientFactory();
        return httpArkbClientFactory.create(arkbNetwork);
    }

    @Bean
    public AcesListenerApi arkaListener(Environment environment) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(environment.getProperty("arkaListener.url"));
        if (environment.containsProperty("arkaListener.apikey")) {
            apiClient.setUsername("token");
            apiClient.setPassword(environment.getProperty("arkaListener.apikey"));
        }

        return new AcesListenerApi(apiClient);
    }

    @Bean
    public String arkaEventCallbackUrl(Environment environment) {
        return environment.getProperty("arkaEventCallbackUrl");
    }

    @Bean
    public BigDecimal arkbPerArka(Environment environment) {
        return environment.getProperty("arkbPerArka", BigDecimal.class);
    }

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());

        return eventMulticaster;
    }
    
    @Bean
    public String arkaUnit(Environment environment) {
        return environment.getProperty("arkaUnit");
    }
    
    @Bean
    public String arkbUnit(Environment environment) {
        return environment.getProperty("arkbUnit");
    }
    
}
