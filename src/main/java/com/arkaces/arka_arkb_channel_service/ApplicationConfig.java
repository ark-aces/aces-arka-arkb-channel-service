package com.arkaces.arka_arkb_channel_service;

import ark_java_client.*;
import com.arkaces.aces_server.aces_service.config.AcesServiceConfig;
import com.arkaces.aces_server.aces_service.notification.NotificationService;
import com.arkaces.aces_server.aces_service.notification.NotificationServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.MailSender;
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
    public String arkaEventCallbackUrl(Environment environment) {
        return environment.getProperty("arkaEventCallbackUrl");
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

    @Bean
    public Integer arkScanDepth(Environment environment) {
        return environment.getProperty("arkScanDepth", Integer.class);
    }

    @Bean
    public Integer arkMinConfirmations(Environment environment) {
        return environment.getProperty("arkMinConfirmations", Integer.class);
    }

    @Bean
    @ConditionalOnProperty(value = "notifications.enabled", havingValue = "true")
    public NotificationService emailNotificationService(Environment environment, MailSender mailSender) {
        return new NotificationServiceFactory().createEmailNotificationService(
                environment.getProperty("serverInfo.name"),
                environment.getProperty("notifications.fromEmailAddress"),
                environment.getProperty("notifications.recipientEmailAddress"),
                mailSender
        );
    }

    @Bean
    @ConditionalOnProperty(value = "notifications.enabled", havingValue = "false", matchIfMissing = true)
    public NotificationService noOpNotificationService() {
        return new NotificationServiceFactory().createNoOpNotificationService();
    }

    @Bean
    public BigDecimal lowCapacityThreshold(Environment environment) {
        return environment.getProperty("lowCapacityThreshold", BigDecimal.class);
    }

}
