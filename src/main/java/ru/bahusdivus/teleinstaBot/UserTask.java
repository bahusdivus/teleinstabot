package ru.bahusdivus.teleinstaBot;

import java.sql.Timestamp;

public class UserTask {
    private int id;
    private int ownerId;
    private String postId;
    private boolean isLikeRequired;
    private int commentRequiredLength;
    private String comment;
    private Timestamp created;

    UserTask(int id, int ownerId, String postId, boolean isLikeRequired, int commentRequiredLength, String comment, Timestamp created) {
        this.id = id;
        this.ownerId = ownerId;
        this.postId = postId;
        this.isLikeRequired = isLikeRequired;
        this.commentRequiredLength = commentRequiredLength;
        this.comment = comment;
        this.created = created;
    }

    public UserTask(int ownerId, String postId, boolean isLikeRequired, int commentRequiredLength, String comment, Timestamp created) {
        this.ownerId = ownerId;
        this.postId = postId;
        this.isLikeRequired = isLikeRequired;
        this.commentRequiredLength = commentRequiredLength;
        this.comment = comment;
        this.created = created;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    String getPostId() {
        return postId;
    }

    boolean isLikeRequired() {
        return isLikeRequired;
    }

    int getCommentRequiredLength() {
        return commentRequiredLength;
    }

    String getComment() {
        return comment;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setLikeRequired(boolean likeRequired) {
        isLikeRequired = likeRequired;
    }

    public void setCommentRequiredLength(int commentRequiredLength) {
        this.commentRequiredLength = commentRequiredLength;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserTask userTask = (UserTask) o;

        return id == userTask.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "UserTask{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", postId='" + postId + '\'' +
                ", isLikeRequired=" + isLikeRequired +
                ", commentRequiredLength=" + commentRequiredLength +
                ", comment='" + comment + '\'' +
                ", created=" + created +
                '}';
    }
}
