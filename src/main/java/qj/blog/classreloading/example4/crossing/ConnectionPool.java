package qj.blog.classreloading.example4.crossing;

public class ConnectionPool {
    Connection conn = new Connection();

    public Connection getConnection() {
        return conn;
    }
}
	