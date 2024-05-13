package com.yourcompany.app;
import java.io.IOException;
import java.util.Scanner;  // Import the Scanner class

public class LoveLetterGame {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Welcome in Love Letter Game!");
        System.out.println("Please enter your Role(Server/Player)");
        System.out.print("Role:");


        Scanner userInput = new Scanner(System.in);  // Create a Scanner object

        String userName = userInput.nextLine();
        if (userName.equals("Server")) {
            Server.main(null);
        }else if(userName.equals("Player")){
            Client.main(null);
        }
        else{
            System.out.println("Role not recognizable. Please try again later");
        }
    }

}
