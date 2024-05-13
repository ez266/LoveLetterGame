package com.yourcompany.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Game initialization, before running
 *
 * */

public class InitializeGame {
    static int numberPlayer;
    static ArrayList<Card> cardDeck = new ArrayList<>();
    static ArrayList<Card> discardedCards = new ArrayList<>();
    static int tokenToWin;

    public static void init() throws IOException, InterruptedException, exceptions {
        Server.gameRunning=true;
        numberPlayer =Server.users.size();
        TokenBestimmen();
        TimeUnit.MILLISECONDS.sleep(20);
        createDeck();
        TheGame.startGame();

    }
    /**
     * Creates Deck
     */
     protected static void createDeck() throws IOException {
            cardDeck.add(new CPrincess());
            cardDeck.add(new CCountess());
            cardDeck.add(new CKing());
            cardDeck.add(new CPrince());
            cardDeck.add(new CPrince());
            cardDeck.add(new CHandmaid());
            cardDeck.add(new CHandmaid());
            cardDeck.add(new CBaron());
            cardDeck.add(new CBaron());
            cardDeck.add(new CPriest());
            cardDeck.add(new CPriest());
            cardDeck.add(new CGuard());
            cardDeck.add(new CGuard());
            cardDeck.add(new CGuard());
            cardDeck.add(new CGuard());
            cardDeck.add(new CGuard());

        int randomCardIndex;
        //removes 3 cards if 2 players only
        if (numberPlayer==2){
                for (int i = 1; i<=3; i++) {
                    randomCardIndex = new Random().nextInt(1,cardDeck.size());
                    discardedCards.add(cardDeck.get(randomCardIndex-1));
                    cardDeck.remove(randomCardIndex-1);
                }
            }
            //draws 1 card for every user
            for(ClientObj drawCardFor : Server.users) {
                if (drawCardFor.inRoom){
                    randomCardIndex = new Random().nextInt(1, cardDeck.size());
                    drawCardFor.PCards.add(cardDeck.get(randomCardIndex-1));
                    cardDeck.remove(cardDeck.get(randomCardIndex-1));
                    System.out.println("CARD FROM " + drawCardFor.getClientName() + drawCardFor.PCards);
                }
            }
    }
    /**
     * Determine Anzahl Token to Win
     */
    private static void TokenBestimmen(){
        switch(numberPlayer) {
            case 2:
                tokenToWin =7;
                break;
            case 3:
                tokenToWin =5;
                break;
            default:
                tokenToWin =4;
        }
    }
     protected static String showDiscardedCards() {

        String discardedCardstemp = "Discarded Cards List:///";
        for(Card cardx : discardedCards) {
            discardedCardstemp+= "- "+cardx.getCName()+", ";
        }
        return discardedCardstemp;
    }

    protected static void resetALL() {
        discardedCards.clear();
        cardDeck.clear();
        for(ClientObj resetuser : Server.users) {
            resetuser.inRoom= false;
            resetuser.playerEliminated=false;
            resetuser.PDiscardedCards.clear();
            resetuser.PCards.clear();
            resetuser.PProtected=false;
            resetuser.playerReady=false;
            resetuser.PTokens=0;
        }
        Server.gameRunning=false;
        Server.roomOpen = false;
        tokenToWin=100;
        TheGame.roundCounter=1;

    }

}
