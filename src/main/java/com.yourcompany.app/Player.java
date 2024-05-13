package com.yourcompany.app;

import java.util.ArrayList;


public class Player{
    protected Boolean PProtected;
    protected int PTokens;
    ArrayList<Card> PCards;
    ArrayList<Card> PDiscardedCards;
     boolean playerReady = false;
     boolean playerEliminated = false;

    /* Construction */
    /**
     * Creates Player
     *
     * @param playerReady : True = ready to play
     * @param PProtected : True = Protected by Handmaid
     * @param PTokens : Number of Tokens
     */
    public Player(int PID, String PName, boolean PProtected, int PTokens, boolean playerReady)
    {
        this.PProtected = PProtected;
        this.PTokens = PTokens;
        this.playerReady = playerReady;
        PCards = new ArrayList<>();
        PDiscardedCards = new ArrayList<>();
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
     * @return Discards Card
     */
     void discardCard(Card card)throws exceptions{
        try{
            PCards.remove(card);
            InitializeGame.discardedCards.add(card);

        }
        catch(Exception e){
            throw new exceptions("Card couldn't be discarded" );
        }
    }
    /**
     * Check if card is in the deck
     * @param cardID = card id to check
     * */
    boolean checkCard(int cardID)throws exceptions{
        for(Card card : PCards){
            if(card.getCID()==cardID){
                return true;
            }
        }return false;
    }



}
