package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.ReviewDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {
    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private static final String REVIEW_CACHE_KEY_PREFIX = "review:";
    private static final String ALL_REVIEWS_CACHE_KEY = "reviews:all";
    private static final long CACHE_TTL_HOURS = 1L;

    private final RedisTemplate<String, ReviewDto> reviewRedisTemplate;
    private final RedisTemplate<String, List<ReviewDto>> reviewListRedisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCacheService(RedisTemplate<String, ReviewDto> reviewRedisTemplate,
                             RedisTemplate<String, List<ReviewDto>> reviewListRedisTemplate,
                             ObjectMapper objectMapper) {
        this.reviewRedisTemplate = reviewRedisTemplate;
        this.reviewListRedisTemplate = reviewListRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Получение отзыва из кеша по ID.
     *
     * @param reviewId ID отзыва.
     * @return ReviewDto или null, если не найден.
     */
    public ReviewDto getCachedReview(String reviewId) {
        String key = REVIEW_CACHE_KEY_PREFIX + reviewId;
        log.debug("Попытка получить отзыв из кеша с ключом: {}", key);

        try {
            ReviewDto cachedReview = reviewRedisTemplate.opsForValue().get(key);
            if (cachedReview != null) {
                log.info("CACHE HIT - Отзыв с ID: {} найден в Redis", reviewId);
                return cachedReview;
            }
            log.info("CACHE MISS - Отзыв с ID: {} не найден в Redis", reviewId);
        } catch (Exception e) {
            log.error("Ошибка при получении отзыва с ID: {} из кеша: {}", reviewId, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Кеширование отдельного отзыва.
     *
     * @param reviewId ID отзыва.
     * @param review   ReviewDto объект.
     */
    public void cacheReview(String reviewId, ReviewDto review) {
        String key = REVIEW_CACHE_KEY_PREFIX + reviewId;
        log.debug("Попытка кешировать отзыв с ключом: {}", key);

        try {
            reviewRedisTemplate.opsForValue().set(key, review, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("CACHE UPDATE - Отзыв с ID: {} успешно кеширован в Redis", reviewId);

            // Инвалидируем кеш всех отзывов после обновления отдельного отзыва
            evictAllReviews();
            log.debug("Инвалидирован кеш всех отзывов после кеширования отдельного отзыва");
        } catch (Exception e) {
            log.error("Ошибка при кешировании отзыва с ID: {}: {}", reviewId, e.getMessage(), e);
        }
    }

    /**
     * Получение всех отзывов из кеша.
     *
     * @return Список ReviewDto или пустой список, если не найдено.
     */
    @SuppressWarnings("unchecked")
    public List<ReviewDto> getCachedAllReviews() {
        log.debug("Попытка получить все отзывы из кеша");

        try {
            List<ReviewDto> cachedReviews = reviewListRedisTemplate.opsForValue().get(ALL_REVIEWS_CACHE_KEY);
            if (cachedReviews != null && !cachedReviews.isEmpty()) {
                log.info("CACHE HIT - Все отзывы найдены в Redis. Количество: {}", cachedReviews.size());
                return cachedReviews;
            }
            log.info("CACHE MISS - Отзывы не найдены в Redis");
        } catch (Exception e) {
            log.error("Ошибка при получении всех отзывов из кеша: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Кеширование всех отзывов.
     *
     * @param reviews Список ReviewDto объектов.
     */
    public void cacheAllReviews(List<ReviewDto> reviews) {
        log.debug("Попытка кешировать все отзывы");

        try {
            if (reviews == null || reviews.isEmpty()) {
                log.warn("Невозможно кешировать пустой или null список отзывов");
                return;
            }

            reviewListRedisTemplate.opsForValue().set(ALL_REVIEWS_CACHE_KEY, reviews, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("CACHE UPDATE - {} отзывов успешно кешированы в Redis", reviews.size());
        } catch (Exception e) {
            log.error("Ошибка при кешировании всех отзывов: {}", e.getMessage(), e);
        }
    }

    /**
     * Удаление отзыва из кеша по ID.
     *
     * @param reviewId ID отзыва.
     */
    public void evictReview(String reviewId) {
        String key = REVIEW_CACHE_KEY_PREFIX + reviewId;
        log.debug("Попытка удалить отзыв из кеша с ключом: {}", key);

        try {
            Boolean deleted = reviewRedisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("CACHE EVICT - Отзыв с ID: {} успешно удалён из Redis", reviewId);
            } else {
                log.info("CACHE MISS - Отзыв с ID: {} не найден в Redis при попытке удаления", reviewId);
            }

            // Инвалидируем кеш всех отзывов после удаления отдельного отзыва
            evictAllReviews();
            log.debug("Инвалидирован кеш всех отзывов после удаления отдельного отзыва");
        } catch (Exception e) {
            log.error("Ошибка при удалении отзыва с ID: {} из кеша: {}", reviewId, e.getMessage(), e);
        }
    }

    /**
     * Удаление всех отзывов из кеша.
     */
    public void evictAllReviews() {
        log.debug("Попытка удалить все отзывы из кеша");

        try {
            Boolean deleted = reviewListRedisTemplate.delete(ALL_REVIEWS_CACHE_KEY);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("CACHE EVICT - Все отзывы успешно удалены из Redis");
            } else {
                log.info("CACHE MISS - Кеш всех отзывов не найден при попытке удаления");
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении всех отзывов из кеша: {}", e.getMessage(), e);
        }
    }
}
