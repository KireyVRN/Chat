package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Client extends JFrame implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String nickName;
    private JTextArea messagesWindow;

    private Client(String ip, int port) {

        super("SUPER CHAT");
        initializeFrame();

        try {
            socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.write(nickName + "\n");
            out.flush();
            new Thread(this).start();
        } catch (IOException e) {
            closeClient();
        }
    }

    @Override
    public void run() {

        String messageFromServer;
        try {
            while ((messageFromServer = in.readLine()) != null) {
                System.out.println(messageFromServer);
                messagesWindow.append(messageFromServer + "\n");
            }
        } catch (Exception e) {
            System.out.println("FUCK! FUCK! FUCK!");
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
        new Client("128.70.38.213", 8888);
    }

    private void initializeFrame() {

        setSize(500, 800);
        setLocationRelativeTo(null);

        messagesWindow = new JTextArea();
        messagesWindow.setEditable(false);
        messagesWindow.setLineWrap(true);
        messagesWindow.setBackground(Color.DARK_GRAY);
        messagesWindow.setForeground(Color.WHITE);
        messagesWindow.setFont(new Font("Dialog", Font.PLAIN, 17));

        JTextField messageField = new JTextField("Enter your message");
        messageField.setBackground(Color.DARK_GRAY);
        messageField.setFont(new Font("Dialog", Font.BOLD, 17));
        messageField.setForeground(Color.ORANGE);
        messageField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                messageField.setText("");
            }
        });

        JScrollPane jScrollPane = new JScrollPane(messagesWindow);
        add(jScrollPane, BorderLayout.CENTER);

        LocalDate localDate = LocalDate.now();
        JLabel date = new JLabel(localDate.getDayOfMonth() + " " + localDate.getMonth().name() + " " + localDate.getYear() + "   " + localDate.getDayOfWeek());
        add(date, BorderLayout.NORTH);
        date.setBackground(Color.BLACK);

        JButton sendMessageButton = new JButton("SEND");
        sendMessageButton.setBackground(Color.ORANGE);
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
                    e1.printStackTrace();
                }
                messageField.grabFocus();
            }
        });

        JPanel jPanel = new JPanel(new BorderLayout());
        add(jPanel, BorderLayout.SOUTH);
        jPanel.add(sendMessageButton, BorderLayout.EAST);
        jPanel.add(messageField, BorderLayout.CENTER);


        JTextField nameField = new JTextField();
        nameField.setFont(new Font("Dialog", Font.BOLD, 30));
        nameField.setBackground(Color.DARK_GRAY);
        nameField.setForeground(Color.ORANGE);

        JDialog dialog = new JDialog(this, "Enter your name", true);
        dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 120);
        dialog.setLocationRelativeTo(null);
        dialog.add(nameField, BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
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
        });
        dialog.add(ok, BorderLayout.SOUTH);
        dialog.setVisible(true);

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

        getRootPane().setDefaultButton(sendMessageButton);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}

