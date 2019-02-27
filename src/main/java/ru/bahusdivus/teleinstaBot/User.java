package ru.bahusdivus.teleinstaBot;


import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
class User {
    private int id;
    private String instId;
    private Long chatId;
    private Timestamp taskTaken;
    private Timestamp taskComplite;

    Set<UserTask> userTasks = new HashSet<>();

    User () {}

    User(String instId, Long chatId) {
        this.instId = instId;
        this.chatId = chatId;
    }

    User(int id, String instId, Long chatId, Timestamp taskTaken, Timestamp taskComplite) {
        this.id = id;
        this.instId = instId;
        this.chatId = chatId;
        this.taskTaken = taskTaken;
        this.taskComplite = taskComplite;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userId", updatable = false, nullable = false)
    int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    String getInstId() {
        return instId;
    }

    void setInstId(String instId) {
        this.instId = instId;
    }

    Long getChatId() {
        return chatId;
    }

    void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    Timestamp getTaskTaken() {
        return taskTaken;
    }

    void setTaskTaken(Timestamp taskTaken) {
        this.taskTaken = taskTaken;
    }

    Timestamp getTaskComplite() {
        return taskComplite;
    }

    void setTaskComplite(Timestamp taskComplite) {
        this.taskComplite = taskComplite;
    }

    @ManyToMany (cascade = {CascadeType.ALL})
    @JoinTable(
            name = "tasklist",
            joinColumns = { @JoinColumn(name = "userId")},
            inverseJoinColumns = {@JoinColumn(name = "taskId")}
    )
    public Set<UserTask> getUserTasks() {
        return userTasks;
    }

    public void setUserTasks(Set<UserTask> userTasks) {
        this.userTasks = userTasks;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", instId='" + instId + '\'' +
                ", chatId='" + chatId + '\'' +
                ", taskTaken=" + taskTaken +
                ", taskComplite=" + taskComplite +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (instId != null ? !instId.equals(user.getInstId()) : user.getInstId() != null) return false;
        return chatId != null ? chatId.equals(user.getChatId()) : user.getChatId() == null;
    }

    @Override
    public int hashCode() {
        int result = instId != null ? instId.hashCode() : 0;
        result = 31 * result + (chatId != null ? chatId.hashCode() : 0);
        return result;
    }
}
