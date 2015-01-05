package qj.blog.classreloading.example4.reloadable;

import qj.blog.classreloading.example4.crossing.ConnectionPool;

@SuppressWarnings("UnusedDeclaration")
public class Context {
    public ConnectionPool pool;

    public UserService userService = new UserService();

    public void init() {
        userService.pool = pool;
    }
}
