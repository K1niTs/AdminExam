package org.example.listener;

import org.example.models.Review;
import org.example.service.ReviewService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {

    private final ReviewService reviewService;

    public RabbitMQListener(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @RabbitListener(queues = "reviewQueue")
    public void handleMessage(String message) throws InterruptedException {
        System.out.println("Received message: " + message);
        Thread.sleep(20000);
        processMessage(message);
    }

    private void processMessage(String message) {
        String[] parts = message.split(":");
        String operation = parts[0];

        switch (operation) {
            case "CREATE":
                String[] createParts = parts[1].split(",");
                // createParts[0] -> client
                // createParts[1] -> rating
                // createParts[2] -> comment
                Review newReview = new Review();
                newReview.setClient(createParts[0]);
                newReview.setRating(Integer.parseInt(createParts[1]));
                newReview.setComment(createParts[2]);
                reviewService.saveReview(newReview);
                break;

            case "UPDATE":
                String[] updateInfo = parts[1].split(",");
                // updateInfo[0] -> id
                // updateInfo[1] -> client
                // updateInfo[2] -> rating
                // updateInfo[3] -> comment
                String id = updateInfo[0];
                Review updatedReview = new Review();
                updatedReview.setId(id);
                updatedReview.setClient(updateInfo[1]);
                updatedReview.setRating(Integer.parseInt(updateInfo[2]));
                updatedReview.setComment(updateInfo[3]);
                reviewService.saveReview(updatedReview);
                break;

            case "DELETE":
                reviewService.deleteReview(parts[1]);
                break;

            default:
                System.out.println("Unknown operation: " + operation);
        }
    }
}
