package qj.blog.classreloading.example4.crossing;

public class Connection {
    public String getUserName() {
//        System.out.println("Connection CL: " + this.getClass().getClassLoader()); // Will output DefaultClassLoader
        return "Joe";
    }
}
