package com.company;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Server {

    private static ArrayList<ServerThread> serverList = new ArrayList<>();
    private static ArrayList<String> messageHistory = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            System.out.println("Server started");
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    serverList.add(new ServerThread(socket));
                } catch (IOException e) {
                    socket.close();
                }
            }
        }
    }

    static class ServerThread extends Thread {

        private Socket socket;
        private BufferedReader in;
        private BufferedWriter out;
        private String nickName;

        private ServerThread(Socket socket) throws IOException {

            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            nickName = in.readLine();
            for (ServerThread serverThread : serverList) {
                if (serverThread != this) {
                    serverThread.out.write(" " + nickName + " joined the chat" + "\n");
                    serverThread.out.flush();
                }
            }
            out.write(" Hello " + nickName + "!" + "\n" + "\n");
            out.flush();
            printMessageHistory(out);
            start();
            addMessageHistory(" " + nickName + " joined the chat" + "\n");
            System.out.println("Client " + nickName + " connected");
        }

        @Override
        public void run() {

            try {
                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {

                    String messageTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                    if (clientMessage.trim().equalsIgnoreCase("stop")) {
                        addMessageHistory(" " + nickName + " left the chat..." + "\n");
                        for (ServerThread serverThread : serverList) {
                            if (this != serverThread) {
                                serverThread.out.write(" " + nickName + " left the chat..." + "\n" + " " + "\n");
                                serverThread.out.flush();
                            }
                        }
                        this.closeServerThread();
                        break;
                    } else {
                        System.out.println(messageTime + " " + nickName + ": " + clientMessage);
                        addMessageHistory(" (" + messageTime + ") " + nickName + " :\n" + " \"" + clientMessage + "\"" + "\n");
                        for (ServerThread serverThread : serverList) {
                            serverThread.out.write(" (" + messageTime + ") " + nickName + " :\n" + " \"" + clientMessage + "\"" + "\n" + "\n");
                            serverThread.out.flush();
                        }
                    }
                }
            } catch (Exception e) {
                this.closeServerThread();
                e.printStackTrace();
            }
        }

        private void closeServerThread() {
            try {
                socket.close();
                in.close();
                out.close();
                this.interrupt();
                System.out.println("Clent " + nickName + " disconnected");
                serverList.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addMessageHistory(String message) {
        if (messageHistory.size() >= 10) {
            messageHistory.add(message);
            messageHistory.remove(0);

        } else {
            messageHistory.add(message);
        }
    }

    private static void printMessageHistory(BufferedWriter writer) {
        if (messageHistory.size() > 0) {
            try {
                for (String string : messageHistory) {
                    writer.write(string + "\n");
                    writer.flush();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}


