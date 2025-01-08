package org.example.controllers;

import org.example.dto.NewReviewRequest;
import org.example.dto.ReviewDto;
import org.example.grpc.ReviewGrpcClient;
import org.example.service.RabbitMQSender;
import org.example.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewGatewayController {

    private final ReviewGrpcClient reviewGrpcClient;
    private final RabbitMQSender rabbitMQSender;
    private final RedisCacheService cacheService;

    private static final Logger log = LoggerFactory.getLogger(ReviewGatewayController.class);

    public ReviewGatewayController(ReviewGrpcClient reviewGrpcClient,
                                   RabbitMQSender rabbitMQSender,
                                   RedisCacheService cacheService) {
        this.reviewGrpcClient = reviewGrpcClient;
        this.rabbitMQSender = rabbitMQSender;
        this.cacheService = cacheService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable String id) {
        log.info("Получение отзыва с ID: {}", id);

        ReviewDto cachedReview = cacheService.getCachedReview(id);
        if (cachedReview != null) {
            log.info("CACHE HIT - Возвращен отзыв с ID: {} из Redis", id);
            return ResponseEntity.ok(cachedReview);
        }

        ReviewDto review = reviewGrpcClient.getReviewById(id).orElse(null);
        if (review != null) {
            log.info("CACHE MISS - Отзыв с ID: {} получен через gRPC и будет кеширован", id);
            cacheService.cacheReview(id, review);
            return ResponseEntity.ok(review);
        } else {
            log.warn("Отзыв с ID: {} не найден", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        log.info("Запрос на получение всех отзывов");

        List<ReviewDto> cachedReviews = cacheService.getCachedAllReviews();
        if (!cachedReviews.isEmpty()) {
            log.info("CACHE HIT - Возвращено {} отзывов из Redis", cachedReviews.size());
            return ResponseEntity.ok(cachedReviews);
        }

        List<ReviewDto> reviews = reviewGrpcClient.getAllReviews();
        if (reviews.isEmpty()) {
            log.warn("Нет доступных отзывов для возврата");
            return ResponseEntity.noContent().build();
        }

        log.info("CACHE MISS - {} отзывов получены через gRPC и будут кешированы", reviews.size());
        cacheService.cacheAllReviews(reviews);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<String> createReview(@RequestBody NewReviewRequest request) {
        log.info("Создание нового отзыва для клиента: {}", request.getClient());

        String message = String.format("CREATE:%s,%d,%s",
                request.getClient(),
                request.getRating(),
                request.getComment());
        rabbitMQSender.sendMessage(message);
        log.info("Сообщение для создания отзыва отправлено в RabbitMQ: {}", message);

        cacheService.evictAllReviews();
        log.info("Кеш всех отзывов инвалидирован");

        return ResponseEntity.accepted().body("Запрос на создание отзыва принят");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateReview(@PathVariable String id, @RequestBody NewReviewRequest request) {
        log.info("Обновление отзыва с ID: {}", id);

        String message = String.format("UPDATE:%s,%s,%d,%s",
                id,
                request.getClient(),
                request.getRating(),
                request.getComment());
        rabbitMQSender.sendMessage(message);
        log.info("Сообщение для обновления отзыва отправлено в RabbitMQ: {}", message);

        cacheService.evictReview(id);
        cacheService.evictAllReviews();
        log.info("Кеш отзыва с ID: {} и кеш всех отзывов инвалидирован", id);

        return ResponseEntity.accepted().body("Запрос на обновление отзыва принят");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable String id) {
        log.info("Удаление отзыва с ID: {}", id);

        String message = String.format("DELETE:%s", id);
        rabbitMQSender.sendMessage(message);
        log.info("Сообщение для удаления отзыва отправлено в RabbitMQ: {}", message);

        cacheService.evictReview(id);
        cacheService.evictAllReviews();
        log.info("Кеш отзыва с ID: {} и кеш всех отзывов инвалидирован", id);

        return ResponseEntity.accepted().body("Запрос на удаление отзыва принят");
    }
}
