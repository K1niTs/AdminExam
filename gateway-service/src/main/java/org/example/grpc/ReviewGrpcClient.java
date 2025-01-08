package org.example.grpc;

import org.example.domainservice.*;
import org.example.dto.NewReviewRequest;
import org.example.dto.ReviewDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(ReviewGrpcClient.class);

    private final ReviewServiceGrpc.ReviewServiceBlockingStub reviewStub;

    public ReviewGrpcClient(ReviewServiceGrpc.ReviewServiceBlockingStub reviewStub) {
        this.reviewStub = reviewStub;
    }

    public Optional<ReviewDto> getReviewById(String id) {
        log.info("Getting review by ID: {}", id);
        GetReviewRequest request = GetReviewRequest.newBuilder()
                .setId(id)
                .build();
        ReviewResponse response = reviewStub.getReview(request);

        if (response.equals(ReviewResponse.getDefaultInstance())) {
            log.debug("Review not found with ID: {}", id);
            return Optional.empty();
        }

        return Optional.of(convertToDto(response));
    }

    public List<ReviewDto> getAllReviews() {
        log.info("Getting all reviews");
        GetAllReviewsRequest request = GetAllReviewsRequest.newBuilder().build();
        GetAllReviewsResponse response = reviewStub.getAllReviews(request);

        return response.getReviewsList().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReviewDto createReview(NewReviewRequest request) {
        log.info("Creating new review for client: {}", request.getClient());
        CreateReviewRequest grpcReq = CreateReviewRequest.newBuilder()
                .setClient(request.getClient())
                .setRating(request.getRating())
                .setComment(request.getComment())
                .build();
        CreateReviewResponse response = reviewStub.createReview(grpcReq);
        return getReviewById(response.getId()).orElseThrow();
    }

    public Optional<ReviewDto> updateReview(String id, NewReviewRequest request) {
        log.info("Updating review with ID: {}", id);
        UpdateReviewRequest grpcReq = UpdateReviewRequest.newBuilder()
                .setId(id)
                .setClient(request.getClient())
                .setRating(request.getRating())
                .setComment(request.getComment())
                .build();
        UpdateReviewResponse response = reviewStub.updateReview(grpcReq);

        if (response.getSuccess()) {
            return getReviewById(id);
        } else {
            return Optional.empty();
        }
    }

    public boolean deleteReview(String id) {
        log.info("Deleting review with ID: {}", id);
        DeleteReviewRequest request = DeleteReviewRequest.newBuilder()
                .setId(id)
                .build();
        DeleteReviewResponse response = reviewStub.deleteReview(request);
        return response.getSuccess();
    }

    private ReviewDto convertToDto(ReviewResponse r) {
        return new ReviewDto(
                r.getId(),
                r.getClient(),
                r.getRating(),
                r.getComment()
        );
    }
}
