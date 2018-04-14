package com.arkaces.arka_arkb_channel_service;

import ark_java_client.*;
import com.arkaces.ApiClient;
import com.arkaces.aces_listener_api.AcesListenerApi;
import com.arkaces.aces_server.aces_service.config.AcesServiceConfig;
import com.arkaces.aces_server.ark_auth.ArkAuthConfig;
import org.h2.tools.Server;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.SQLException;

@Configuration
@EnableScheduling
@Import({AcesServiceConfig.class, ArkAuthConfig.class})
@EnableJpaRepositories
@EntityScan
public class ApplicationConfig {

    @Bean
    public ArkClient arkaClient(Environment environment) {
        ArkNetworkFactory arkaNetworkFactory = new ArkNetworkFactory();
        String arkaNetworkConfigPath = environment.getProperty("arkaNetworkConfigPath");
        ArkNetwork arkaNetwork = arkaNetworkFactory.createFromYml(arkaNetworkConfigPath);

        HttpArkClientFactory httpArkaClientFactory = new HttpArkClientFactory();
        return httpArkaClientFactory.create(arkaNetwork);
    }

    @Bean
    public ArkClient arkbClient(Environment environment) {
        ArkNetworkFactory arkbNetworkFactory = new ArkNetworkFactory();
        String arkbNetworkConfigPath = environment.getProperty("arkbNetworkConfigPath");
        ArkNetwork arkbNetwork = arkbNetworkFactory.createFromYml(arkbNetworkConfigPath);

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

    @Bean(initMethod="start", destroyMethod="stop")
    public Server h2WebConsonleServer () throws SQLException {
        return Server.createWebServer("-web","-webAllowOthers","-webDaemon","-webPort", "8082");
    }

    @Bean
    public BigDecimal arkbPerArka(Environment environment) {
        return environment.getProperty("arkbPerArka", BigDecimal.class);
    }

}
