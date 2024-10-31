//package ru.t1.java.demo.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.*;
//import org.springframework.kafka.listener.CommonErrorHandler;
//import org.springframework.kafka.listener.ContainerProperties;
//import org.springframework.kafka.listener.DefaultErrorHandler;
//import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
//import org.springframework.kafka.support.serializer.JsonDeserializer;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//import org.springframework.util.backoff.FixedBackOff;
//import ru.t1.java.demo.kafka.KafkaAccountProducer;
//import ru.t1.java.demo.kafka.KafkaClientProducer;
//import ru.t1.java.demo.kafka.KafkaTransactionProducer;
//import ru.t1.java.demo.kafka.MessageDeserializer;
//import ru.t1.java.demo.model.dto.AccountDto;
//import ru.t1.java.demo.model.dto.ClientDto;
//import ru.t1.java.demo.model.dto.TransactionDto;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Configuration
//public class DemoKafkaConfig<T> {
//
//    @Value("${t1.kafka.consumer.group-id}")
//    private String groupId;
//    @Value("${t1.kafka.bootstrap.server}")
//    private String servers;
//    @Value("${t1.kafka.session.timeout.ms:15000}")
//    private String sessionTimeout;
//    @Value("${t1.kafka.max.partition.fetch.bytes:300000}")
//    private String maxPartitionFetchBytes;
//    @Value("${t1.kafka.max.poll.records:1}")
//    private String maxPollRecords;
//    @Value("${t1.kafka.max.poll.interval.ms:3000}")
//    private String maxPollIntervalsMs;
//    @Value("${t1.kafka.topic.client_id_registered}")
//    private String clientTopic;
//
//
//    @Bean
//    public ConsumerFactory<String, ClientDto> consumerListenerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageDeserializer.class);
//        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "ru.t1.java.demo.model.dto.ClientDto");
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
//        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
//        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
//        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
//        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalsMs);
//        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, MessageDeserializer.class.getName());
//        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, MessageDeserializer.class);
//
//        DefaultKafkaConsumerFactory factory = new DefaultKafkaConsumerFactory<String, ClientDto>(props);
//        factory.setKeyDeserializer(new StringDeserializer());
//
//        return factory;
//    }
//
//
//    @Bean
//    ConcurrentKafkaListenerContainerFactory<String, ClientDto> kafkaListenerContainerFactory(@Qualifier("consumerListenerFactory") ConsumerFactory<String, ClientDto> consumerFactory) {
//        ConcurrentKafkaListenerContainerFactory<String, ClientDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factoryBuilder(consumerFactory, factory);
//        return factory;
//    }
//
//    private <T> void factoryBuilder(ConsumerFactory<String, T> consumerFactory, ConcurrentKafkaListenerContainerFactory<String, T> factory) {
//        factory.setConsumerFactory(consumerFactory);
//        factory.setBatchListener(true);
//        factory.setConcurrency(1);
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
//        factory.getContainerProperties().setPollTimeout(5000);
//        factory.getContainerProperties().setMicrometerEnabled(true);
//        factory.setCommonErrorHandler(errorHandler());
//    }
//
//    private CommonErrorHandler errorHandler() {
//        DefaultErrorHandler handler = new DefaultErrorHandler(new FixedBackOff(1000, 3));
//        handler.addNotRetryableExceptions(IllegalStateException.class);
//        handler.setRetryListeners((record, ex, deliveryAttempt) -> {
//            log.error(" RetryListeners message = {}, offset = {} deliveryAttempt = {}", ex.getMessage(), record.offset(), deliveryAttempt);
//        });
//        return handler;
//    }
//
//    @Bean("client")
//    @Primary
//    public KafkaTemplate<String, T> kafkaClientTemplate(@Qualifier("producerClientFactory") ProducerFactory<String, T> producerPatFactory) {
//        return new KafkaTemplate<>(producerPatFactory);
//    }
//
//
//    @Bean
//    @ConditionalOnProperty(value = "t1.kafka.producer.enable",
//            havingValue = "true",
//            matchIfMissing = true)
//    public KafkaClientProducer producerClient(@Qualifier("client") KafkaTemplate<String, ClientDto> template) {
//        template.setDefaultTopic(clientTopic);
//        return new KafkaClientProducer(template);
//    }
//
//    @Bean("producerClientFactory")
//    public ProducerFactory<String, T> producerClientFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        props.put(ProducerConfig.RETRIES_CONFIG, 3);
//        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
//        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
//        return new DefaultKafkaProducerFactory<>(props);
//    }
//
//}
package ru.t1.java.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.demo.kafka.*;
import ru.t1.java.demo.model.dto.*;
import ru.t1.java.demo.service.impl.ClientServiceImpl;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class DemoKafkaConfig {

    @Value("${t1.kafka.consumer.group-id}")
    private String groupId;
    @Value("${t1.kafka.bootstrap.server}")
    private String servers;
    @Value("${t1.kafka.session.timeout.ms:15000}")
    private String sessionTimeout;
    @Value("${t1.kafka.max.partition.fetch.bytes:300000}")
    private String maxPartitionFetchBytes;
    @Value("${t1.kafka.max.poll.records:1}")
    private String maxPollRecords;
    @Value("${t1.kafka.max.poll.interval.ms:3000}")
    private String maxPollIntervalsMs;
    @Value("${t1.kafka.topic.client_id_registered}")
    private String clientTopic;
    @Value("${t1.kafka.topic.accounts}")
    private String accountsTopic;
    @Value("${t1.kafka.topic.transactions}")
    private String transactionsTopic;

    // Конфигурация для ClientDto
    @Bean
    public ConsumerFactory<String, ClientDto> consumerFactoryClient() {
        Map<String, Object> props = commonConsumerProperties();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "ru.t1.java.demo.model.dto.ClientDto");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClientDto> kafkaListenerContainerFactoryClient() {
        ConcurrentKafkaListenerContainerFactory<String, ClientDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryClient());
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    // Конфигурация для AccountDto
    @Bean
    public ConsumerFactory<String, AccountDto> consumerFactoryAccount() {
        Map<String, Object> props = commonConsumerProperties();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "ru.t1.java.demo.model.dto.AccountDto");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AccountDto> kafkaListenerContainerFactoryAccount() {
        ConcurrentKafkaListenerContainerFactory<String, AccountDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryAccount());
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    // Конфигурация для TransactionDto
    @Bean
    public ConsumerFactory<String, TransactionDto> consumerFactoryTransaction() {
        Map<String, Object> props = commonConsumerProperties();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "ru.t1.java.demo.model.dto.TransactionDto");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionDto> kafkaListenerContainerFactoryTransaction() {
        ConcurrentKafkaListenerContainerFactory<String, TransactionDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryTransaction());
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    // Конфигурация для OperationInfoAbstractDto
    @Bean
    public ConsumerFactory<String, OperationInfoAbstractDto> consumerFactoryOperationTransaction() {
        Map<String, Object> props = commonConsumerProperties();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "ru.t1.java.demo.model.dto.OperationInfoAbstractDto");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OperationInfoAbstractDto> kafkaListenerContainerFactoryOperation() {
        ConcurrentKafkaListenerContainerFactory<String, OperationInfoAbstractDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryOperationTransaction());
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    // Общая конфигурация Consumer
    private Map<String, Object> commonConsumerProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalsMs);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, MessageDeserializer.class.getName());
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, MessageDeserializer.class);
        return props;
    }

    private CommonErrorHandler errorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(new FixedBackOff(1000, 3));
        handler.addNotRetryableExceptions(IllegalStateException.class);
        handler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.error("RetryListeners message = {}, offset = {} deliveryAttempt = {}", ex.getMessage(), record.offset(), deliveryAttempt);
        });
        return handler;
    }

    // Продюсер для ClientDto
    @Bean("client")
    @Primary
    public KafkaTemplate<String, ClientDto> kafkaClientTemplate(@Qualifier("producerClientFactory") ProducerFactory<String, ClientDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable", havingValue = "true", matchIfMissing = true)
    public KafkaClientProducer producerClient(@Qualifier("client") KafkaTemplate<String, ClientDto> template) {
        template.setDefaultTopic(clientTopic);
        return new KafkaClientProducer(template);
    }

    // Продюсер для AccountDto
    @Bean("account")
    public KafkaTemplate<String, AccountDto> kafkaAccountTemplate(@Qualifier("producerAccountFactory") ProducerFactory<String, AccountDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable", havingValue = "true", matchIfMissing = true)
    public KafkaAccountProducer producerAccount(@Qualifier("account") KafkaTemplate<String, AccountDto> template) {
        template.setDefaultTopic(accountsTopic);
        return new KafkaAccountProducer(template);
    }

    // Продюсер для TransactionDto
    @Bean("transaction")
    public KafkaTemplate<String, TransactionDto> kafkaTransactionTemplate(@Qualifier("producerTransactionFactory") ProducerFactory<String, TransactionDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable", havingValue = "true", matchIfMissing = true)
    public KafkaTransactionProducer producerTransaction(@Qualifier("transaction") KafkaTemplate<String, TransactionDto> template) {
        template.setDefaultTopic(transactionsTopic);
        return new KafkaTransactionProducer(template);
    }

    // Продюсер для OperationDto
    @Bean("operation")
    public KafkaTemplate<String, OperationInfoAbstractDto> kafkaOperationTemplate(@Qualifier("producerOperationFactory") ProducerFactory<String, OperationInfoAbstractDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable", havingValue = "true", matchIfMissing = true)
    public KafkaCreationTransactionProducer producerOperation(@Qualifier("operation") KafkaTemplate<String, OperationInfoAbstractDto> template) {
        template.setDefaultTopic(transactionsTopic);
        return new KafkaCreationTransactionProducer(template);
    }

    @Bean("producerClientFactory")
    public ProducerFactory<String, ClientDto> producerClientFactory() {
        return createProducerFactory(ClientDto.class);
    }

    @Bean("producerAccountFactory")
    public ProducerFactory<String, AccountDto> producerAccountFactory() {
        return createProducerFactory(AccountDto.class);
    }

    @Bean("producerTransactionFactory")
    public ProducerFactory<String, TransactionDto> producerTransactionFactory() {
        return createProducerFactory(TransactionDto.class);
    }

    @Bean("producerOperationFactory")
    public ProducerFactory<String, OperationInfoAbstractDto> producerOperationFactory() {
        return createProducerFactory(OperationInfoAbstractDto.class);
    }

    private <T> ProducerFactory<String, T> createProducerFactory(Class<T> clazz) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);
    }
    // для Long
//    @Bean("longProducerFactory")
//    public ProducerFactory<String, Long> producerLongFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
//        props.put(ProducerConfig.RETRIES_CONFIG, 3);
//        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
//        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
//        return new DefaultKafkaProducerFactory<>(props);
//    }
//
//    @Bean("longKafkaTemplate")
//    public KafkaTemplate<String, Long> kafkaLongTemplate(@Qualifier("longProducerFactory") ProducerFactory<String, Long> producerFactory) {
//        return new KafkaTemplate<>(producerFactory);
//    }
//    @Bean
//    public ConsumerFactory<String, Long> consumerLongFactory() {
//        Map<String, Object> props = commonConsumerProperties();
//        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Long.class.getName());
//        return new DefaultKafkaConsumerFactory<>(props);
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, Long> kafkaListenerContainerFactoryLong() {
//        ConcurrentKafkaListenerContainerFactory<String, Long> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerLongFactory());
//        factory.setBatchListener(true);
//        factory.setConcurrency(1);
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
//        factory.getContainerProperties().setPollTimeout(5000);
//        factory.setCommonErrorHandler(errorHandler());
//        return factory;
//    }
//
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean("stringProducerFactory")
    public ProducerFactory<String, String> producerStringFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean("stringKafkaTemplate")
    public KafkaTemplate<String, String> kafkaStringTemplate(@Qualifier("stringProducerFactory") ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, String> consumerStringFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "your-group-id");
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactoryString() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerStringFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }



}



