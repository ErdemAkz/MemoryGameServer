package com.company;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */




import java.net.*;
import java.io.*;
import java.awt.*;
import java.lang.Thread;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import java.awt.event.*;
/*

 */
public class TxtClient extends javax.swing.JPanel implements Runnable{
    public String name="";
    public String buffer="";

    JTextArea DisplayText;
    JTextField InputText;
    Socket socket;
    boolean connected=false;
    public TxtClient() {

        DisplayText = new JTextArea(10, 10);
        InputText = new JTextField(10);
        InputText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String msg=InputText.getText();
                InputText.setText("");
                DisplayText.append(msg+"\r\n");
                sendMessage(msg);
            }
        });
        setLayout(new java.awt.BorderLayout());
        DisplayText.setEditable(false);
        DisplayText.setSize(new Dimension(200,100));
        add(new JScrollPane(DisplayText), java.awt.BorderLayout.CENTER);
        add(InputText,java.awt.BorderLayout.PAGE_END);



    }

    public void connectTo() {
        try {
            socket = new Socket("192.168.1.150", 8080);
            connected=true;
            DisplayText.append("* Connected!\r\n");
        }
        catch (IOException ex) {
            DisplayText.append("* can not connect to server!\r\n");

        }
    }

    public void sendMessage(String msg) {

        try {
            OutputStream out = socket.getOutputStream();
            BufferedWriter bufOut = new BufferedWriter( new OutputStreamWriter( out ) );
            bufOut.write( msg );
            bufOut.newLine();
            bufOut.flush();
        }
        catch (Exception ex) {
            connected=false;
            DisplayText.append("* can not send message!\r\n");
            connectTo();
        }
    }

    public static void main(String[] args) {
        JFrame fr2= new JFrame();
        fr2.setBounds(100,100,400,300);
        fr2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TxtClient p2=new TxtClient();

        fr2.add(p2);
        fr2.setVisible(true);

        p2.connectTo();
        Thread t1 = new Thread(p2);
        t1.start();


    }
    @Override
    public void run() {
        byte [] buffer=new byte[512];
        int n=-1;
        System.out.println("Run baby!");
        while (true) {
            try {

                InputStream in = socket.getInputStream();
                BufferedReader bufin = new BufferedReader( new InputStreamReader( in ) );
                String msg=bufin.readLine();
                DisplayText.append(msg+"\r\n");
            }
            catch (Exception ex) {
                connected=false;
                DisplayText.append("* "+n+" can not read from socket!\r\n");
                //        connect();
            }

        }
    }


}