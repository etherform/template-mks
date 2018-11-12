package com.crutchbag.mks.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class MQConfig {

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
    public RabbitAdmin amqpAdmin(ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        return new RabbitTemplate(cf);
    }

    @Bean
    public Queue inputQueue(@Value("#{'${name}'.concat('_in')}") String queueName) {
        return new Queue(queueName, false, false, false);
    }

    @Bean
    public Queue logQueue(@Value("#{'${name}'.concat('_log')}") String queueName) {
        return new Queue(queueName, false, false, false);
    }

    @Bean
    public Queue outputQueue(@Value("#{'${name}'.concat('_out')}") String queueName) {
        return new Queue(queueName, false, false, false);
    }
}
