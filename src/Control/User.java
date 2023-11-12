package Control;

import java.util.ArrayList;
import java.util.List;

public class User {
    private List<String> task;
    private String username;
    private int userId;

    public User(String username) {
        this.username = username;
        this.task = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getTask() {
        return task;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
