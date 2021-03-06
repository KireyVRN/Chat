package com.company;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.time.LocalDate;

public class Client extends JFrame implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String nickName;
    private JTextArea messagesWindow;
    private JTextArea inputArea;
    private JButton sendButton;
    private java.applet.AudioClip soundOfMessage;
    private java.applet.AudioClip soundOfExit;

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
                messagesWindow.append(messageFromServer + "\n");
                if (messageFromServer.contains("left the chat...")) {
                    soundOfExit.play();
                } else {
                    soundOfMessage.play();
                }
                System.out.println(messageFromServer);
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
        nameField.setFont(new Font("Dialog", Font.PLAIN, 25));
        nameField.setBackground(new Color(50, 104, 118));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);

        JDialog dialog = new JDialog(this, "Enter your name", true);
        dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 120);
        dialog.setLocationRelativeTo(null);
        dialog.add(nameField, BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.setBackground(Color.DARK_GRAY);
        ok.setForeground(Color.WHITE);
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

        soundOfMessage = Applet.newAudioClip(Client.class.getResource("send.aiff"));
        soundOfExit = Applet.newAudioClip(Client.class.getResource("sendFire.aiff"));
        getRootPane().setDefaultButton(sendButton);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initMessageWindow() {

        messagesWindow = new JTextArea();
        messagesWindow.setEditable(false);
        messagesWindow.setLineWrap(true);
        messagesWindow.setBackground(new Color(40, 40, 40));
        messagesWindow.setForeground(new Color(190, 233, 120));
        //RESERVE VARIANT     messagesWindow.setForeground(new Color(154, 201, 252));
        messagesWindow.setFont(new Font("Dialog", Font.PLAIN, 17));
        DefaultCaret caret = (DefaultCaret) messagesWindow.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    private void initInputArea() {

        inputArea = new JTextArea("Enter your message");
        inputArea.setBackground(new Color(80, 80, 80));
        inputArea.setFont(new Font("Dialog", Font.PLAIN, 19));
        inputArea.setForeground(Color.WHITE);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setRows(3);
        inputArea.setCaretColor(Color.WHITE);

        inputArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !inputArea.getText().trim().isEmpty()) {
                    try {
                        out.write(inputArea.getText() + "\n");
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
        sendButton.setBackground(new Color(106, 170, 60));
        //RESERVE VARIANT      sendButton.setBackground(new Color(63, 131, 160));
        sendButton.setFont(new Font("Dialog", Font.BOLD, 20));
        sendButton.setForeground(Color.WHITE);

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
}