package com.ric.mks;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class App {

    //public String id = Integer.toHexString((int)(Math.random()*0x1000000));
    private String name;
    private String logQueneName;
    private String controlQueneIn;

    @Autowired
    private AmqpTemplate amqp;
    @Autowired
    private MksControl<Commands> mksControl;
    @Autowired
    private MksControl<MksCommands> mksCall;

    public void log(String str) {
        System.out.println(str);
    }

    public App(@Value("${name}") String appName) {
        this.name = appName;
        this.logQueneName = appName + "_log";
        this.controlQueneIn = appName + "_control";
    }

    public void sendLog(String str) {
        amqp.convertAndSend(logQueneName, str);
    }

    //public String getId() {return id;}
    public String getName() {return name;}
    public void setLogQueneName(String s) {this.logQueneName = s;}
    public void setControlQueneName(String s) {this.controlQueneIn =s;}

    @Bean
    public MksControl<Commands> mksController(Commands commands) {
        return new MksControl<Commands>(commands);
    }

    @Bean
    public MksControl<MksCommands> mkdCommands(MksCommands cmds) {
        return new MksControl<MksCommands>(cmds);
    }

    @Bean
    public ConnectionFactory connectionFactory(
            @Value("${amqp_host}") String host,
            @Value("${amqp_user}") String user,
            @Value("${amqp_password}") String password) {
        log("RMQ host:"+host);
        log("App name:"+getName());
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory(host);
        connectionFactory.setUsername(user);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @RabbitListener(queues = "#{inputQueue.name}")
    private void cmdReceive(Message message) {
        String msg = new String(message.getBody());
        CommandReturn ret = mksControl.control(msg);
        if (!ret.error) {
            sendLog(ret.log);
            return;
        }
        ret = mksCall.control(msg);
        sendLog(ret.log);
    }

    @Bean
    private Queue inputQueue() {
        return new Queue(controlQueneIn, false, false, false);
    }

    @Bean
    private Queue genLogQueue() {
        return new Queue(logQueneName, false, false, false);
    }



}
