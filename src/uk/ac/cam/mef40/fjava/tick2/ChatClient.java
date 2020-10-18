package uk.ac.cam.mef40.fjava.tick2;

import uk.ac.cam.cl.fjava.messages.*;

import java.io.*;
import java.net.Socket;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
            System.err.println("This application requires two arguments: <machine> <port>");
            return;
        }

        // Connect
        try {
            final Socket s = new Socket(server, port);
            final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

            Thread output =
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                InputStream is = s.getInputStream();
                                ObjectInputStream ois = new ObjectInputStream(is);

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
            OutputStream os = s.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            while (true) {
                byte[] input = r.readLine().getBytes();
                String inputStr = new String(input);
                Message msg;

                // Handle special commands
                if (inputStr.startsWith("\\nick ")) {
                    msg = new ChangeNickMessage(inputStr.substring(6));
                    oos.writeObject(msg);
                } else if (inputStr.equals("\\quit")) {
                    System.out.format("%s [Client] Connection terminated.\n", dateFormatter.format(new Date()));
                    s.close();
                    return;
                } else if (inputStr.startsWith("\\")) {
                    System.out.format("%s [Server] Unknown command \"%s\"\n", dateFormatter.format(new Date()), inputStr);
                } else {
                    // Forward chat message to other clients via server
                    msg = new ChatMessage(inputStr);
                    oos.writeObject(msg);
                }
            }
        } catch (IOException e) {
            System.err.format("Cannot connect to %s on port %s\n", server, port);
            return;
        }
    }
}
