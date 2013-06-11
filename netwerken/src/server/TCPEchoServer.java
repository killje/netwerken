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

    public static String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return "";
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
                System.out.println(message);
                StringTokenizer st = new StringTokenizer(message);
                String start = st.nextToken();
                numMessages++;
                if (start.equals("STAT")) {
                    if (state.equals("TRANSACTION")) {
                        File[] mailMessages = currentDirectory.listFiles();
                        int messages = 0;
                        long size = new Long(0);
                        for (int i = 0; i < mailMessages.length; i++) {
                            if (getFileExtension(mailMessages[i].getName()).equals("txt")) {
                                size += mailMessages[i].length();
                                messages++;
                            }
                        }
                        out.println("+OK " + messages + " " + size);

                    }
                } else if (start.equals("USER")) {
                    if (st.countTokens() == 1) {
                        if (!state.equals("AUTHORIZATION") && !state.equals("TRANSACTION")) {
                        }
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
                            pass = pass.substring(1, pass.length());//to cut off the first space
                            if (pass.equals(getPassword(currentDirectory.getAbsolutePath()))) {
                                out.println("+OK maildrop locked and ready");
                                state = "TRANSACTION";
                            } else {
                                System.out.println(getPassword(currentDirectory.getAbsolutePath()));
                                System.out.println(pass);
                                out.println("-ERR Wrong password");
                            }
                        } else {
                            out.println("-ERR uncorrect usage of PASS, use PASS <string>");
                        }
                    } else {
                        out.println("-ERR not in AUTHORIZATION state, use first USER name");
                    }
                } else if (start.equals("LIST")) {
                    if (state.equals("TRANSACTION")) {
                        if (st.countTokens() == 1) {
                            int messageNumber = Integer.parseInt(st.nextToken());
                            File[] documents = currentDirectory.listFiles();
                            if (documents != null) {
                                int messages = 0;
                                for (int i = 0; i < documents.length; i++) {
                                    if (getFileExtension(documents[i].getName()).equals("txt")) {
                                        messages++;
                                    }
                                }
                                File[] mailMessages = new File[messages];
                                messages = 0;
                                for (int i = 0; i < documents.length; i++) {
                                    if (getFileExtension(documents[i].getName()).equals("txt")) {
                                        mailMessages[messages] = new File(documents[i].toURI());
                                        messages++;
                                    }
                                }
                                if (messageNumber <= messages && messageNumber > 0) {
                                    out.println("+OK " + messageNumber + " " + mailMessages[messages - 1].length());
                                } else {
                                    out.println("-ERR no such message, only " + messages + " messages in maildrop");
                                }
                            } else {
                                out.println("-ERR there are no mails");
                            }
                        } else if (st.countTokens() == 0) {
                            File[] mailMessages = currentDirectory.listFiles();
                            long size = new Long(0);
                            int messages = 0;
                            if (mailMessages != null) {
                                for (int i = 0; i < mailMessages.length; i++) {
                                    if (getFileExtension(mailMessages[i].getName()).equals("txt")) {
                                        size += mailMessages[i].length();
                                        messages++;
                                    }
                                }
                                String messageOut;
                                if (messages != 0) {
                                    messageOut = "+OK " + messages + " messages (" + size + " octets)";
                                    messages = 0;
                                    for (int i = 0; i < mailMessages.length; i++) {
                                        if (getFileExtension(mailMessages[i].getName()).equals("txt")) {
                                            messages++;
                                            messageOut += "\n" + messages + " " + mailMessages[i].length();
                                        }
                                    }
                                    messageOut += "\n.";
                                    out.println(messageOut);
                                }
                                out.println("-ERR there are no mails");
                            } else {
                                out.println("-ERR there are no mails");
                            }
                        } else {
                            out.println("-ERR uncorrect usage of LIST, use LIST [msg]");
                        }
                    } else {
                        out.println("-ERR not in TRANSACTION state");
                    }
                } else if (start.equals("RETR")) {
                    if (state.equals("TRANSACTION")) {
                        if (st.countTokens() == 1) {
                            int messageNumber = Integer.parseInt(st.nextToken());
                            File[] documents = currentDirectory.listFiles();
                            int messages = 0;
                            for (int i = 0; i < documents.length; i++) {
                                if (getFileExtension(documents[i].getName()).equals("txt")) {
                                    messages++;
                                }
                            }
                            File[] mailMessages = new File[messages];
                            messages = 0;
                            for (int i = 0; i < documents.length; i++) {
                                if (getFileExtension(documents[i].getName()).equals("txt")) {
                                    mailMessages[messages] = new File(documents[i].toURI());
                                    messages++;
                                }
                            }
                            if (messageNumber <= mailMessages.length && messageNumber > 0) {
                                String messageOut;
                                messageOut = "+OK " + messageNumber + " " + mailMessages[messageNumber - 1].length();
                                File input = new File(currentDirectory.getAbsolutePath() + "\\" + mailMessages[messageNumber - 1].getName());
                                System.out.println(read(input));
                                messageOut += "\n" + read(input);
                                messageOut += "\n.";
                                out.println(messageOut);
                            } else {
                                out.println("-ERR no such message, only " + mailMessages.length + " messages in maildrop");
                            }
                        } else {
                            out.println("-ERR uncorrect usage of RETR, use RETR msg");
                        }
                    } else {
                        out.println("-ERR not in TRANSACTION state");
                    }
                } else if (start.equals("DELE")) {
                    if (state.equals("TRANSACTION")) {
                        if (st.countTokens() == 1) {
                            int messageNumber = Integer.parseInt(st.nextToken());
                            File[] documents = currentDirectory.listFiles();
                            int messages = 0;
                            for (int i = 0; i < documents.length; i++) {
                                if (getFileExtension(documents[i].getName()).equals("txt")) {
                                    messages++;
                                }
                            }
                            File[] mailMessages = new File[messages];
                            messages = 0;
                            for (int i = 0; i < documents.length; i++) {
                                if (getFileExtension(documents[i].getName()).equals("txt")) {
                                    mailMessages[messages] = new File(documents[i].toURI());
                                    messages++;
                                }
                            }
                            messages = 0;
                            if (mailMessages.length >= messageNumber && messageNumber > 0) {
                                for (int i = 0; i < mailMessages.length; i++) {
                                    if (getFileExtension(mailMessages[i].getName()).equals("txt")) {
                                        messages++;
                                        if (messages == messageNumber) {
                                            String newFile = mailMessages[i].getName();
                                            newFile = replaceLastOcurense(newFile, "txt", "dele");
                                            System.out.println(currentDirectory.getAbsolutePath() + "\\" + newFile);
                                            mailMessages[i].renameTo(new File(currentDirectory.getAbsolutePath() + "\\" + newFile));
                                        }
                                    }
                                }
                                out.println("+OK message " + messageNumber + " deleted");
                            } else {
                                out.println("-ERR could not find mail");

                            }
                        } else {
                            out.println("-ERR uncorrect usage of DELE, use DELE msg");
                        }
                    } else {
                        out.println("-ERR not in TRANSACTION state");
                    }
                } else if (start.equals("QUIT")) {
                    out.println("+OK");
                    return;
                } else {
                    out.println("Message " + numMessages
                            + ": " + message);     //Step 4.
                }
                message = in.readLine();
            }
            out.println("Closing Server");
            System.exit(0);
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

    private static String read(File file) {
        String inline;
        String Output = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            if ((inline = br.readLine()) != null) {
                Output = inline;
                while ((inline = br.readLine()) != null) {
                    Output += "\n" + inline;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("a error has occured");
        }
        return Output;
    }

    private static String replaceLastOcurense(String fileName, String s, String q) {
        int i = fileName.lastIndexOf(s);
        if (i > 0) {
            return fileName.substring(0, i) + q;
        }
        return fileName;
    }

    private static String getPassword(String file) {
        File passFile = new File(file + "\\pass.pass");
        String inline;
        String Output = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(passFile));
            if ((inline = br.readLine()) != null) {
                Output = inline;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("a error has occured");
        }
        return Output;
    }
}
