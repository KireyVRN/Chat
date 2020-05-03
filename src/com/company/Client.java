package com.company;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.applet.Applet;
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
    private JTextArea inputArea;
    private JButton sendButton;

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
                notifyWithSound();
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
        new Client("localhost", 8888);
    }

    private void initializeFrame() {

        setSize(500, 800);
        setLocationRelativeTo(null);

        initMessageWindow();
        JScrollPane jScrollPane = new JScrollPane(messagesWindow);
        add(jScrollPane, BorderLayout.CENTER);

        initInputArea();
        JScrollPane messageFieldPane = new JScrollPane(inputArea);

        LocalDate localDate = LocalDate.now();
        JLabel date = new JLabel(localDate.getDayOfMonth() + " " + localDate.getMonth().name() + " " + localDate.getYear() + "   " + localDate.getDayOfWeek());
        add(date, BorderLayout.NORTH);
        date.setBackground(Color.BLACK);

        initButton();

        JPanel jPanel = new JPanel(new BorderLayout());
        add(jPanel, BorderLayout.SOUTH);
        jPanel.add(sendButton, BorderLayout.EAST);
        jPanel.add(messageFieldPane, BorderLayout.CENTER);


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

        getRootPane().setDefaultButton(sendButton);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initMessageWindow() {
        messagesWindow = new JTextArea();
        messagesWindow.setEditable(false);
        messagesWindow.setLineWrap(true);
        messagesWindow.setBackground(Color.DARK_GRAY);
        messagesWindow.setForeground(Color.WHITE);
        messagesWindow.setFont(new Font("Dialog", Font.PLAIN, 17));

        DefaultCaret caret = (DefaultCaret) messagesWindow.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    private void initInputArea() {
        inputArea = new JTextArea("Enter your message");
        inputArea.setBackground(Color.DARK_GRAY);
        inputArea.setFont(new Font("Dialog", Font.BOLD, 17));
        inputArea.setForeground(Color.ORANGE);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setRows(3);
        inputArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !inputArea.getText().trim().isEmpty()) {
                    try {
                        out.write(inputArea.getText()+ "\n");
                        out.flush();
                        if (inputArea.getText().trim().equalsIgnoreCase("stop")) {
                            closeClient();
                            Client.super.dispose();
                        }
                        inputArea.setText("");
                        inputArea.grabFocus();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    inputArea.setText("");
                }
            }
        });

        inputArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                inputArea.setText("");
            }
        });
    }

    private void initButton() {
        sendButton = new JButton("SEND");
        sendButton.setBackground(Color.ORANGE);
        sendButton.addActionListener(e -> {

            if (!inputArea.getText().trim().isEmpty()) {
                try {
                    out.write(inputArea.getText() + "\n");
                    out.flush();
                    if (inputArea.getText().trim().equalsIgnoreCase("stop")) {
                        closeClient();
                        this.dispose();
                    }
                    inputArea.setText("");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                inputArea.grabFocus();
            }
        });
    }

    private void notifyWithSound(){
        Applet.newAudioClip(Client.class.getResource("send.aiff")).play();
    }
}

