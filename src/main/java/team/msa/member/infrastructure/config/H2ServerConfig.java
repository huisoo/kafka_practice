package team.msa.member.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.h2.tools.Server;

import java.sql.SQLException;

@Slf4j
@Configuration
@EnableR2dbcAuditing
public class H2ServerConfig {
    private Server webServer;

    @Value("${spring.r2dbc.port}")
    String h2ConsolePort;

    @EventListener(ContextRefreshedEvent.class)
    public void start() throws SQLException {
        log.info("starting h2 console at port {}", h2ConsolePort);
        this.webServer = Server.createWebServer("-webPort", h2ConsolePort);
        this.webServer.start();
    }

    //종료이벤트
    @EventListener(ContextClosedEvent.class)
    public void stop() {
        log.info("stopping h2 console at port {}", h2ConsolePort);
        this.webServer.stop();
    }
}