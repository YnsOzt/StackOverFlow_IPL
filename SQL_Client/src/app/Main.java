package app;
import static util.Util.lireEntierEntre;
import static util.Util.lireEntier;
import java.sql.Connection;

public class Main {
	@SuppressWarnings("unused")
	private static Connection conn;
	public static void main(String[] args) {
		Connection conn = new Connexion().getConnexion();
		DBManager db = new DBManager(conn);
		int choix = 0;
		boolean connected = false;
		System.out.println("--- Bienvenue ---");
		do {
			System.out.println("1) Se connecter");
			System.out.println("2) S'inscire");
			System.out.println("3) Quitter l'application");
			choix = lireEntierEntre(1,3);
			switch(choix) {
			case 1:
				while(db.connexion() == -1) {
					
				}
				connected = true;
				break;
			case 2:
				db.creeUtiisateur();
				break;
			case 3:
				System.out.println("Aurevoir !");
				System.exit(0);
			}
		}while((choix == 2) || (choix < 1 && choix > 3));
		
		if(connected) {
			do {
				System.out.println("1) introduire une nouvelle question");
				System.out.println("2) Visualiser les questions que vous avez posées");
				System.out.println("3) Visualiser les questions auxquelles vous avez répondu");
				System.out.println("4) Visualiser toutes les questions");
				System.out.println("5) Visualiser les questions liée à un tag en particulier");
				System.out.println("6) Visualiser les réponses d'une question");
				System.out.println("7) Quitter l'application");
				choix = lireEntierEntre(1,7);
				switch(choix) {
				case 1:
					System.out.println(db.introduireQuestion()) ;
					break;
				case 2:
					db.visualierQuestionsPose();
					break;
				case 3:
					db.visualierQuestionsRepondu();
					break;
				case 4:
					db.visualiserToutesQuestionsPosees();
					break;
				case 5:
					db.visualiersQuestionsTags();
					break;
				case 6:
					db.visualiserToutesQuestionsPosees();
					int noQuest;
					do{
						noQuest = lireEntier("Veuillez entrez un numéro de question valide");
					}while(!db.verifierQuestion(noQuest));
					boolean termine = false;
					do {
						db.visualiserReponseQuestion(noQuest);
						System.out.println("1) introduire une nouvelle réponse");
						System.out.println("2) voter pour une réponse");
						System.out.println("3) éditer la question");
						System.out.println("4) éditer une réponse");
						System.out.println("5) ajouter tag pour la question");
						System.out.println("6) clôturée la question");
						System.out.println("7) revenir en arrière");
						choix = lireEntierEntre(1,7);
						switch(choix) {
						case 1:
							db.ajouterReponse(noQuest);
							break;
						case 2:
							db.voterReponse(noQuest);
							break;
						case 3:
							db.editerQuestion(noQuest);
							break;
						case 4:
							db.editerReponse(noQuest);
							break;
						case 5:
							db.ajouterTagQuestion(noQuest);
							break;
						case 6:
							db.cloturerQuestion(noQuest);
							termine = true;
							break;
						case 7:
							termine = true;
						}
					}while(!termine);
				break;
				case 7:
					System.out.println("Aurevoir !");
					System.exit(0);		
				}
			}while( true);
		}
	}
	
	
	
	
	
}
