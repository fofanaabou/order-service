package com.sinignaci.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinignaci.orderservice.book.Book;
import com.sinignaci.orderservice.book.BookClient;
import com.sinignaci.orderservice.domain.Order;
import com.sinignaci.orderservice.domain.OrderStatus;
import com.sinignaci.orderservice.event.OrderAcceptedMessage;
import com.sinignaci.orderservice.web.OrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@Testcontainers
@Import(TestChannelBinderConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceApplicationTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutputDestination output;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookClient bookClient;


	@Container
	static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

	@Container
	static RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3"));


	@DynamicPropertySource
	static void dynamicProperties(DynamicPropertyRegistry registry) {

		registry.add("spring.r2dbc.url", OrderServiceApplicationTests::r2dbcUrl);
		registry.add("spring.r2dbc.username", postgresql::getUsername);
		registry.add("spring.r2dbc.password", postgresql::getPassword);
		registry.add("spring.flyway.url", postgresql::getJdbcUrl);


		registry.add("spring.rabbitmq.host", rabbitmq::getHost);
		registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
		registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
		registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
	}

	private static String r2dbcUrl() {
		return String.format("r2dbc:postgresql://%s:%s/%s", postgresql.getHost(),
				postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT), postgresql.getDatabaseName());
	}

    @Test
    void whenGetOrdersThenReturn() throws IOException {

        String bookIsbn = "1234567893";
        Book book = Book.builder().isbn(bookIsbn)
                .title("Titre")
                .author("Author")
                .price(9.9)
                .build();

        given(bookClient.getBookByIsbn(bookIsbn)).willReturn(Mono.just(book));
        OrderRequest orderRequest = OrderRequest.builder().isbn(bookIsbn).quantity(1).build();

        Order expectedOrder = webTestClient.post().uri("/orders")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class).returnResult().getResponseBody();

        assertThat(expectedOrder).isNotNull();
        assertThat(objectMapper.readValue(output.receive().getPayload(), OrderAcceptedMessage.class))
                .isEqualTo(OrderAcceptedMessage.builder().orderId(expectedOrder.id()).build());

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Order.class).value(orders -> assertThat(orders.stream().filter(order -> order.bookIsbn().equals(bookIsbn)).findAny()).isNotEmpty());
    }

	@Test
	void whenPostRequestAndBookExistsThenOrderAccepted() throws IOException {
		String bookIsbn = "1234567899";
		Book book = new Book(bookIsbn, "Title", "Author", 9.90);
		given(bookClient.getBookByIsbn(bookIsbn)).willReturn(Mono.just(book));
		OrderRequest orderRequest = new OrderRequest(bookIsbn, 3);

		Order createdOrder = webTestClient.post().uri("/orders")
				.bodyValue(orderRequest)
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(Order.class).returnResult().getResponseBody();

		assertThat(createdOrder).isNotNull();
		assertThat(createdOrder.bookIsbn()).isEqualTo(orderRequest.isbn());
		assertThat(createdOrder.quantity()).isEqualTo(orderRequest.quantity());
		assertThat(createdOrder.bookName()).isEqualTo(book.title() + " - " + book.author());
		assertThat(createdOrder.bookPrice()).isEqualTo(book.price());
		assertThat(createdOrder.status()).isEqualTo(OrderStatus.ACCEPTED);

		assertThat(objectMapper.readValue(output.receive().getPayload(), OrderAcceptedMessage.class))
				.isEqualTo(new OrderAcceptedMessage(createdOrder.id()));
	}

	@Test
	void whenPostRequestAndBookNotExistsThenOrderRejected() {
		String bookIsbn = "1234567894";
		given(bookClient.getBookByIsbn(bookIsbn)).willReturn(Mono.empty());
		OrderRequest orderRequest = new OrderRequest(bookIsbn, 3);

		Order createdOrder = webTestClient.post().uri("/orders")
				.bodyValue(orderRequest)
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(Order.class).returnResult().getResponseBody();

		assertThat(createdOrder).isNotNull();
		assertThat(createdOrder.bookIsbn()).isEqualTo(orderRequest.isbn());
		assertThat(createdOrder.quantity()).isEqualTo(orderRequest.quantity());
		assertThat(createdOrder.status()).isEqualTo(OrderStatus.REJECTED);
	}
}
