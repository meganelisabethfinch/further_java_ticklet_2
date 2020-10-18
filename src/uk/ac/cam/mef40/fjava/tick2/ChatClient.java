package uk.ac.cam.mef40.fjava.tick2;

import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

import java.io.*;
import java.net.Socket;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClient {
    public static void main(String[] args) {
        String server = null;
        int port = 0;

        // Validate inputs
        try {
            server = args[0];
            port = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("This application requires two arguments: <machine> <port");
            return;
        }

        // Connect
        try {
            final Socket s = new Socket(server, port);

            Thread output =
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                InputStream is = s.getInputStream();
                                ObjectInputStream ois = new ObjectInputStream(is);
                                SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

                                while (true) {
                                    Message msg = (Message)ois.readObject();
                                    Date date = msg.getCreationTime();
                                    String dateString = dateFormatter.format(date);

                                    // Check for either of the server-to-client message types
                                    if (msg instanceof RelayMessage) {
                                        var rm = (RelayMessage)msg;
                                        System.out.format("%s [%s] %s\n", dateString, rm.getFrom(), rm.getMessage());
                                    } else if (msg instanceof StatusMessage) {
                                        var sm = (StatusMessage)msg;
                                        System.out.format("%s [Server] %s\n", dateString, sm.getMessage());
                                    }
                                }

                            } catch (IOException | ClassNotFoundException e) {
                                System.err.println(e.getMessage());
                                return;
                            }
                        }
                    };
            output.setDaemon(true);
            output.start();

            // Handle user input in main thread
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
            OutputStream outputStream = s.getOutputStream();
            while (true) {

            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }
    }
}
