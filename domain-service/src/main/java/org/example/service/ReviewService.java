package org.example.service;

import org.example.models.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewService {
    List<Review> getAllReviews();
    Optional<Review> getReviewById(String id);
    Review saveReview(Review review);
    void deleteReview(String id);
}
