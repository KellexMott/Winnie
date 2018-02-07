package com.techart.winnie.models;

/**
 * Created by Kelvin on 05/06/2017.
 */

public class Comment {
    private String user;
    private String author;
    private String commentText;
    private Long timeCreated;


    public Comment() {
    }


    public String getUser() { return user;}
    public void setUser(String user) {
        this.user = user;
    }

    public String getCommentText() {
        return commentText;
    }
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Long getTimeCreated() {
        return timeCreated;
    }
    public void setTimeCreated(Long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
