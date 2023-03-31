package com.company;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Console;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.Set;
import java.util.Iterator;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Enumeration;
import java.text.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.lang.Thread;
import java.util.Hashtable;
import java.util.Random;




/**
 */
public class Server {
    Selector selector;
    ServerSocketChannel SS;
    SelectionKey selectKy;
    ArrayList<SocketChannel> players = new ArrayList<>();

    Hashtable <SocketChannel,Client>  clients=new Hashtable(10);
    int[] kartlar = new int[64];
    int birinciKart;
    int ikinciKart;
    public int order=0;

    public Server() throws IOException {
        selector = Selector.open();
        System.out.println("Selector is open for making connection: " + selector.isOpen());
        SS = ServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("192.168.1.150", 8080);
        SS.bind(hostAddress);
        SS.configureBlocking(false);
        int ops = SS.validOps();
        selectKy = SS.register(selector, ops, null);
    }


    public String  random(int N){
        String ret="";
        ArrayList  A= new ArrayList(N);

        for (int i=0; i<N/2;i++)
        {
            A.add(i*2,i+1);
            A.add(i*2+1,i+1);
        }

        Random rand = new Random();
        int kalan=N;
        for (int i=0; i<N;i++)
        {
            int x=rand.nextInt(kalan);
            kalan--;
            kartlar[i]=(int)A.get(x);
            if (ret.length()==0) ret=""+ (A.get(x));
            else ret = ret +" "+ A.get(x);
            A.remove(x);
        }
        return ret;
    }

