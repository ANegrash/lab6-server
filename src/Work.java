import java.io.IOException;

public class Work {
    public Work() throws IOException, ClassNotFoundException {

        Connect connect = new Connect();
        connect.createServer();
    }
}
