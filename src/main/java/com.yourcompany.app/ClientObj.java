package com.yourcompany.app;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
/**
 * Communicator between server, user, and game (Inherits Player)
 *
 * saves player data
 * here are some methods to interact with the user
 * */
public class ClientObj extends Player{
    Integer ClientID;
    String ClientName;
    Socket ClientSocket;
    boolean connected = false;
    boolean inRoom=false;
    boolean askingForResponse=false;
    String userInput;


    //Constructor
    public ClientObj(Integer ClientID, String ClientName, Socket ClientSocket){
        super(ClientID,ClientName,false,0,false);
        this.ClientID = ClientID;
        this.ClientName = ClientName;
        this.ClientSocket = ClientSocket;
    }
    /**
     * @return PlayerStatus
     */
    public boolean getPReady()
    {
        return playerReady;
    }
    /**
     * @return PlayerProtection
     */
    public boolean getPProtected()
    {
        return PProtected;
    }
    /**
     * @return PlayerTokens
     */
    public int getPTokens()
    {
        return PTokens;
    }
    /**
     * Assigns user name
     * */
     void setClientName(String ClientName){
        this.ClientName = ClientName;
    }
    /**
     * @return user name in string
     *
     * */
     public String getClientName(){
        return ClientName;
    }
    /**
     * @return user ID
     *
     * */
    public int getClientID(){
        return ClientID;
    }
    public String getInGame(){
        if(inRoom){
            return  "(In game)";
        }else{
            return"(Not in game)";
        }
    }
    /**
     * @return socket address
     *
     * */
    public Socket getClientSocket(){
        return ClientSocket;
    }
    /**
     * initializes & ask username, method called by server
     * */
    public void askForName() throws IOException, InterruptedException {
        Server.sendMsg("Enter Name: ",ClientObj.this);
        BufferedReader in= new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
        String inputLine = in.readLine();
        checkName(inputLine);
    }
    /**
     * Check if name exists
     * @param name = checks if name already exists, if it does, asks user for another name
     * */
    public void checkName(String name) throws IOException, InterruptedException {
        for(int i =0; i<Server.users.size(); i++){
            while(Server.users.get(i).getClientName().equals(name)) {
                Server.sendMsg("Name already exists, please choose another name: ", ClientObj.this);
                BufferedReader in= new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
                name= in.readLine();
            }
        }
        setClientName(name);
        connected = true;
        Server.sendMsg("Welcome " + ClientName + "!", ClientObj.this);
        TimeUnit.MILLISECONDS.sleep(10);
        Server.sendMsg("---------Entered Lobby--------", ClientObj.this);
        TimeUnit.MILLISECONDS.sleep(10);
        Server.sendMsg("\u001B[32mCommands\u001B[0m : CreateGame, JoinGame, ShowRules, ShowPlayers, msg"+"\u001B[1m/Name/Message\u001B[0m"+", bye", ClientObj.this);

        Server.sendMsgExclude(getClientName()+" Joined", ClientObj.this, "\u001B[31mServer\u001B[0m");
    }
    /**
     * Asks for user response (in game mostly)
     * @param withMsg false = only wait for answer
     * @return user response
     * */
    public String askForResponse(String msg,boolean withMsg) throws IOException, InterruptedException {
        if (withMsg){
        Server.sendMsg("Server: "+msg, this);
        Server.sendMsgExclude("Waiting for response from "+getClientName(), this, "\u001B[31mServer\u001B[0m");
        }
        askingForResponse=true;
        while(askingForResponse){
            TimeUnit.MILLISECONDS.sleep(20);
        }
        return userInput;


    }
    /**
     * @return time in Hour:Min:Sec as string
     *
     * */

