package qj.blog.classreloading.example4.reloadable;

import qj.blog.classreloading.example4.crossing.ConnectionPool;

public class UserService {
    ConnectionPool pool;

    @SuppressWarnings("UnusedDeclaration")
    public void hello() {
//        System.out.println("UserService CL: " + this.getClass().getClassLoader()); // Will output ExceptingClassLoader
        System.out.println("Hi " + pool.getConnection().getUserName());
    }
}