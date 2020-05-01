package com.company;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class Server {

    static ArrayList<ServerThread> serverList = new ArrayList<>();
    static MessageHistory messageHistory;

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8888);
        messageHistory = new MessageHistory();
        System.out.println("Server started");

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    serverList.add(new ServerThread(socket));
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            serverSocket.close();
        }
    }
}

class ServerThread extends Thread {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String nickName;

    public ServerThread(Socket socket) throws IOException {

        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        nickName = in.readLine();
        out.write(" Hello " + nickName + "!" + "\n" + "\n");
        out.flush();
        Server.messageHistory.printMessageHistory(out);
        start();
        System.out.println("Client " + nickName + " connected");
    }

    @Override
    public void run() {

        try {
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                String messageTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
                if (clientMessage.trim().equalsIgnoreCase("stop")) {
                    Server.messageHistory.addMessage(nickName + " left the chat..." + "\n");
                    for (ServerThread serverThread : Server.serverList) {
                        if (this != serverThread) {
                            serverThread.out.write(" " + nickName + " left the chat..." + "\n" + " " + "\n");
                            serverThread.out.flush();
                        }
                    }
                    this.closeServerThread();
                    break;
                } else {
                    System.out.println(messageTime + " " + nickName + ": " + clientMessage);
                    Server.messageHistory.addMessage(" " + messageTime + " " + nickName + " :\n" + " \"" + clientMessage + "\"" + "\n");
                    for (ServerThread serverThread : Server.serverList) {
                        serverThread.out.write(" " + messageTime + " " + nickName + " :\n" + " \"" + clientMessage + "\"" + "\n" + "\n");
                        serverThread.out.flush();
                    }
                }
            }
        } catch (Exception e) {
            this.closeServerThread();
        }
    }

    private void closeServerThread() {
        try {
            socket.close();
            in.close();
            out.close();
            this.interrupt();
            System.out.println("Clent " + nickName + " disconnected");
            Server.serverList.remove(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MessageHistory {

    private LinkedList<String> history = new LinkedList<>();

    public void addMessage(String message) {
        if (history.size() >= 10) {
            history.removeFirst();
            history.add(message);
        } else {
            history.add(message);
        }
    }

    public void printMessageHistory(BufferedWriter writer) {
        if (history.size() > 0) {
            try {
                for (String string : history) {
                    writer.write(string + "\n");
                    writer.flush();
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}