    /**
     * Thread to receive message
     * bye command is here
     * */
    Thread receive = new Thread(new Runnable() {
        @Override
        public void run() {
            BufferedReader in = null;
            String inputLine;
            try {
                in = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                try {
                    inputLine = in.readLine();
                    userInput = inputLine;

                    if (assesInput(inputLine) ==false ) {

                        if (inputLine.equals("bye") && !Server.gameRunning) {
                            Server.sendMsg("Bye " + ClientName + "!", ClientObj.this);
                            Server.sendMsgExclude(ClientName + " Left the Room", ClientObj.this, "Server: ");
                            receive.interrupt();

                            Server.disconnectClient(ClientSocket);
                            in.close();
                        } else if (inputLine.equals("bye")) {
                            System.out.println("Server: Game is still running!");
                        } else {
                            System.out.println(getClientName() + ": " + inputLine);
                            Server.sendMsgExclude(inputLine, ClientObj.this, getClientName());
                            Server.sendMsg(getClientName()+": "+inputLine, ClientObj.this);

                        }
                    }

                } catch (Exception e) {
                    try {
                        Server.disconnectClient(ClientSocket);
                    } catch (Exception e1) {
                        Server.broadcast(getClientName()+" left",false);
                        break;
                    }
                }
            }
        }
    });
    /**
     * Checks if user input is a command
     *
     * */
    public Boolean assesInput(String msg) throws IOException, URISyntaxException, InterruptedException {
        if (askingForResponse) {
            askingForResponse=false;
            return true;
        }
        String[] tempMsg = msg.split("/",3);
        ClientObj tempTargetClient = null;
        boolean clientFound = false;
        if(tempMsg[0].equals("msg")){
            for(ClientObj usertemp : Server.users){
                if(usertemp.getClientName().equals(tempMsg[1])){
                    tempTargetClient = usertemp;
                    clientFound = true;
                }
            }
            if(tempTargetClient==null||!clientFound){
                Server.sendMsg("Invalid User",this);
                return true;
            }else{
                Server.sendMsg(getClientName()+" \u001B[31m(private)\u001B[0m: "+tempMsg[2], tempTargetClient);
                return true;}
        }

        switch(msg) {
            case "Ready":
                // code block
                if(inRoom){
                    Server.broadcast(getClientName() + " is ready!",true);
                    playerReady=true;}
                else{
                    Server.sendMsg("Cannot Ready: Please join a game first, or create one.",this);

                }
                return true;
            case "ShowPlayers":
                // code block
                Server.sendMsg(Server.askPlayers(), ClientObj.this);
                return true;
            case "ShowRules":
                Desktop.getDesktop().browse(new URI("https://alderac.com/wp-content/uploads/2017/11/Love-Letter-Premium_Rulebook.pdf"));
                return true;
            case "CreateGame":
                if(!Server.roomOpen){
                    Server.roomOpen=true;
                    inRoom=true;
                    Server.sendMsgExclude(getClientName() + " Created a game! Join by typing \u001B[32mJoinGame\u001B[0m",this,"\u001B[31mServer\u001B[0m");
                    Server.sendMsg("\u001B[32mGame Created!\u001B[0m, waiting for players to join",this);
                    Server.sendMsg("Commands: Ready, LeaveGame, ShowRules, ShowPlayers, MyTokens",this);

                }else{
                    Server.sendMsg("Cannot create new game. A game exists already. Join by typing \u001B[32mJoinGame\u001B[0m",this);
                }
                return true;
            case "JoinGame":
                if(!Server.roomOpen){
                    Server.sendMsg("No game available, create one by typing \u001B[32mCreateGame\u001B[0m",this);
                }else if(!inRoom&&!Server.gameRunning){
                    Server.sendMsg("\u001B[32mJoined a Game\u001B[0m",this);
                    TimeUnit.MILLISECONDS.sleep(5);
                    Server.sendMsg("Commands: Ready, LeaveGame, ShowRules, ShowPlayers",this);
                    inRoom=true;
                }else if(Server.gameRunning&&!inRoom) {
                    Server.sendMsg("Game is running, please wait!", this);
                }
                else{
                    Server.sendMsg("You're in a room already!",this);
                }

                return true;
            case "MyTokens":
                if(!inRoom){
                    Server.sendMsg("You are not in a game",this);
                }else{
                    Server.sendMsg("You have "+getPTokens()+" tokens",this);
                }
                return true;
            case "LeaveGame":
                if(inRoom){
                    inRoom=false;
                    Server.sendMsg("You left the game",this);
                    Server.sendMsgExclude(getClientName() + " left the game",this,"Server");
                    for(ClientObj usertemp : Server.users){
                        if(usertemp.inRoom){
                            break;
                        }
                        if (usertemp.inRoom==false && usertemp==Server.users.getLast()) {
                            Server.roomOpen=false;
                            Server.broadcast("Available Game is closed (No Player)",false);
                        }
                    }
                    
                }else{
                    Server.sendMsg("You are not in a game!",this);
                }
                return true;

            case "setmytokentolala":
                //cheats token to 6, for testing purposes
                PTokens=6;
                return true;

            default:
                // code block
                return false;
        }
    }

}
