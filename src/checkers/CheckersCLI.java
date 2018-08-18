package checkers;

import java.util.Scanner;
import checkers.Logic.CheckersLogic;

public class CheckersCLI{
	
	public static void main(String[] args){
		Scanner sc=new Scanner(System.in);
		
		boolean validLevel=false;
		byte gameLevel=1;
		while(validLevel==false) {
			try {
				gameLevel = setDifficulty(sc);
				validLevel=true;
			}catch(Exception e) {
				System.out.println("INVALID DIFFICULTY LEVEL: The difficulty level must be an integer between 1 and 255\n");
			}
		}

		CheckersLogic game = new CheckersLogic(gameLevel);
		
		//Test setup methods
		//game.debugTestKingSetup();
		//game.debugTestDoubleHopRed();
		//game.debugTestDoubleHopBlack();
		
		String input;
		System.out.println(game.getBoard());
		System.out.println();
		System.out.print("You are Red, make the first move:");
		input = sc.nextLine().toUpperCase();
		try {
			while(game.bothHavePieces() && !input.equals("0")){
				byte[] move;
				try {
					move=parseMove(input);
				}catch(Exception e) {
					System.out.println(e.getMessage());
					input = sc.nextLine().toUpperCase();
					continue;
				}
								
				if(game.acceptMove(move)) {
					game.computerMove();
					System.out.println("My move: "+moveToString(game.getLastMove()));
					System.out.println(game.getBoard());
					System.out.print("Your move: ");
				}else{
					System.out.println("That isn't a valid move. Try again.");
				}
				input = sc.nextLine().toUpperCase();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		sc.close();
	}
	
	private static byte setDifficulty(Scanner sc) throws Exception {
		System.out.println("Please choose a difficulty level. The higher the number, the harder your opponent.");
		System.out.println("Note: If you choose a level that exceeds the capabilities of your machine, errors may occur.");
				
		byte diff = Byte.parseByte(sc.nextLine());
		if(diff==0) {
			throw new Exception("0 is not a valid difficulty level.");
		}
		return diff;
	}

	private static byte[] parseMove(String move) throws Exception{
		int len = move.length();
		if(len%2==1) {
			throw new Exception("moves must have an even number of characters to completely define positions");
		}
		byte[] retVal=new byte[len];
		try{
			for(int i=0; i<len; i++) {
				if(i%2==0) {
					retVal[i]=(byte)(move.charAt(i)-65);
				}else{
					retVal[i]=(byte)(Integer.parseInt(""+move.charAt(i))-1);
				}
			}
		}catch(Exception e){
			throw new Exception("Problem parsing move");
		}
		return retVal;
	}
	
	private static String moveToString(byte[] move){
		String retVal = "";
		try{
			for(int i=0; i<move.length; i+=2) {
				retVal += (char)(move[i]+65);
				retVal += (move[(i+1)]+1)+" ";
			}
		}catch(Exception e){
			retVal = "Error converting move to string.";
		}
		return retVal;
	}
	
}