    public void processCommand(Client client,String msg) {
        int n=msg.indexOf(' ');
        String cmd="";
        if (n>0) {
            cmd=msg.substring(0,n);
        }
        else cmd=msg;

        System.out.println("komut "+msg);
        if (cmd.equals("/name")) {
            client.name=msg.substring(n+1);
            clients.replace(client.sc, client);
            System.out.println("donen: "+getPlayers());
            sendAllClients("/listPlayers "+getPlayers());
        }
        else if (cmd.equals("/newgame")) {
            clients.replace(client.sc, client);
            sendAllClients("/newgame "+random(64));
            fillPlayers();
            order=0;
            Client cl = clients.get(players.get(order));

            sendAllClients("/listPoints "+getPoints());
            sendAllClients("/order "+cl.name);

        }
        else if(cmd.equals("/t1")){
            sendAllClients(client.sc,msg);
            birinciKart = Integer.parseInt(msg.substring(n+1));
        }
        else if(cmd.equals("/t2")){
            sendAllClients(client.sc,msg);
            ikinciKart = Integer.parseInt(msg.substring(n+1));
            if (kartlar[birinciKart]!=kartlar[ikinciKart]){
                orderSwitch();
            }
            else{
                client.points++;
                clients.put(client.sc,client);
                sendAllClients("/listPoints "+getPoints());
                sendAllClients("/order "+client.name);
            }
        }
        else if (cmd.equals("/exit")){
            try {
                client.sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientCleanUP(client.sc);
        }
        else
            sendAllClients(client.sc,msg);


    }
    public String getPoints(){
        String oyuncuPuanlari="";

        for (int i=0;i<players.size();i++){
            SocketChannel key = players.get(i);
            Client client1=clients.get(key);
            System.out.println(client1.name);
            if (i==0)
                oyuncuPuanlari=oyuncuPuanlari+""+client1.name+":"+client1.points;
            else
                oyuncuPuanlari=oyuncuPuanlari+" "+client1.name+":"+client1.points;
        }
        System.out.println(oyuncuPuanlari);
        return oyuncuPuanlari;
    }


    public String getPlayers(){
        String oyuncuIsimleri="";
        System.out.println("oyuncu sayisi: "+clients.size());
        Enumeration<SocketChannel> enumeration = clients.keys();
        CharsetEncoder enc = Charset.forName("UTF8").newEncoder();
        int i=0;
        while(enumeration.hasMoreElements()) {
            SocketChannel key = enumeration.nextElement();

            Client client1=clients.get(key);
            System.out.println(client1.name);
            if (i==0)
                oyuncuIsimleri=oyuncuIsimleri+""+client1.name;
            else
                oyuncuIsimleri=oyuncuIsimleri+" "+client1.name;
            i++;


        }
        System.out.println(oyuncuIsimleri);
        return oyuncuIsimleri;
    }

    public void orderSwitch(){

        order++;
        if (order>=players.size())
            order=0;
        try {
            Client cl = clients.get(players.get(order));
            if (cl != null) sendAllClients("/order "+cl.name);
        }
        catch (Exception ex)
        {}


    }


    public void processMessage(Client client,String msg ) {
        if (msg.length()<1) return;

        if (msg.charAt(0)=='/') {
            processCommand(client, msg);
        }
        else {
            sendAllClients(client.sc,"<"+client.name+"> "+msg);
        }


    }
    public void fillPlayers() {
        players = new ArrayList<SocketChannel>();
        Enumeration<SocketChannel> enumeration = clients.keys();
        CharsetEncoder enc = Charset.forName("UTF8").newEncoder();

        while(enumeration.hasMoreElements()) {
            SocketChannel key = enumeration.nextElement();
            players.add(key);

        }
    }

    public void sendAllClients(SocketChannel gonderen,String msg) {

        Enumeration<SocketChannel> enumeration = clients.keys();
        CharsetEncoder enc = Charset.forName("UTF8").newEncoder();

        while(enumeration.hasMoreElements()) {
            SocketChannel key = enumeration.nextElement();

            if (key != gonderen) {
                try {
                    key.write(enc.encode(CharBuffer.wrap(msg+"\r\n")));

                }
                catch (Exception ex)
                {
                    System.out.println("Write socket failed! "+ex);
                    clientCleanUP(key);
                }

            }
        }
    }

    public void sendAllClients(String msg) {

        Enumeration<SocketChannel> enumeration = clients.keys();
        CharsetEncoder enc = Charset.forName("UTF8").newEncoder();

        while(enumeration.hasMoreElements()) {

            SocketChannel key = enumeration.nextElement();

            try {
                int n=key.write(enc.encode(CharBuffer.wrap(msg+"\r\n")));
                System.out.println(n+": "+msg +"  "+key);
            }
            catch (Exception ex)
            {
                System.out.println("Write socket failed! "+ex);
                clientCleanUP(key);

            }

        }



    }

    public void clientCleanUP(SocketChannel ch) {
        System.out.println("cleanup " + ch);
        for (int i=0;i<players.size();i++){
            if (players.get(i)==ch) {
                players.remove(i);
                if (order==i)
                    orderSwitch();
                break;
            }
        }
        clients.remove(ch);
        try {
            ch.close();
        }
        catch (Exception ex2) {};
    }

    public void readClients() {
        Iterator itr;
        int noOfKeys;
        for (;;) {

            try {
                // System.out.println("Waiting for the select operation...");
                noOfKeys = selector.select();
                // System.out.println("The Number of selected keys are: " + noOfKeys);

                Set selectedKeys = selector.selectedKeys();
                itr = selectedKeys.iterator();

            }
            catch (Exception ex) {
                continue;
            }
            while (itr.hasNext()) {
                SelectionKey ky = (SelectionKey) itr.next();
                if (ky.isAcceptable()) {
                    // The new client connection is accepted
                    try {
                        SocketChannel cl = SS.accept();
                        cl.configureBlocking(false);
                        // The new connection is added to a selector
                        cl.register(selector, SelectionKey.OP_READ);
                        System.out.println("The new connection is accepted from the client: " + cl);
                        Client client=new Client();
                        client.sc=cl;
                        clients.put(cl, client);
                        System.out.println(clients.size()+" oyuncu.");
                    }
                    catch (Exception ex) {
                        System.out.println(""+ex);
                        continue;
                    }

                }
                else if (ky.isReadable()) {
                    // Data is read from the client
                    SocketChannel cl = (SocketChannel) ky.channel();
                    try {
                        Client gonderen=clients.get(cl);


                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        int n=cl.read(buffer);
                        if (n==-1)  {
                            cl.close();
                            continue;
                        }
                        String s = new String(buffer.array()).substring(0,n);
                        int start=0;

                        for (int i=0; i < s.length();i++) {
                            char ch=s.charAt(i);
                            if (ch == 13  || ch ==10 ) // \r\n  şeklinde geliyor
                            {
                                Client p=clients.get(cl);
                                gonderen.buffer=gonderen.buffer+""+s.substring(start,i);
                                processMessage(p,gonderen.buffer );
                                System.out.println(cl+" "+gonderen.buffer);
                                if (ch == 13)  i++;
                                start = i + 1;

                                gonderen.buffer="";
                            }

                        }
                        if(start<s.length()){
                            gonderen.buffer=s.substring(start);
                            System.out.println(gonderen.buffer);
                        }


                    }
                    catch (Exception ex) {
                        System.out.println(cl+ " read işleminde hata aldı!");
                        clientCleanUP(cl);

                    }
                }
                else {
                    System.out.println("burdayim: "+ky);
                }

                itr.remove();
            } // end of while loop
        } // end of for loop


    }




    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            Server srv=new Server();
            srv.readClients();
        }
        catch (Exception ex){
            System.out.println(""+ex);
        }


    }

}
