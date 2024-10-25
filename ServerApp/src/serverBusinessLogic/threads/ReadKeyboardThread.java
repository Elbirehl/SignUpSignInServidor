/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverBusinessLogic.threads;

import java.util.Scanner;

/**
 *
 * @author 2dam
 */
public class ReadKeyboardThread extends Thread{
  
   
   public void run(){
        Scanner sc = new Scanner(System.in);
    System.out.println("Presiona esc para salir");
    
    String input = sc.nextLine();
    if (input.equalsIgnoreCase("esc")){
        
    }
   }
}
