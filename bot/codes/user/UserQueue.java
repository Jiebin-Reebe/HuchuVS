package user;

import java.util.ArrayList;
import java.util.List;

public class UserQueue {
    private final List<User> users = new ArrayList<>();

    public void add(User user) {
        users.add(user);
    }

    public List<User> getAll() {
        return new ArrayList<>(users);
    }

    public void clear() {
        users.clear();
    }

    public int size() {
        return users.size();
    }

    public boolean removeByName(String name) {
        return users.removeIf(user -> user.name.equalsIgnoreCase(name));
    }

    public boolean isRoleTaken(String role) {
        for (User u : users) {
            if (u.role.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRoleFull(String role) {
        long count = users.stream()
                .filter(u -> u.role.equalsIgnoreCase(role))
                .count();
        return count >= 2;
    }

    public boolean exists(String name) {
        for (User u : users) {
            if (u.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
