package org.example.dto;

public class NewReviewRequest {
    private String client;
    private int rating;

    @Override
    public String toString() {
        return "NewReviewRequest{" +
                "client='" + client + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                '}';
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private String comment;

    public NewReviewRequest() {
    }

    public NewReviewRequest(String client, int rating, String comment) {
        this.client = client;
        this.rating = rating;
        this.comment = comment;
    }


}
