package org.example.grpc;

import io.grpc.stub.StreamObserver;
import org.example.domainservice.*;
import org.example.models.Review;
import org.example.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewServiceGrpcImpl extends ReviewServiceGrpc.ReviewServiceImplBase {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewServiceGrpcImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void getReview(GetReviewRequest request, StreamObserver<ReviewResponse> responseObserver) {
        Optional<Review> optionalReview = reviewRepository.findById(request.getId());

        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            ReviewResponse response = ReviewResponse.newBuilder()
                    .setId(review.getId())
                    .setClient(review.getClient())
                    .setRating(review.getRating())
                    .setComment(review.getComment())
                    .build();
            responseObserver.onNext(response);
        } else {
            responseObserver.onNext(ReviewResponse.getDefaultInstance());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void createReview(CreateReviewRequest request, StreamObserver<CreateReviewResponse> responseObserver) {
        Review review = new Review(
                request.getClient(),
                request.getRating(),
                request.getComment()
        );
        review = reviewRepository.save(review);

        CreateReviewResponse response = CreateReviewResponse.newBuilder()
                .setId(review.getId())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateReview(UpdateReviewRequest request, StreamObserver<UpdateReviewResponse> responseObserver) {
        Optional<Review> optionalReview = reviewRepository.findById(request.getId());
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setClient(request.getClient());
            review.setRating(request.getRating());
            review.setComment(request.getComment());

            reviewRepository.save(review);

            UpdateReviewResponse response = UpdateReviewResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
        } else {
            UpdateReviewResponse response = UpdateReviewResponse.newBuilder()
                    .setSuccess(false)
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deleteReview(DeleteReviewRequest request, StreamObserver<DeleteReviewResponse> responseObserver) {
        if (reviewRepository.existsById(request.getId())) {
            reviewRepository.deleteById(request.getId());
            DeleteReviewResponse response = DeleteReviewResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
        } else {
            DeleteReviewResponse response = DeleteReviewResponse.newBuilder()
                    .setSuccess(false)
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getAllReviews(GetAllReviewsRequest request, StreamObserver<GetAllReviewsResponse> responseObserver) {
        var reviews = reviewRepository.findAll();

        var reviewResponses = reviews.stream()
                .map(r -> ReviewResponse.newBuilder()
                        .setId(r.getId())
                        .setClient(r.getClient())
                        .setRating(r.getRating())
                        .setComment(r.getComment())
                        .build()
                )
                .collect(Collectors.toList());

        GetAllReviewsResponse response = GetAllReviewsResponse.newBuilder()
                .addAllReviews(reviewResponses)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
