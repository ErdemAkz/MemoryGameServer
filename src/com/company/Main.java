package com.company;

public class Main {

    public static void main(String[] args) {
        try {
            Server srv=new Server();
            srv.readClients();
        }
        catch (Exception ex){
            System.out.println(""+ex);
        }
    }
}
