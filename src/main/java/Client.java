import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        final String host = "127.0.0.1";
        final int port = 8989;

        try (Socket socket = new Socket(host, port)) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.println("блокчейн");
            System.out.println(reader.readLine());
            while (reader.ready()) {
                System.out.println(reader.readLine());
            }


        } catch (IOException e) {
            e.getMessage();
        }




    }
}
