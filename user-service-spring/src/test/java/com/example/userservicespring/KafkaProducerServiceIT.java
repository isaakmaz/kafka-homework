package com.example.userservicespring;

import com.example.userservicespring.dto.CreateUserRequestDto;
import com.example.userservicespring.dto.EventType;
import com.example.userservicespring.dto.UserEventDto;
import com.example.userservicespring.entity.User;
import com.example.userservicespring.repository.UserRepository;
import com.example.userservicespring.service.UserService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
@Testcontainers
class KafkaProducerServiceIT {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.2"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void shouldSendUserCreatedEventToKafka() throws InterruptedException {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                kafka.getBootstrapServers(), "test-group", "true");

        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserEventDto.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.userservicespring.dto");

        DefaultKafkaConsumerFactory<String, UserEventDto> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("user-events");
        KafkaMessageListenerContainer<String, UserEventDto> container = new KafkaMessageListenerContainer<>(cf, containerProperties);

        final BlockingQueue<ConsumerRecord<String, UserEventDto>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, UserEventDto>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, 1);

        try {
            String name = "Test Kafka";
            String email = "kafka-test@example.com";
            CreateUserRequestDto requestDto = new CreateUserRequestDto(name, email, 40);

            User savedUser = new User();
            savedUser.setId(1L);
            savedUser.setName(name);
            savedUser.setEmail(email);
            savedUser.setAge(40);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            userService.save(requestDto);

            ConsumerRecord<String, UserEventDto> singleRecord = records.poll(5, TimeUnit.SECONDS);
            assertThat(singleRecord).isNotNull();
            UserEventDto event = singleRecord.value();
            assertThat(event.eventType()).isEqualTo(EventType.USER_CREATED);
            assertThat(event.email()).isEqualTo(email);
            assertThat(event.name()).isEqualTo(name);
        } finally {
            container.stop();
        }
    }
}