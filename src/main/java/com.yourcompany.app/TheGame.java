package com.yourcompany.app;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.yourcompany.app.Server.broadcast;
import static com.yourcompany.app.Server.gameRunning;

/**
 * Game logic and sequence
 *
 * */
public class TheGame {
    static int randomCardIndex;
    static ClientObj playerPlay;
    static Card chosenCard;
    static boolean thisPlayerWins = false;
    static boolean lastManStanding = true;
    static ClientObj gameWinnerFound;
    static int roundCounter = 1;
    static boolean skipturn=false;
    /**
     * Starts game after initialization
     * called by InitializeGame
     * */

     static void startGame() throws IOException, InterruptedException, exceptions {
         //while for game
        while (true) {
            //check token
            for (ClientObj toCheckToken : Server.users){
                if (toCheckToken.getPTokens()>=InitializeGame.tokenToWin){
                    gameWinnerFound = toCheckToken;
                }
            }
            if (gameWinnerFound != null){
                break;
            }
            for (ClientObj toResetElim : Server.users){
                toResetElim.playerEliminated=false;
            }
            thisPlayerWins=false;
            //round start
            broadcast("\u001B[32mStarting Round " +roundCounter+"\u001B[0m",true);

            while (!thisPlayerWins && !InitializeGame.cardDeck.isEmpty()) {
                //turns start
                broadcast("Starting new turn", true);
                for (ClientObj playerPlay : Server.users) {
                    TheGame.playerPlay = playerPlay;
                    skipturn=false;
                    if (playerPlay.playerEliminated == false) {

                        //check if this player is the last man standing, if yes, break
                        lastManStanding = true;
                        for (ClientObj checkPlayer : Server.users) {
                            if (checkPlayer != playerPlay && !checkPlayer.playerEliminated) {
                                //breaks if a player isn't eliminated yet
                                lastManStanding = false;
                            }
                        }
                        //stops turn n round if last man standing
                        if (lastManStanding) {
                            playerPlay.PTokens += 1;
                            thisPlayerWins = true;
                            broadcast(playerPlay.getClientName() + " wins the round", true);
                            lastManStanding = false;
                            break;
                        }
                        //shows all discarded card
                        broadcast(InitializeGame.showDiscardedCards(),true);
                        //shows every player their card at the start of round
                        for(ClientObj toShow: Server.users){
                            if(!toShow.playerEliminated){
                                Server.sendMsg("\u001B[32mYou\u001B[0m have this card: "+toShow.PCards.getFirst().getCName()+"(ID: "+toShow.PCards.getFirst().getCID()+")",toShow);
                            }
                        }
                        Server.sendMsgExclude(playerPlay.getClientName() + "'s turn", playerPlay, "Server");
                        Server.sendMsg("\u001B[31mIt's your turn!\u001B[0m", playerPlay);
                        //reset handmaid protection
                        if(playerPlay.PProtected){
                            playerPlay.PProtected = false;
                        }

                        //draws random card
                        System.out.println("PLAYER CARDS"+playerPlay.getClientName()+playerPlay.PCards);
                        try {
                            drawcard(playerPlay);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        if(!skipturn){

                        System.out.println("PLAYER CARDS"+playerPlay.getClientName()+playerPlay.PCards);
                        System.out.println(InitializeGame.cardDeck);
                        System.out.println(InitializeGame.discardedCards);
                        //show both available cards
                        Server.sendMsg("Please choose card ID to discard:", playerPlay);
                        for (int i = 1; i <= 2; i++) {
                            Server.sendMsg("ID(" + i + ") " + playerPlay.PCards.get(i - 1).getCName() + "(Number: " + playerPlay.PCards.get(i - 1).getCID() + ")", playerPlay);
                            TimeUnit.MILLISECONDS.sleep(5);
                        }
                        //choose card
                        boolean validResponse = false;
                        int chosenCard = 1;
                        while (!validResponse) {
                            String response = playerPlay.askForResponse("", false);
                            try {
                                int tempCheckInt = Integer.parseInt(response);
                                switch (tempCheckInt) {
                                    case 1:
                                        validResponse = true;
                                        break;
                                    case 2:
                                        chosenCard = 2;
                                        validResponse = true;
                                        break;
                                    default:
                                        throw new Exception("");
                                }
                            } catch (Exception e) {
                                Server.sendMsg("Invalid input, please choose a card ID", playerPlay);
                            }
                        }
                        broadcast(playerPlay.getClientName() + " discarded " + playerPlay.PCards.get(chosenCard - 1).getCName(), true);
                        //end choose card
                        //discard card -> take action
                        TheGame.chosenCard = playerPlay.PCards.get(chosenCard - 1);
                        discard(TheGame.chosenCard);
                        // broadcast, discards this

                            }//<- if skipped bracket
                        skipturn=false;
                    }//<- if bracket
                    //check if deck empty
                    if (InitializeGame.cardDeck.isEmpty()) {
                        //round ends because deck empty
                        ClientObj winner = determineWinner();
                        winner.PTokens += 1;
                        thisPlayerWins = true;
                        broadcast(winner.getClientName() + " wins the round", true);

                        break;
                    }
                }//<- for bracket
                //resets handmaid protection (after turns)

            }//<-while bracket
            //round ends, clears card deck, create new deck, clear player cards and discarded cards,
            thisPlayerWins = false;
            ++roundCounter;
            InitializeGame.cardDeck.clear();
            InitializeGame.discardedCards.clear();
            for (ClientObj toClearDeck : Server.users) {
                toClearDeck.PCards.clear();
                toClearDeck.PDiscardedCards.clear();
                toClearDeck.playerEliminated=false;
            }
            InitializeGame.createDeck();
            //print token
            broadcast("Starting new round", true);
            broadcast("------------------", true);
            broadcast("Tokens:", true);
            for (ClientObj printToken : Server.users) {
                if (printToken.inRoom){
                    broadcast( printToken.getClientName()+" has "+printToken.getPTokens() ,true);
                }
            }

        }//<--whiletrue bracket
         //winner found
        broadcast( "\uD83C\uDF89 \u001B[32m"+gameWinnerFound.getClientName()+" wins the game!\u001B[0m \uD83C\uDF89",true);                         TimeUnit.MILLISECONDS.sleep(5);
         TimeUnit.MILLISECONDS.sleep(5);
         broadcast( "----------------------------------",true);
         TimeUnit.MILLISECONDS.sleep(5);
         broadcast( "----------Entered Lobby!----------",false);


         //reset all
        InitializeGame.discardedCards.clear();
         InitializeGame.discardedCards.clear();
         InitializeGame.resetALL();
         System.out.println("Reset all done, ready for next game");
         //back to basics
         gameWinnerFound=null;
         broadcast("\u001B[32mCommands\u001B[0m : CreateGame, JoinGame, ShowRules, ShowPlayers, msg"+"\u001B[1m/Name/Message\u001B[0m"+", bye", false);
         System.out.println(gameRunning);

     }

    /**
     * draws card for a player
     * @param forPlayer target player
     *
     * */
    private static void drawcard(ClientObj forPlayer) throws Exception {
        if(!InitializeGame.cardDeck.isEmpty()) {
            //draws random card, index minus one, because random from size
            randomCardIndex = new Random().nextInt(1,InitializeGame.cardDeck.size() );
            forPlayer.PCards.add(InitializeGame.cardDeck.get(randomCardIndex-1));
            Server.sendMsg("You recieved: "+InitializeGame.cardDeck.get(randomCardIndex-1).getCName()+"(ID: "+InitializeGame.cardDeck.get(randomCardIndex-1).getCID()+")",forPlayer);
            if(InitializeGame.cardDeck.get(randomCardIndex-1).getCID()==7){
                if(forPlayer.checkCard(5)||forPlayer.checkCard(6)){
                    for(Card checkCard:forPlayer.PCards){
                        if(checkCard.getCID()==7){
                            InitializeGame.discardedCards.add(checkCard);
                            forPlayer.PDiscardedCards.add(checkCard);
                            forPlayer.PCards.remove(checkCard);
                            skipturn=true;
                            Server.sendMsg("You recieved Countess and King/Prince exists, Countess must be discarded. Skipping turn",forPlayer);
                        }
                    }
                }
            }
            if(InitializeGame.cardDeck.get(randomCardIndex-1).getCID()==5||InitializeGame.cardDeck.get(randomCardIndex-1).getCID()==6){
                if(forPlayer.checkCard(7)){
                    for(Card checkCard:forPlayer.PCards){
                        if(checkCard.getCID()==7){
                            InitializeGame.discardedCards.add(checkCard);
                            forPlayer.PDiscardedCards.add(checkCard);
                            forPlayer.PCards.remove(checkCard);
                            skipturn=true;
                            Server.sendMsg("You recieved King/Prince, Countess must be discarded. Skipping turn",forPlayer);
                        }
                    }
                }
            }
            InitializeGame.cardDeck.remove(randomCardIndex-1);
        }else{
            throw new Exception ("Deck is empty");
        }
    }

    /**
    * Chooses player to take action
    * @param self = true when self should be chooseable
     * @return chosen player
    * */
     private static ClientObj choosePlayer(boolean self, String msg) throws IOException, InterruptedException {
        Server.sendMsg("Please choose player to "+msg+":",playerPlay);
        //prints player list
        //available IDs to choose. Used later on checking response
        ArrayList<Integer> availPlayerID = new ArrayList<>();
        ArrayList<ClientObj> availablePlayers = new ArrayList<>();
        for(int i =0 ;i<Server.users.size();i++){
            //skips eliminated users
            if(!Server.users.get(i).playerEliminated && !Server.users.get(i).getPProtected()){
                //check self
                 if(self){
                    Server.sendMsg("ID("+(i+1)+") "+Server.users.get(i).getClientName(),playerPlay);
                     availablePlayers.add(Server.users.get(i));
                     availPlayerID.add(++i);
                     --i;
                    TimeUnit.MILLISECONDS.sleep(5);
                }else{
                     if(Server.users.get(i)!=playerPlay){
                     Server.sendMsg("ID("+(i+1)+") "+Server.users.get(i).getClientName(),playerPlay);
                         availablePlayers.add(Server.users.get(i));
                         availPlayerID.add(++i);
                         --i;
                         TimeUnit.MILLISECONDS.sleep(5);
                     }
                 }
            }
        }
        //start check answer
        boolean validPlayer = false;
        int chosenPlayer = 1;
        while (!validPlayer) {
            if(availPlayerID.isEmpty()){
                Server.sendMsg("No player available to choose, continuing game", playerPlay);
                return null;
            }
            String response = playerPlay.askForResponse("", false);
            try {
                int tempCheckInt = Integer.parseInt(response);
                switch (tempCheckInt) {
                    case 1:
                        if(availPlayerID.contains(1)){
                            validPlayer = true;
                            break;
                        }
                        throw new Exception("");
                    case 2:
                        if(availPlayerID.contains(2)){
                            chosenPlayer = 2;
                            validPlayer = true;
                            break;
                        }
                        throw new Exception("");
                    case 3:
                        if(availPlayerID.contains(3)){
                            chosenPlayer = 3;
                            validPlayer = true;
                            break;
                        }
                        throw new Exception("");
                    case 4:
                        if(availPlayerID.contains(4)){
                            chosenPlayer = 4;
                            validPlayer = true;
                            break;
                        }
                        throw new Exception("");
                    default:
                        throw new Exception("");
                }
            } catch (Exception e) {
                Server.sendMsg("Invalid input, please choose a player ID", playerPlay);
            }
    }
        Server.sendMsg("You chose "+Server.users.get(chosenPlayer-1).getClientName(), playerPlay);

        return Server.users.get(chosenPlayer-1);
    }
    /**
     * Eliminates player:
     * - clears player deck
     * - moves player deck to discarded cards
     * @param elimplayer = player to eliminate
     * */
    private static void elimPlayer(ClientObj elimplayer) throws IOException, InterruptedException {
        elimplayer.playerEliminated = true;
        InitializeGame.discardedCards.addAll(elimplayer.PCards);
        elimplayer.PDiscardedCards.addAll(elimplayer.PCards);
        elimplayer.PCards.clear();
        Server.sendMsg("\u001B[31mYou are eliminated\u001B[0m",elimplayer);
        Server.sendMsgExclude(elimplayer.getClientName()+" is eliminated",elimplayer,"\u001B[31mServer\u001B[0m");

    }
    /**
     * Discard card princess and eliminates player
     * @param targetPlayer = player to apply effect on
     * */
    private static void discardPrincess(ClientObj targetPlayer) throws IOException, InterruptedException {
        elimPlayer(targetPlayer);
    }
    /**
     * Discard card Countess
     * @param targetPlayer = player to apply effect on
     * */

    /**
     * Discard card King
     * @param targetPlayer = player to apply effect on
     * */
    private static void discardKing(ClientObj targetPlayer) throws IOException, InterruptedException {
        ClientObj chosenPlayer=choosePlayer(false,"trade card with");
        if(chosenPlayer!=null) {
            Card tempCard = chosenPlayer.PCards.get(0);
            chosenPlayer.PCards.remove(0);
            chosenPlayer.PCards.add(playerPlay.PCards.get(0));
            playerPlay.PCards.remove(0);
            playerPlay.PCards.add(tempCard);
            Server.sendMsg("Now you have: " + playerPlay.PCards.get(0).getCName() + "(Number: " + playerPlay.PCards.get(0).getCID() + ")", playerPlay);
            Server.sendMsg(playerPlay.getClientName() + " traded card with you, now you have: " + chosenPlayer.PCards.get(0).getCName() + "(Number: " + chosenPlayer.PCards.get(0).getCID() + ")", chosenPlayer);
        }
    }
    /**
     * Discard card Prince
     * @param targetPlayer = player to apply effect on
     * */
    private static void discardPrince(ClientObj targetPlayer) throws IOException, InterruptedException {
        ClientObj chosenPlayer=choosePlayer(true,"discard their card and to draw a new one");
        if (chosenPlayer!=null) {
            if (chosenPlayer.PCards.get(0).getCID() == 8) {
                discardPrincess(chosenPlayer);
            } else {
                Server.sendMsg(playerPlay.getClientName() + " used prince on you. Your card is discarded", chosenPlayer);
                InitializeGame.discardedCards.addAll(chosenPlayer.PCards);
                chosenPlayer.PCards.clear();
                //draws new card
                try {
                    drawcard(chosenPlayer);
                } catch (Exception e) {
                    System.out.println(e + ", drawing card from discarded card");
                    chosenPlayer.PCards.add(InitializeGame.discardedCards.getFirst());
                    InitializeGame.discardedCards.removeFirst();
                }

                Server.sendMsg("This card is drawn for you: " + chosenPlayer.PCards.getFirst().getCName() + "(Number: " + chosenPlayer.PCards.getFirst().getCID() + ")", chosenPlayer);

            }
        }
    }
    /**
     * Discard card Handmaid
     * @param targetPlayer = player to apply effect on
     * */
    private static void discardHandmaid(ClientObj targetPlayer) throws IOException {
        targetPlayer.PProtected =true;
    }
    /**
     * Discard card Baron
     * @param targetPlayer = player to apply effect on
     * */
    private static void discardBaron(ClientObj targetPlayer) throws IOException, InterruptedException {
        Card playerplayCard;
        ClientObj playerToCompare = choosePlayer(false,"compare card with:");
        if(playerToCompare!=null) {
            if (playerPlay.PCards.get(0) == chosenCard) {
                playerplayCard = playerPlay.PCards.get(1);
            } else {
                playerplayCard = playerPlay.PCards.get(0);
            }
            if (playerToCompare.PCards.get(0).getCID() < playerplayCard.getCID()) {
                Server.sendMsg(playerPlay.getClientName() + " compared card with you and you lost.", playerToCompare);
                Server.sendMsg(playerToCompare.getClientName() + " has less card number", playerPlay);
                elimPlayer(playerToCompare);
                //tell they are compared and lost



            } else if (playerToCompare.PCards.get(0).getCID() == playerplayCard.getCID()) {
                //do nothing
                //print its same number
                Server.sendMsg("You both have the same card. No one eliminated", playerToCompare);

            } else {
                //tell you are compared and lost
                Server.sendMsg(playerToCompare.getClientName() + " has bigger card number, you lost", targetPlayer);
                elimPlayer(targetPlayer);
            }
        }else{
            Server.sendMsg("Unable to compare", playerPlay);
        }
    }
    /**
     * Discard card Priest
     * @param targetPlayer = player to apply effect on
     * */
    private static void discardPriest(ClientObj targetPlayer) throws IOException, InterruptedException {
        TimeUnit.MILLISECONDS.sleep(2);
        ClientObj chosenPlayer= choosePlayer(false,"reveal their card:");
        if (chosenPlayer!=null) {
            TimeUnit.MILLISECONDS.sleep(2);
            Server.sendMsg(chosenPlayer.getClientName() + " has " + chosenPlayer.PCards.getFirst().getCName() + "(Number: " + chosenPlayer.PCards.getFirst().getCID() + ")", playerPlay);
            TimeUnit.MILLISECONDS.sleep(2);
            Server.sendMsg(playerPlay.getClientName() + " saw your card.", chosenPlayer);
        }
    }
    /**
     * Discard card Guard
     * @param targetPlayer = player to apply effect on
     * */
    private static void discardGuard(ClientObj targetPlayer) throws IOException, InterruptedException {

        ClientObj chosenPlayer= choosePlayer(false,"guess their card:");
        if(chosenPlayer!=null) {


            //prints all card number
            Server.sendMsg("Which card \u001B[32mnumber\u001B[0m do you think " + chosenPlayer.getClientName() + " has?", playerPlay);
            Server.sendMsg("\u001B[32m8\u001B[0m. Princess", playerPlay);
            TimeUnit.MILLISECONDS.sleep(2);
            Server.sendMsg("\u001B[32m7\u001B[0m. Countess", playerPlay);
            TimeUnit.MILLISECONDS.sleep(2);
            Server.sendMsg("\u001B[32m6\u001B[0m. King", playerPlay);
            TimeUnit.MILLISECONDS.sleep(2);
            Server.sendMsg("\u001B[32m5\u001B[0m. Prince", playerPlay);
            TimeUnit.MILLISECONDS.sleep(2);
            Server.sendMsg("\u001B[32m4\u001B[0m. Housemaid", playerPlay);
            TimeUnit.MILLISECONDS.sleep(2);
            Server.sendMsg("\u001B[32m3\u001B[0m. Baron", playerPlay);
            TimeUnit.MILLISECONDS.sleep(2);
            Server.sendMsg("\u001B[32m2\u001B[0m. Priest", playerPlay);
            TimeUnit.MILLISECONDS.sleep(2);
            Server.sendMsg("\u001B[32m1\u001B[0m. Guard", playerPlay);

            boolean validNumber = false;
            int chosenNumber = 0;
            //choose card number
            while (!validNumber) {
                String response = playerPlay.askForResponse("", false);
                try {
                    int tempCheckInt = Integer.parseInt(response);
                    switch (tempCheckInt) {
                        case 8:
                            chosenNumber = 8;
                            validNumber = true;
                            break;
                        case 7:
                            chosenNumber = 7;
                            validNumber = true;
                            break;

                        case 6:
                            chosenNumber = 6;
                            validNumber = true;
                            break;

                        case 5:
                            chosenNumber = 5;
                            validNumber = true;
                            break;

                        case 4:
                            chosenNumber = 4;
                            validNumber = true;
                            break;

                        case 3:
                            chosenNumber = 3;
                            validNumber = true;
                            break;

                        case 2:
                            chosenNumber = 2;
                            validNumber = true;
                            break;

                        case 1:
                            chosenNumber = 1;
                            validNumber = true;
                            break;

                        default:
                            throw new Exception("");
                    }
                } catch (Exception e) {
                    Server.sendMsg("Invalid input, please choose card \u001B[32mnumber\u001B[0m:", playerPlay);
                }
            }
            if (chosenNumber == chosenPlayer.PCards.getFirst().getCID()) {
                Server.sendMsg(playerPlay.getClientName()+" guessed your card",chosenPlayer);
                elimPlayer(chosenPlayer);
            } else {
                Server.sendMsg("Guess missed. No one eliminated", playerPlay);
            }
        }
    }
    /**
     * Method to discard card
     * @param cardToDiscard which card to discard
     *
     * */
    private static void discard(Card cardToDiscard) throws IOException, InterruptedException, exceptions {
        int cardId = cardToDiscard.getCID();
        switch(cardId){
            case 8:
                playerPlay.discardCard(cardToDiscard);
                discardPrincess(playerPlay);
                break;
            case 7:
                playerPlay.discardCard(cardToDiscard);
                break;
            case 6:
                playerPlay.discardCard(cardToDiscard);
                discardKing(playerPlay);
                break;
            case 5:
                playerPlay.discardCard(cardToDiscard);
                discardPrince(playerPlay);
                break;
            case 4:
                playerPlay.discardCard(cardToDiscard);
                discardHandmaid(playerPlay);
                break;
            case 3:
                playerPlay.discardCard(cardToDiscard);
                discardBaron(playerPlay);
                break;
            case 2:
                playerPlay.discardCard(cardToDiscard);
                discardPriest(playerPlay);
                break;
            case 1:
                playerPlay.discardCard(cardToDiscard);
                discardGuard(playerPlay);
                break;
            default:
                System.out.println("Error choosing card");
                break;

        }

    }

    /**
     * Determine winner if point comparison needed
     * @return winner as ClientObj
     * */
    private static ClientObj determineWinner(){
        ArrayList<Integer> playerPoints = new ArrayList<Integer>();
        ArrayList<Integer> duplicateCompare = new ArrayList<Integer>();
        ArrayList<ClientObj> duplicateCompareClient = new ArrayList<ClientObj>();

        for(ClientObj addThisPlayer: Server.users) {
            if (!addThisPlayer.playerEliminated){
                playerPoints.add(addThisPlayer.PCards.getFirst().getCID());
            }else{
                playerPoints.add(0);
            }
        }
        //look for duplicates first
        int duplicate = 0;
        for (int i = 0; i < playerPoints.size(); i++) {
            for (int j = i + 1; j < playerPoints.size(); j++) {
                if (playerPoints.get(i).equals(playerPoints.get(j))) {
                    duplicate = i;
                    break;
                }
            }
        }

            //look for max
            int maxPoint = Collections.max(playerPoints);
            int maxPlayerIndex = playerPoints.indexOf(maxPoint);
            //is duplicate == max?
            if (playerPoints.get(maxPlayerIndex).equals(duplicate)) {
                for (int x = 0; x < maxPlayerIndex; x++) {
                    if (playerPoints.get(x).equals(duplicate)) {
                        //adds player to compare into list
                        duplicateCompareClient.add(Server.users.get(x));
                        int sumPoints = 0;
                        for (int y = 0; y < Server.users.get(x).PDiscardedCards.size(); y++) {
                            sumPoints += Server.users.get(x).PDiscardedCards.get(y).getCID();
                        }
                        duplicateCompare.add(sumPoints);
                    }

                }
                int maxPoint2 = Collections.max(duplicateCompare);
                int maxPlayerIndex2 = playerPoints.indexOf(maxPoint2);
                return Server.users.get(maxPlayerIndex2);

            }
            return Server.users.get(maxPlayerIndex);


        }
    }


