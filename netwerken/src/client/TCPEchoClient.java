package client;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class TCPEchoClient {

    private static InetAddress host;
    private static final int PORT = 1234;

    public static void main(String[] args) {
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Host ID not found!");
            System.exit(1);
        }
        userProgram();
    }

    private static void run() {
        Socket link = null;				//Step 1.

        try {
            link = new Socket(host, PORT);		//Step 1.

            BufferedReader in =
                    new BufferedReader(new InputStreamReader(link.getInputStream()));//Step 2.

            PrintWriter out = new PrintWriter(
                    link.getOutputStream(), true);	 //Step 2.

            //Set up stream for keyboard entry...
            BufferedReader userEntry =
                    new BufferedReader(new InputStreamReader(System.in));

            String message = "";
            String response;
            do {
                System.out.print("Enter message: ");

                message = userEntry.readLine();
                out.println(message); 		//Step 3.
                System.out.println("");
                do {
                    response = in.readLine();
                    System.out.println("SERVER> " + response);
                } while (in.ready());
                if (message.equals("QUIT") && response.startsWith("+OK")) {
                    link.close();
                    System.exit(0);
                }

            } while (!message.equals("***CLOSE***"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println(
                        "\n* Closing connection... *");
                link.close();				//Step 4.
            } catch (IOException e) {
                System.out.println("Unable to disconnect!");
                System.exit(1);
            }
        }
    }

    private static void userProgram() {
        Socket link = null;				//Step 1.

        try {
            link = new Socket(host, PORT);		//Step 1.

            BufferedReader in =
                    new BufferedReader(new InputStreamReader(link.getInputStream()));//Step 2.

            PrintWriter out = new PrintWriter(
                    link.getOutputStream(), true);	 //Step 2.

            //Set up stream for keyboard entry...
            BufferedReader userEntry =
                    new BufferedReader(new InputStreamReader(System.in));

            String message = "";
            String response;
            System.out.println("Welkom to this POP3 client");
            System.out.println("to view the posible commands use");
            System.out.println("\"help\" or \"?\"\n");
            do {
                System.out.print("Enter Command: ");
                message = userEntry.readLine();
                if (message.equals("login")) {
                    System.out.print("Enter Username: ");
                    message = userEntry.readLine();
                    out.println("USER " + message);
                    response = in.readLine();
                    if (response.startsWith("+OK")) {
                        System.out.print("Enter Password: ");
                        message = userEntry.readLine();
                        out.println("PASS " + message);
                        response = in.readLine();
                        if (response.startsWith("+OK")) {
                            System.out.println("you are now loged in.\n");
                        } else {
                            System.out.println("could not log in\n(" + response + ")\n");
                        }
                    } else {
                        System.out.println("could not log in\n(" + response + ")\n");
                    }
                } else if (message.equals("information")) {
                    out.println("STAT");
                    response = in.readLine();
                    if (response.startsWith("+OK")) {
                        StringTokenizer st = new StringTokenizer(response);
                        st.nextToken();
                        System.out.println("the mail box contains: " + st.nextToken() + " emails of total size " + st.nextToken() + "\n");
                    } else {
                        System.out.println("could not retrive server information\n(" + response + ")\n");
                    }
                } else if (message.equals("list")) {
                    out.println("LIST");
                    response = in.readLine();
                    if (response.startsWith("+OK")) {
                        response = in.readLine();
                        do {
                            StringTokenizer st = new StringTokenizer(response);
                            System.out.println("message: " + st.nextToken() + " of size " + st.nextToken());
                            response = in.readLine();
                        } while (!response.equals("."));
                        System.out.println("");
                    } else {
                        System.out.println("could not retrive server list\n(" + response + ")\n");
                    }
                } else if (message.startsWith("delete")) {
                    message = message.replace("delete ", "");
                    int messageNumber = 0;
                    messageNumber = Integer.parseInt(message);
                    if (messageNumber != 0) {
                        out.println("DELE " + messageNumber);
                        response = in.readLine();
                        if (response.startsWith("+OK")) {
                            System.out.println("the message has been deleted\n");
                        } else {
                            System.out.println("could not delete message\n(" + response + ")\n");
                        }
                    } else {
                        System.out.println("could not parse int\n");
                    }
                } else if (message.startsWith("retrive")) {
                    message = message.replace("retrive ", "");
                    int messageNumber = 0;
                    messageNumber = Integer.parseInt(message);
                    if (messageNumber != 0) {
                        out.println("RETR " + messageNumber);
                        response = in.readLine();
                        if (response.startsWith("+OK")) {
                            response = in.readLine();
                            do {
                                System.out.println(response);
                                response = in.readLine();
                            } while (!response.equals("."));
                            System.out.println("");
                        } else {
                            System.out.println("could not read message\n(" + response + ")\n");
                        }
                    } else {
                        System.out.println("could not parse int\n");
                    }
                } else if (message.equals("logout")) {
                    out.println("QUIT");
                    response = in.readLine();
                    if (response.startsWith("+OK")) {
                        link.close();
                        System.exit(0);
                    }
                } else if (message.equals("help") || message.equals("?")) {
                    System.out.println("login \t\t- will prompt you asking for username and password");
                    System.out.println("logout \t\t- will close the link between you and the server and exit the programm");
                    System.out.println("list \t\t- shows a list of messages on the server");
                    System.out.println("information \t- shows you how much emails there are and how much space it takes");
                    System.out.println("retrive <msg> \t- retreves the message <msg> from the server");
                    System.out.println("delete <msg> \t- deletes <msg> from the server\n");
                } else {
                    System.out.println("unknown command to see the available");
                    System.out.println("commands use \"help\" or \"?\"\n");
                }


//                System.out.print("Enter message: ");
//
//                message = userEntry.readLine();
//                out.println(message); 		//Step 3.
//                System.out.println("");
//                do{
//                    response = in.readLine();
//                    System.out.println("SERVER> " + response);
//                }while (in.ready());
//                if (message.equals("QUIT")&&response.startsWith("+OK")) {
//                    link.close();
//                    System.exit(0);
//                }


            } while (!message.equals("***CLOSE***"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println(
                        "\n* Closing connection... *");
                link.close();				//Step 4.
            } catch (IOException e) {
                System.out.println("Unable to disconnect!");
                System.exit(1);
            }
        }
    }
}
