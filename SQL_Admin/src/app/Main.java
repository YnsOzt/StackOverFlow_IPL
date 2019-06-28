package app;

import java.sql.Connection;
import static util.Util.lireEntierEntre;
public class Main {

	public static void main(String[] args) {
		Connection conn = new Connexion().getConnexion();
		DBManager db = new DBManager(conn);
		int choix = 0;
		Boolean continuer = true;
		System.out.println("---- Bienvenue dans la partie Admin de l'application");
		do {
			System.out.println("1) Désactiver un compte");
			System.out.println("2) Améliorer le status d'un utilisateur");
			System.out.println("3) Consulter l'historique d'un utilisateur");
			System.out.println("4) Créer un tag");
			System.out.println("5) Quitter l'application");
			choix = lireEntierEntre(1,4);
			
			switch(choix) {
			case 1:
				db.desactiverCompteUtilisateur();
				break;
				
			case 2:
				db.ameliorerStatusUtilisateur();
				break;
				
			case 3:
				db.voirHistorique();
				break;
				
			case 4:
				db.creeTag();
				break;
				
			case 5:
				System.out.println("Aurevoir !");
				System.exit(1);
			}
		}while(continuer);
	}

}
