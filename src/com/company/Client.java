package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Client extends JFrame {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String nickName;
    private JTextArea messagesWindow;
    private JTextField messageField;

    public Client(String ip, int port) {

        super("SUPER CHAT");

        initializeFrame();

        try {
            this.socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.write(nickName + "\n");
            out.flush();
            new ReadMessages().start();
        } catch (IOException e) {
            closeClient();
        }
    }

    private class ReadMessages extends Thread {

        @Override
        public void run() {

            String messageFromServer;
            try {
                while ((messageFromServer = in.readLine()) != null) {
                    System.out.println(messageFromServer);
                    messagesWindow.append(messageFromServer + "\n");
                }
            } catch (Exception e) {

            }
        }
    }

    private void closeClient() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client("localhost", 8888);
    }

    private void initializeFrame() {

        setSize(600, 800);
        setLocationRelativeTo(null);
        messagesWindow = new JTextArea();
        messagesWindow.setEditable(false);
        messagesWindow.setLineWrap(true);
        messagesWindow.setBackground(Color.LIGHT_GRAY);
        JScrollPane jScrollPane = new JScrollPane(messagesWindow);
        jScrollPane.setBackground(Color.GRAY);
        add(jScrollPane, BorderLayout.CENTER);
        JLabel date = new JLabel(new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(new Date()));
        add(date, BorderLayout.NORTH);
        JButton sendMessageButton = new JButton("SEND");
        sendMessageButton.setBackground(Color.ORANGE);
        JPanel jPanel = new JPanel(new BorderLayout());
        add(jPanel, BorderLayout.SOUTH);
        jPanel.add(sendMessageButton, BorderLayout.EAST);
        messageField = new JTextField("Enter your message");
        jPanel.add(messageField, BorderLayout.CENTER);
        JDialog dialog = new JDialog(this, "Enter your name", true);
        dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(null);
        JTextField nameField = new JTextField();
        dialog.add(nameField, BorderLayout.CENTER);
        JButton ok = new JButton("OK");

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!nameField.getText().isEmpty() && nameField.getText().trim().length() != 0) {
                    nickName = nameField.getText();
                    dispose();
                } else {
                    nameField.setText("Please, enter your name");
                    nameField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusGained(FocusEvent e) {
                            nameField.setText("");
                        }
                    });
                }
            }
        });

        dialog.add(ok, BorderLayout.SOUTH);
        dialog.setVisible(true);

        messageField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                messageField.setText("");
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    out.write("stop");
                    out.flush();
                    closeClient();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });

        sendMessageButton.addActionListener(e -> {

            if (!messageField.getText().trim().isEmpty()) {
                try {
                    out.write(messageField.getText() + "\n");
                    out.flush();
                    if (messageField.getText().trim().equalsIgnoreCase("stop")) {
                        closeClient();
                        this.dispose();
                    }
                    messageField.setText("");
                } catch (Exception e1) {
                    // e1.printStackTrace();
                }
                messageField.grabFocus();
            }
        });

        this.getRootPane().setDefaultButton(sendMessageButton);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}

