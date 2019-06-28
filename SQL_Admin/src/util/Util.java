package util;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Util {
	private static Scanner scanner = new Scanner(System.in);
	
	
	//fonction qui lit un entier entre les bornes comprise
	public static int lireEntierEntre(int a, int b) {
		int choix;
		do {
			System.out.println("\nVeuillez introduire un chiffre entre " + a + " et " + b);
			try {
				choix = scanner.nextInt();
				if(choix >= a && choix <= b) {
					return choix;
				}
			}catch(InputMismatchException e) {
				scanner.nextLine();//nettoi le scanner dans le cas où l'utilisateur aurait entrer un mauvais input
				
			}
		}while(true);
	}
	
	//fonction qui lit un entier
		public static int lireEntier(String message) {
			int choix = 0;
			try {
				System.out.println(message);
				choix = scanner.nextInt();
				
			}catch(InputMismatchException e) {
				scanner.nextLine();
			}
			return choix;
		}
	
	//fonction qui affiche le message et lit un String
	public static String lireString(String message) {
		String msg;
		do {
			System.out.println(message);
			try {
				msg = scanner.next();
				return msg;
			}catch(InputMismatchException e) {
				scanner.nextLine();
			}
		}while(true);
	}
	
	//fonction qui affiche le message et lit une ligne
	public static String lireLigne(String message) {
		
		String msg;
		do {
			System.out.println(message);
			try {
				msg = scanner.nextLine();
				return msg;
			}catch(InputMismatchException e) {
				scanner.nextLine();
			}
		}while(true);
	}
	
	public static void viderBuffer() {
		if(scanner.hasNextLine()) {
			scanner.nextLine();
		}
	}
	
	
	
}
