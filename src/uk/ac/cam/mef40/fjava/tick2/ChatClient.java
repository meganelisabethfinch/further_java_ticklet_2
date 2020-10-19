package uk.ac.cam.mef40.fjava.tick2;

import uk.ac.cam.cl.fjava.messages.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

@FurtherJavaPreamble(
        author = "Megan Elisabeth Finch",
        date = "19th October 2020",
        crsid = "mef40",
        summary = "A chat client that uses objects and serialisation",
        ticker = FurtherJavaPreamble.Ticker.C
)
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
            System.out.format("%s [Client] Connected to %s on port %s.\n", dateFormatter.format(new Date()), server, port);

            Thread output =
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                InputStream is = s.getInputStream();
                                DynamicObjectInputStream ois = new DynamicObjectInputStream(is);

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
                                    } else if (msg instanceof NewMessageType) {
                                        var nm = (NewMessageType)msg;
                                        ois.addClass(nm.getName(), nm.getClassData());
                                        System.out.format("%s [Client] New class %s loaded.\n", dateString, nm.getName());


                                    } else {
                                        // System.out.format("%s [Client] New message of unknown type received.\n", dateString);
                                        String data = "";
                                        for (Field field : msg.getClass().getDeclaredFields()) {
                                            field.setAccessible(true);
                                            String fieldName = field.getName();
                                            Object fieldValue = field.get(msg);
                                            data += " " + fieldName + "(" + fieldValue.toString() + "),";
                                        }

                                        // Trim string
                                        if (data.endsWith(",")) {
                                            data = data.substring(0, data.length() - 1);
                                        }

                                        System.out.format("%s [Client] %s:%s\n", dateString, msg.getClass().getName(), data);

                                        for (Method method : msg.getClass().getMethods()) {
                                            if (method.isAnnotationPresent(Execute.class) && method.getParameterCount() == 0) {
                                                method.invoke(msg);
                                            }
                                        }
                                    }
                                }

                            } catch (IOException | ClassNotFoundException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
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
