package org.example.dto;

import java.io.Serializable;

public class ReviewDto implements Serializable {
    private String id;
    private String client;
    private int rating;
    private String comment;

    public ReviewDto() {
    }

    @Override
    public String toString() {
        return "ReviewDto{" +
                "id='" + id + '\'' +
                ", client='" + client + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public ReviewDto(String id, String client, int rating, String comment) {
        this.id = id;
        this.client = client;
        this.rating = rating;
        this.comment = comment;
    }

}
