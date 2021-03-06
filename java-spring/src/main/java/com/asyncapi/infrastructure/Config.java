package com.asyncapi.infrastructure;

import com.asyncapi.service.MessageHandlerService;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;

@Configuration
public class Config {

    @Value("${mqtt.broker.address}")
    private String address;

    @Value("${mqtt.broker.timeout.connection}")
    private int connectionTimeout;

    @Value("${mqtt.broker.timeout.disconnection}")
    private long disconnectionTimeout;

    @Value("${mqtt.broker.timeout.completion}")
    private long completionTimeout;

    @Value("${mqtt.broker.clientId}")
    private String clientId;

    @Value("${mqtt.broker.username}")
    private String username;

    @Value("${mqtt.broker.password}")
    private String password;

    

    
    @Value("${mqtt.topic.receiveLightMeasurement}")
    private String receiveLightMeasurementTopic;
    
    @Value("${mqtt.topic.turnOn}")
    private String turnOnTopic;
    
    @Value("${mqtt.topic.turnOff}")
    private String turnOffTopic;
    
    @Value("${mqtt.topic.dimLight}")
    private String dimLightTopic;
    

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        
        
        
        
        options.setServerURIs(new String[] { address });
        if (!StringUtils.isEmpty(username)) {
            options.setUserName(username);
        }
        if (!StringUtils.isEmpty(password)) {
            options.setPassword(password.toCharArray());
        }
        options.setConnectionTimeout(connectionTimeout);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Autowired
    MessageHandlerService messageHandlerService;

    
    @Bean
    public IntegrationFlow turnOnFlow() {
        return IntegrationFlows.from(turnOnInbound())
                .handle(messageHandlerService::handleTurnOn)
                .get();
    }

    @Bean
    public MessageProducerSupport turnOnInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId,
                mqttClientFactory(), turnOnTopic);
        adapter.setCompletionTimeout(connectionTimeout);
        adapter.setDisconnectCompletionTimeout(disconnectionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        return adapter;
    }
    
    @Bean
    public IntegrationFlow turnOffFlow() {
        return IntegrationFlows.from(turnOffInbound())
                .handle(messageHandlerService::handleTurnOff)
                .get();
    }

    @Bean
    public MessageProducerSupport turnOffInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId,
                mqttClientFactory(), turnOffTopic);
        adapter.setCompletionTimeout(connectionTimeout);
        adapter.setDisconnectCompletionTimeout(disconnectionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        return adapter;
    }
    
    @Bean
    public IntegrationFlow dimLightFlow() {
        return IntegrationFlows.from(dimLightInbound())
                .handle(messageHandlerService::handleDimLight)
                .get();
    }

    @Bean
    public MessageProducerSupport dimLightInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId,
                mqttClientFactory(), dimLightTopic);
        adapter.setCompletionTimeout(connectionTimeout);
        adapter.setDisconnectCompletionTimeout(disconnectionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        return adapter;
    }
    

    
    @Bean
    public MessageChannel receiveLightMeasurementOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "receiveLightMeasurementOutboundChannel")
    public MessageHandler receiveLightMeasurementOutbound() {
        MqttPahoMessageHandler pahoMessageHandler = new MqttPahoMessageHandler(clientId, mqttClientFactory());
        pahoMessageHandler.setAsync(true);
        pahoMessageHandler.setCompletionTimeout(completionTimeout);
        pahoMessageHandler.setDisconnectCompletionTimeout(disconnectionTimeout);
        pahoMessageHandler.setDefaultTopic(receiveLightMeasurementTopic);
        
        
        return pahoMessageHandler;
    }
    

}
