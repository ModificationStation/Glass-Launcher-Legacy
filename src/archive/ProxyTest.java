package archive;

import java.io.IOException;

public class ProxyTest {
    public static void main(String[] args) throws IOException {
        new ThreadProxy(new boolean[] {true, true, true});
    }
}
