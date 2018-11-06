package com.crutchbag.mks.amqp;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Setter;

@EnableRabbit
@Configuration
public class MQConfig {

    @Setter
    @Value("#{'${name}'.concat('_input')}")
    private String inputQueueName;

    @Setter
    @Value("#{'${name}'.concat('_log')}")
    private String logQueueName;

    @Setter
    @Value("#{'${name}'.concat('_output')}")
    private String outputQueueName;

    @Bean
    public ConnectionFactory connectionFactory(
            @Value("${amqp.host}") String host,
            @Value("${amqp.user}") String user,
            @Value("${amqp.password}") String password) {
        CachingConnectionFactory cf = new CachingConnectionFactory(host);
        cf.setUsername(user);
        cf.setPassword(password);
        return cf;
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        return new RabbitTemplate(cf);
    }

    @Bean
    public Queue inputQueue() {
        return new Queue(inputQueueName, false, false, false);
    }

    @Bean
    public Queue logQueue() {
        return new Queue(logQueueName, false, false, false);
    }

    @Bean
    public Queue outputQueue() {
        return new Queue(outputQueueName, false, false, false);
    }
}
