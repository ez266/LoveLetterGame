package com.yourcompany.app;
/**
 * Creates Card Object
 *
 */
public class Card {
    private int CID;
    private String CName;

    /**
     * Card Constructor
     *
     * @param CID : com.yourcompany.app.Card ID (1-8)
     * @param CName : com.yourcompany.app.Card Name

     */
    public Card(int CID, String CName)
    {
        this.CID = CID;
        this.CName = CName;
    }

    /* Start of Methods */
    /**
     * @return com.yourcompany.app.Card ID
     */
     int getCID()
    {
        return CID;
    }
    /**
     * @return com.yourcompany.app.Card Name
     */
     String getCName()
    {
        return CName;
    }

}
