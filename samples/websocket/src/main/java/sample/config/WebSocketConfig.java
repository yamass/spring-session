package sample.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.ExpiringSession;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.socket.server.SessionRepositoryMessageInterceptor;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import sample.data.ActiveWebSocketUserRepository;
import sample.websocket.WebSocketConnectHandler;
import sample.websocket.WebSocketDisconnectHandler;

@Configuration
@EnableScheduling
@EnableWebSocketMessageBroker
public class WebSocketConfig<S extends ExpiringSession> extends AbstractWebSocketMessageBrokerConfigurer implements WebSocketConfigurer {

    @Autowired
    SessionRepository<S> sessionRepository;

    @Bean
    public WebSocketConnectHandler<S> webSocketConnectHandler(SimpMessageSendingOperations messagingTemplate, ActiveWebSocketUserRepository repository) {
        return new WebSocketConnectHandler<S>(messagingTemplate, repository);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/messages")
                .withSockJS()
                .setInterceptors(sessionRepositoryInterceptor());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(sessionRepositoryInterceptor());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue/", "/topic/");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Bean
    public SessionRepositoryMessageInterceptor<S> sessionRepositoryInterceptor() {
        return new SessionRepositoryMessageInterceptor<S>(sessionRepository);
    }

    @Bean
    public WebSocketDisconnectHandler<S> webSocketDisconnectHandler(SimpMessageSendingOperations messagingTemplate, ActiveWebSocketUserRepository repository) {
        return new WebSocketDisconnectHandler<S>(messagingTemplate, repository);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
            .addHandler(sessionRepositoryInterceptor(), "/messages");
    }
}