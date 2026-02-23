package com.practice.users;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UserStore {
    private static final Map<String, User> USERS = new ConcurrentHashMap<>();

    static {
        USERS.put("1001", new User("1001", "Andrea Garcia", "andrea@demo.com"));
        USERS.put("1002", new User("1002", "Pepito Perez", "pepito@demo.com"));
    }

    private UserStore() {}

    public static Map<String, User> users() {
        return USERS;
    }
}

