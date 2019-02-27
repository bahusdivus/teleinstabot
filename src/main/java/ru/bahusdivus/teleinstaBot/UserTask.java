package ru.bahusdivus.teleinstaBot;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="task")
public class UserTask {
    private int id;
    private int ownerId;
    private String postId;
    private boolean likeRequired;
    private int commentRequiredLength;
    private String comment;
    private Timestamp created;
    private Set<User> users = new HashSet<>();

    UserTask() {}

    public UserTask(int id, int ownerId, String postId, boolean likeRequired, int commentRequiredLength, String comment, Timestamp created) {
        this.id = id;
        this.ownerId = ownerId;
        this.postId = postId;
        this.likeRequired = likeRequired;
        this.commentRequiredLength = commentRequiredLength;
        this.comment = comment;
        this.created = created;
    }

    public UserTask(int ownerId, String postId, boolean likeRequired, int commentRequiredLength, String comment, Timestamp created) {
        this.ownerId = ownerId;
        this.postId = postId;
        this.likeRequired = likeRequired;
        this.commentRequiredLength = commentRequiredLength;
        this.comment = comment;
        this.created = created;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="taskId", updatable = false, nullable = false)
    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    String getPostId() {
        return postId;
    }

    @Column(name="isLikeRequired")
    boolean isLikeRequired() {
        return likeRequired;
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

    @ManyToMany (mappedBy = "userTasks")
    public Set<User> getUsers() {
        return users;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setLikeRequired(boolean likeRequired) {
        this.likeRequired = likeRequired;
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

    public void setUsers(Set<User> users) {
        this.users = users;
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
                ", likeRequired=" + likeRequired +
                ", commentRequiredLength=" + commentRequiredLength +
                ", comment='" + comment + '\'' +
                ", created=" + created +
                '}';
    }
}
