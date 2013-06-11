package server;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class TCPEchoServer {

    private static ServerSocket servSock;
    private static final int PORT = 1234;

    public static void main(String[] args) {
        System.out.println("Opening port...\n");
        try {
            servSock = new ServerSocket(PORT);      //Step 1.
        } catch (IOException e) {
            System.out.println("Unable to attach to port!");
            System.exit(1);
        }
        do {
            run();
        } while (true);
    }

    private static void run() {
        Socket link = null;                        //Step 2.

        boolean authorization = false;
        try {
            link = servSock.accept();               //Step 2.

            BufferedReader in =
                    new BufferedReader(
                    new InputStreamReader(
                    link.getInputStream())); //Step 3.
            PrintWriter out = new PrintWriter(
                    link.getOutputStream(), true); //Step 3.

            int numMessages = 0;
            String message = in.readLine();         //Step 4.
            File file = new File("src\\server\\mailMessages");
            File currentDirectory = new File(file.getAbsolutePath());
            String state = "";
            while (!message.equals("***CLOSE***")) {
                if (authorization) {
                    state = "AUTHORIZATION";
                    authorization = false;
                } else if (state.equals("AUTHORIZATION")) {
                    state = "";
                }
                System.out.println("Message received.");

                StringTokenizer st = new StringTokenizer(message);
                String start = st.nextToken();
                numMessages++;
                if (start.equals("STAT")) {
                    File[] mailMessages = file.listFiles();
                    long size = new Long(0);
                    for (int i = 0; i < mailMessages.length; i++) {
                        size += mailMessages[i].length();
                    }
                    out.println("+OK " + mailMessages.length + " " + size);
                } else if (start.equals("USER")) {
                    if (st.countTokens() == 1) {
                        String boxName = st.nextToken();
                        currentDirectory = new File(file.getAbsolutePath() + "\\" + boxName);
                        if (currentDirectory.exists()) {
                            authorization = true;
                            out.println("+OK " + boxName + " is a valid mainbox");
                        } else {
                            out.println("-ERR never heard of mainbox " + boxName);
                        }
                    } else {
                        out.println("-ERR uncorrect usage of USER, use USER <name>");
                    }
                } else if (start.equals("PASS")) {
                    if (state.equals("AUTHORIZATION")) {
                        if (st.hasMoreTokens()) {
                            String pass = st.nextToken(""); //to make the rest of the string one string
                            pass = pass.substring(1, pass.length() - 1);//to cut off the first space
                            out.println("+OK maildrop locked and ready");
                            state = "TRANSACTION";
                            //throw new UnsupportedOperationException("not yet implemented");
                        } else {
                            out.println("-ERR uncorrect usage of PASS, use PASS <string>");
                        }
                    } else {
                        out.println("-ERR unable to lock maildrop");
                    }
                } else if (start.equals("LIST")) {
                    System.out.println(state);
                    if (state.equals("TRANSACTION")) {
                        if (st.countTokens() == 1) {
                            int messageNumber = Integer.parseInt(st.nextToken());
                            File[] mailMessages = currentDirectory.listFiles();
                            out.println("+OK " + messageNumber + " messages (" + mailMessages[messageNumber].length() + " octets)");
                        } else if (st.countTokens() == 0) {
                            File[] mailMessages = currentDirectory.listFiles();
                            long size = new Long(0);
                            for (int i = 0; i < mailMessages.length; i++) {
                                size += mailMessages[i].length();
                            }
                            out.println("+OK " + mailMessages.length + " messages (" + size + " octets)");
                            for (int i = 0; i < mailMessages.length; i++) {
                                out.println(i+1 + " " + mailMessages[i].length());
                            }
                            out.println(".");
                        } else {
                            out.println("-ERR uncorrect usage of LIST, use LIST [msg]");
                        }
                    } else {
                        out.println("-ERR not in TRANSACTION state");
                    }
                } else if (start.equals("RETR")) {
                } else if (start.equals("DELE ")) {
                } else if (start.equals("QUIT ")) {
                } else {
                    out.println("Message " + numMessages
                            + ": " + message);     //Step 4.
                }
                message = in.readLine();
            }
            System.exit(0);
            out.println(numMessages
                    + " messages received.");	//Step 4.
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println(
                        "\n* Closing connection... *");
                link.close();				    //Step 5.
            } catch (IOException e) {
                System.out.println("Unable to disconnect!");
                System.exit(1);
            }
        }
    }
}
