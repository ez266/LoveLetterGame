package com.yourcompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

/**
 * User client
 *
 * */
public class Client {
    static  Scanner input = new Scanner(System.in);
    static Socket clientSocket;
    /**
     * Establishes connection und runs essential processes
     *
     * */
    public static void main(String[] args) throws IOException, InterruptedException {
        if(Server.getGameRunning()){
            System.out.println("Game is already running, please try again later" );
            System.exit(0);
        }
        try {
            System.out.println("Waiting for server...");
            clientSocket = new Socket("127.0.0.1", 1337);
        } catch (Exception e) {
            System.out.println("Server isn't available now, please try again later");
            System.exit(0);
        }

        receive.start();
        send.start();

    }
    /**
     * Thread to send message
     *
     * */
    static Thread send = new Thread(new Runnable() {
    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
            while (true) {
                String inputLine = input.next();
                out.println(inputLine);
                for (int i = 0; i<Server.users.size(); i++){
                if(Server.users.get(i).getClientSocket()==clientSocket){
                    Server.users.get(i).receive.start();
                }}
            }
        } catch (Exception e) {
            System.out.println("unable to send message");
        }
    }
    });
    /**
     * Thread to receive message
     *
     * */
    static Thread receive = new Thread(new Runnable() {
        @Override
        public void run() {
            BufferedReader in = null;

            while(true) {
                try {
                    in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    String inputLine = in.readLine();
                    if(inputLine==null) {
                        System.out.println("Connection closed");

                        System.exit(0);

                    }
                    System.out.println(inputLine);

                } catch (IOException e) {
                    System.out.println("Connection closed");
                    System.exit(0);
                }


            }
        }
    });




}
