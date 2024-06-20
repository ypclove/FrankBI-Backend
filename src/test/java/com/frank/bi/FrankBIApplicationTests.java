package com.frank.bi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

@SpringBootTest
@Slf4j
class FrankBIApplicationTests {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private Environment config;

    @Test
    void testSendMessage2SimpleQueue() {
        String queue = "simple.queue";
        String message = "hello, spring amqp!";
        rabbitTemplate.convertAndSend(queue, message);
    }

    @Test
    public void AlipayTest() {
        log.info(config.getProperty("app-id"));
    }
}
