package com.yourcompany.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Server Host, manages server and background tasks
 * @author hizkiaezrasutantio
 * @version 1.0
 *
 * */
public class Server {
    static Scanner input = new Scanner(System.in);

    static ArrayList<ClientObj> users = new ArrayList<>();
    static ArrayList<Integer> availID = new ArrayList<>();//Arrays.asList(1,2,3,4);
    static Socket clientSocket;
    public static boolean gameRunning = false ;
    static boolean roomOpen = false ;
    static ServerSocket serversocket;

    /**
     * Server Main Method creates new socket, adds available ID for user unique identifier
     * Then starts accepting clients once socket established
     * */
    public static void main(String[] args) throws IOException {

        System.out.println("Waiting for connection...");
        try{
            serversocket = new ServerSocket(1337);}
        catch(Exception e){
            System.out.println("Couldn't create server, port already in use");
            System.exit(0);
        }

        availID.add(1);
        availID.add(2);
        availID.add(3);
        availID.add(4);
        acceptClient.start();

        //Connection Listener, creates ClientObj and assign ID

        //Thread broadcast manually
        /**
         * Thread to read manual input in server window and broadcasts to all
         * */
    Thread send = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                String inputLine = input.nextLine();
                broadcast(inputLine, false);
                System.out.println("\u001B[31mServer:\u001B[0m "+inputLine);

            }
        }
    });send.start();

        /**
         * This thread checks if all players are ready
         * Once all players in the room ready, this starts game
         * */
        Thread checkReady = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                boolean allready = false;
                while(!allready){
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (users.size() >= 2) {
                        for(int i =0;i<users.size();){
                            if(!users.get(i).getPReady()) {
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                i = 0;
                            } else if (!users.get(i).inRoom) {
                                i=0;
                            } else{
                                i++;
                            }
                        }allready = true;
                    }
                }
                broadcast("--------------------------------------------", true);
                broadcast("Starting Game!", true);
                try {
                    System.out.println("------starting game--------");

                    Server.gameRunning = true;

                    InitializeGame.init();
                } catch (Exception e) {
                    System.out.println("Unable to start game: "+e);
                }
            }
            }
        });checkReady.start();
    }
        /**
         * Broadcast a message to whole client
         * @param msg = message to send
         * @param ingame true = broadcast only to users in gameroom
         * */
        protected static void broadcast(String msg,boolean ingame){
        String[] tempMsg = msg.split("///");
        if(!ingame){
            for (ClientObj user : users) {
                try {
                    if (user.connected) {
                        PrintWriter out = new PrintWriter(user.getClientSocket().getOutputStream(), true);
                        for (String s : tempMsg) {
                            out.println("\u001B[31mServer:\u001B[0m " + s);
                            TimeUnit.MILLISECONDS.sleep(10);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Broadcast failed to some to some: "+e);
                }
            }
        }else {
            for (ClientObj user : users) {
                if (user.inRoom && user.connected) {
                    try {

                        PrintWriter out = new PrintWriter(user.getClientSocket().getOutputStream(), true);
                        for (String s : tempMsg) {
                            out.println("\u001B[31mServer:\u001B[0m " + s);
                            TimeUnit.MILLISECONDS.sleep(10);
                        }
                    } catch (Exception e) {
                        System.out.println("Broadcast failed to some to some: "+e);
                    }
                }
            }
        }
        System.out.println("(BROADCAST)\u001B[31mServer:\u001B[0m " + msg);
        }
    /**
     * Send a message to one client
     * @param msg = message to send
     * @param Client = target client
     * */
    protected static void sendMsg(String msg, ClientObj Client) throws IOException, InterruptedException {
        PrintWriter out = new PrintWriter(Client.ClientSocket.getOutputStream(), true);
        String[] tempMsg = msg.split("///");
        for (String s : tempMsg) {
            out.println(s);
            TimeUnit.MILLISECONDS.sleep(10);
            System.out.println("Server to \u001B[31m" + Client.getClientName() + "\u001B[0m: " + msg);
        }
    }
    /**
     * Send a message to one everyone, except one client
     * @param msg = message to send
     * @param Client = client to exclude
     * @param Sender = sender of the message (User, Server, or Game)
     * */
    protected static void sendMsgExclude(String msg, ClientObj Client, String Sender) throws IOException, InterruptedException {
        System.out.println("Server except to \u001B[31m"+Client.getClientName() +"\u001B[0m: " + msg);
        for (ClientObj user : users) {
            if (user.getClientSocket() != Client.ClientSocket && user.connected) {
                PrintWriter out = new PrintWriter(user.getClientSocket().getOutputStream(), true);
                out.println(Sender + ": " + msg);
                TimeUnit.MILLISECONDS.sleep(10);
            }
        }
    }
    /**
     * Disconnect client when player enters "bye"
     * @param socket = which socket to disconnect
     * */
    protected static void disconnectClient(Socket socket) throws IOException {
        ClientObj toRemove = null;
        for (ClientObj user : users) {
            if (user.getClientSocket() == socket) {
                toRemove = user;
            }
        }
        availID.add(toRemove.getClientID());
        System.out.println(availID);

        users.remove(toRemove);

        toRemove.getClientSocket().close();
        System.out.println("Remaining Users: "+users.size());
        if (users.size() == 0) {
            System.out.println("Waiting for connection...");
        }
        for (ClientObj user : users) {
            System.out.print(user.getClientName()+", ");
        }

    }

    /**
     * Asks for player list
     * @return in string
     * */
     protected static String askPlayers(){
        String tempPlayers = "";
        for (ClientObj user : users) {
            tempPlayers += "- " + user.getClientName() +" | ID: " + user.getClientID()+" "+user.getInGame()+"///";
        }
        return tempPlayers;
    }

    /**
     * Thread to accept connections, only if game isn't running
     * */
    static Thread acceptClient = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {
                if (!availID.isEmpty()&&!gameRunning) {

                    try {
                        clientSocket = serversocket.accept();
                        if(gameRunning){
                            clientSocket.close();

                        }else{
                        int randomID = availID.getFirst();
                        ClientObj tempClient = new ClientObj(randomID, "", clientSocket);
                        users.add(tempClient);
                        tempClient.askForName();
                        availID.removeFirst();
                        System.out.println(availID);
                        System.out.println("Accepted connection from " + clientSocket);
                        System.out.println("User Count: " + users.size());
                        System.out.println(askPlayers());
                        tempClient.receive.start();}
                    } catch (Exception e) {
                        System.out.println("User tried to connect but gave up: " + e);
                    }

                }
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    });
    //Connection Listener end
    public static boolean getGameRunning(){
        return gameRunning;
    }
}
