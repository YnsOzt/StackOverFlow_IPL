package app;
import static util.EncryptAndDecrypt.Decrypt;
import static util.EncryptAndDecrypt.encrypt;
import static util.Util.lireLigne;
import static util.Util.lireEntierEntre;
import static util.Util.lireEntier;
import static util.Util.lireString;
import static util.Util.viderBuffer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import util.Querries;

public class DBManager {
	@SuppressWarnings("unused")
	private Connection conn;
	private int id_utilisateur_connecte;
	private String pseudo_utilisateur_connecte;
	
	private PreparedStatement seConnecter, recupereMotDePasse, inscrireUtilisateur,
						recuperePseudoUtilisateur,questionsPosee,questionsRepondu,touteQuestionsPosee,
						questionsAvecTags, introductionQuestion, voirTags, ajouterTagsQuestion,
						voirReponses, ajouterReponse, verifierQuestion, verifierTag, cloturerQuestion,
						recupereNoReponse,voterReponse, modifierCorpsQuestion, modifierTitreQuestion,
						modifierCorpsReponse;
	
	public DBManager(Connection conn) {
		this.conn = conn;
		try {
			seConnecter = conn.prepareStatement(Querries.seConnecter);
			recupereMotDePasse = conn.prepareStatement(Querries.recupMDP);
			inscrireUtilisateur = conn.prepareStatement(Querries.inscrireUtilisateur);
			recuperePseudoUtilisateur = conn.prepareStatement(Querries.recuperePseudoUtilisateur);
			questionsPosee = conn.prepareStatement(Querries.questionsPosee);
			questionsRepondu = conn.prepareStatement(Querries.questionsRepondu);
			touteQuestionsPosee = conn.prepareStatement(Querries.touteQuestionsPosee);
			questionsAvecTags = conn.prepareStatement(Querries.questionsAvecTags);
			introductionQuestion = conn.prepareStatement(Querries.introductionQuestion);
			voirTags = conn.prepareStatement(Querries.voirTags);
			ajouterTagsQuestion = conn.prepareStatement(Querries.ajouterTagsQuestion);
			voirReponses = conn.prepareStatement(Querries.voirReponses);
			ajouterReponse = conn.prepareStatement(Querries.ajouterReponse);
			verifierQuestion = conn.prepareStatement(Querries.verifierQuestion);
			verifierTag = conn.prepareStatement(Querries.verifierTag);
			cloturerQuestion = conn.prepareStatement(Querries.cloturerQuestion);
			recupereNoReponse = conn.prepareStatement(Querries.recupereNoReponse);
			voterReponse = conn.prepareStatement(Querries.voterReponse);
			modifierCorpsQuestion = conn.prepareStatement(Querries.modfifierCorpsQuestion);
			modifierCorpsReponse = conn.prepareStatement(Querries.modifierCorpsReponse);
			modifierTitreQuestion = conn.prepareStatement(Querries.modifierTitreQuestion);
		}catch(SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	//fonction qui connecte un utilisateur
	public int connexion() {
		System.out.println("Information de connexion : ");
		id_utilisateur_connecte = -1;
		String utilisateur = lireString("Veuillez entrez votre nom d'utilisateur : ");
		String motDePasseClair = lireString("Veuillez entrez votre mot de passe : ");
		
		try {
			recupereMotDePasse.setString(1, utilisateur);
			try(ResultSet rs = recupereMotDePasse.executeQuery()){
				rs.next();
				String hashed_mdp = rs.getString(1);
				if(hashed_mdp != null) {
					if(Decrypt(motDePasseClair, hashed_mdp)) { //si mdp correcte
						seConnecter.setString(1, utilisateur);
						try(ResultSet rs2 = seConnecter.executeQuery()){
							rs2.next();
							id_utilisateur_connecte = rs2.getInt(1);
							recuperePseudoUtilisateur.setInt(1, id_utilisateur_connecte);
							try(ResultSet rs3 = recuperePseudoUtilisateur.executeQuery()){
								rs3.next();
								pseudo_utilisateur_connecte = rs3.getString(1);
								
							}
							System.out.println("Connexion réussit, Bienvenue "+pseudo_utilisateur_connecte +"!");
						}
					}
				}else {
					System.out.println("Connexion échoué, Réessayer !");
					System.out.println();
				}
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return id_utilisateur_connecte;
	}
	
	//fonction qui permet la création d'un utilisateur
	public int creeUtiisateur() {
		System.out.println("Création de compte : ");
		String mail = lireString("Veuillez entrez l'email de l'utilisateur à crée : ");
		String login = lireString("Veuillez entrez le login de l'utilisateur à crée : ");
		String mdp = lireString("Veuillez entrez le mot de passe de l'utilisateur à crée : ");
		int toReturn = -1;
		try {
			inscrireUtilisateur.setString(1, mail);
			inscrireUtilisateur.setString(2,login);
			inscrireUtilisateur.setString(3, encrypt(mdp));
			try(ResultSet rs = inscrireUtilisateur.executeQuery()){
				rs.next();
				toReturn = rs.getInt(1);//recupère l'id du nouvel utilisateur
				System.out.println("Utilisateur crée !");
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
	
	public void visualierQuestionsPose() {
		System.out.println("Voici les différentes questions que vous avez posées");
	
		String pseudoAuteur;
		int numeroQuestion;
		String titre;
		Timestamp dateCreation;
		String editeur;
		Timestamp dateDernierEdition;
		String corps;
		try {
			System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
			System.out.printf("| %-6s | %-30s | %-17s | %-28s | %-17s | %-21s |\n","Numéro","Titre","Auteur","Date création","Editeur","Date édition");
			System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
			
			questionsPosee.setString(1, pseudo_utilisateur_connecte);
			try(ResultSet rs = questionsPosee.executeQuery()){
				while(rs.next()) {
					pseudoAuteur = rs.getString(1);
					numeroQuestion = rs.getInt(2);
					titre = rs.getString(3);
					dateCreation = rs.getTimestamp(4);
					editeur = rs.getString(5);
					dateDernierEdition = rs.getTimestamp(6);
					corps = rs.getString(8);
					System.out.printf("| %-6s | %-30s | %-17s | %-28s | %-17s | %-21s |\n",numeroQuestion, titre, pseudoAuteur, dateCreation, editeur, dateDernierEdition);
					System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
					System.out.printf("|Question : %-124s |\n",corps);
					System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
				}
				System.out.println();
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void visualierQuestionsRepondu() {
		System.out.println("Voici les différentes questions auxquelles vous avez répondu : ");
	
		String pseudoAuteur;
		int numeroQuestion;
		String titre;
		Timestamp dateCreation;
		String editeur;
		Timestamp dateDernierEdition;
		String corps;
		try {
			System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
			System.out.printf("| %-6s | %-30s | %-17s | %-28s | %-17s | %-21s |\n","Numéro","Titre","Auteur","Date création","Editeur","Date édition");
			System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
			
			questionsRepondu.setInt(1, id_utilisateur_connecte);
			try(ResultSet rs = questionsRepondu.executeQuery()){
				while(rs.next()) {
					pseudoAuteur = rs.getString(1);
					numeroQuestion = rs.getInt(2);
					titre = rs.getString(3);
					dateCreation = rs.getTimestamp(4);
					editeur = rs.getString(5);
					dateDernierEdition = rs.getTimestamp(6);
					corps = rs.getString(8);
					System.out.printf("| %-6s | %-30s | %-17s | %-28s | %-17s | %-21s |\n",numeroQuestion, titre, pseudoAuteur, dateCreation, editeur, dateDernierEdition);
					System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
					System.out.printf("|Question : %-124s |\n",corps);
					System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
				
				}
				System.out.println();
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}

	
	public void visualiserToutesQuestionsPosees() {
		System.out.println("Voici les différentes questions auxquelles vous avez répondu : ");
		String pseudoAuteur;
		int numeroQuestion;
		String titre;
		Timestamp dateCreation;
		String editeur;
		Timestamp dateDernierEdition;
		String corps;
		try {
			System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
			System.out.printf("| %-6s | %-30s | %-17s | %-28s | %-17s | %-21s |\n","Numéro","Titre","Auteur","Date création","Editeur","Date édition");
			System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
			
			try(ResultSet rs = touteQuestionsPosee.executeQuery()){
				while(rs.next()) {
					pseudoAuteur = rs.getString(1);
					numeroQuestion = rs.getInt(2);
					titre = rs.getString(3);
					dateCreation = rs.getTimestamp(4);
					editeur = rs.getString(5);
					dateDernierEdition = rs.getTimestamp(6);
					corps = rs.getString(8);
					System.out.printf("| %-6s | %-30s | %-17s | %-28s | %-17s | %-21s |\n",numeroQuestion, titre, pseudoAuteur, dateCreation, editeur, dateDernierEdition);
					System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
					System.out.printf("|Question : %-124s |\n",corps);
					System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
				
				}
				System.out.println();
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void visualiersQuestionsTags() {
		afficherTags();
		String pseudoAuteur;
		int numeroQuestion;
		String titre;
		Timestamp dateCreation;
		String editeur;
		Timestamp dateDernierEdition;
		String corps;
		String tag;
		do {
			afficherTags();
			tag = lireString("Veuillez un tag qui se trouve bien dans la liste");
		}while(!verifierTag(tag));
		System.out.println("Voici les différentes questions contenant le tag "+tag+" : ");
		try {
			System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
			System.out.printf("| %-6s | %-30s | %-17s | %-28s | %-17s | %-21s |\n","Numéro","Titre","Auteur","Date création","Editeur","Date édition");
			System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
			
			questionsAvecTags.setString(1, tag);
			try(ResultSet rs = questionsAvecTags.executeQuery()){
				while(rs.next()) {
					pseudoAuteur = rs.getString(1);
					numeroQuestion = rs.getInt(2);
					titre = rs.getString(3);
					dateCreation = rs.getTimestamp(4);
					editeur = rs.getString(5);
					dateDernierEdition = rs.getTimestamp(6);
					corps = rs.getString(9);
					System.out.printf("| %-6s | %-30s | %-17s | %-28s | %-17s | %-21s |\n",numeroQuestion, titre, pseudoAuteur, dateCreation, editeur, dateDernierEdition);
					System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
					System.out.printf("|Question : %-124s |\n",corps);
					System.out.println("+--------+--------------------------------+-------------------+------------------------------+-------------------+-----------------------+");
				
				}
				System.out.println();
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public int introduireQuestion() {
		System.out.println("Création de question : ");
		String titre = lireLigne("Veuillez entrer le titre de votre question : ");
		String corps = lireLigne("Veuillez entrer le corps de votre question : ");
		int numeroQuestion = -1;
		try {
			introductionQuestion.setString(1, titre);
			introductionQuestion.setString(2, corps);
			introductionQuestion.setInt(3,id_utilisateur_connecte);
			try (ResultSet rs = introductionQuestion.executeQuery()){
				rs.next();
				numeroQuestion = rs.getInt(1);
				System.out.println("Votre question à bien été bien introduite ");
			}
			System.out.println();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return numeroQuestion;
	}
	
	private void afficherTags() {
		System.out.println("Voici les différentes Tags : ");
		try {
			try(ResultSet rs = voirTags.executeQuery()){
				while(rs.next()) {
					System.out.println(rs.getString(1));
				}
				System.out.println();
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void ajouterTagQuestion(int noQuest) {
		try {
			visualierQuestionsPose();
			String tag;
			do {
				afficherTags();
				tag = lireString("Veuillez un tag qui se trouve bien dans la liste");
			}while(!verifierTag(tag));
			
			ajouterTagsQuestion.setInt(1, id_utilisateur_connecte);
			ajouterTagsQuestion.setInt(2, noQuest);
			ajouterTagsQuestion.setString(3, tag);
			try(ResultSet rs2 = ajouterTagsQuestion.executeQuery()){
				System.out.println("Le tag" + tag +"a bien été ajouter à la question numéro "+noQuest);
			}
			
		}catch(SQLException e) {
			System.out.println(e.getMessage());
			System.out.println();
		}

	}
	
	
	
	public void visualiserReponseQuestion(int noQuest) {
		int numero;
		Timestamp date;
		String auteur;
		int score;
		String reponse;
		System.out.println("Voici les réponses de la question"+noQuest +" : ");
		try {
			System.out.println("+--------+-------------------+------------------------------+--------+----------------------------------------------------+");
			System.out.printf("| %-6s | %-17s | %-28s | %-6s | %-50s |\n","Numéro","Auteur","Date création","score","reponse");
			System.out.println("+--------+-------------------+------------------------------+--------+----------------------------------------------------+");
			
			voirReponses.setInt(1, noQuest);
			try(ResultSet rs = voirReponses.executeQuery()){
				while(rs.next()) {
					numero = rs.getInt(1);
					date = rs.getTimestamp(2);
					recuperePseudoUtilisateur.setInt(1, rs.getInt(3));
					//récupère le pseudo de l'utilisateur
					try(ResultSet rs2 = recuperePseudoUtilisateur.executeQuery()) {
						rs2.next();
						auteur = rs2.getString(1);
 					}
					score = rs.getInt(4);
					reponse = rs.getString(5);
					System.out.printf("| %-6s | %-17s | %-28s | %-6s | %-50s |\n",numero,auteur,date,score,reponse);
					System.out.println("+--------+-------------------+------------------------------+--------+----------------------------------------------------+");
					}
				System.out.println();
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void ajouterReponse(int noQuest) {
		String reponse = lireLigne("Veuillez entrez votre réponse : ");
		try {
			ajouterReponse.setInt(1, id_utilisateur_connecte);
			ajouterReponse.setInt(2, noQuest);
			ajouterReponse.setString(3, reponse);
			try(ResultSet rs = ajouterReponse.executeQuery()){
				System.out.println("Réponse ajouté avec succès");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean verifierQuestion(int noQuest) {
		boolean existe = false;
		try {
			verifierQuestion.setInt(1, noQuest);
			try(ResultSet rs = verifierQuestion.executeQuery()){
				rs.next();
				existe = rs.getBoolean(1);
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return existe;
	}
	
	public boolean verifierTag(String tag) {
		boolean existe = false;
		try {
			verifierTag.setString(1, tag);
			try(ResultSet rs = verifierTag.executeQuery()){
				rs.next();
				existe = rs.getBoolean(1);
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return existe;
	}
	
	
	public boolean cloturerQuestion(int noQuest) {
		try {
			cloturerQuestion.setInt(1, noQuest);
			cloturerQuestion.setInt(2, id_utilisateur_connecte);
			try(ResultSet rs = cloturerQuestion.executeQuery()){
				System.out.println("Question cloturé avec succès");
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
		return true;
	}
	
	private int recupereNoReponse(int noQuest, int numeroRep) {
		int toReturn = -1;
		try {
			recupereNoReponse.setInt(1, noQuest);
			recupereNoReponse.setInt(2, numeroRep);
			try(ResultSet rs = recupereNoReponse.executeQuery()){
				rs.next();
				toReturn = rs.getInt(1); 
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
		return toReturn;
	}
	
	public void voterReponse(int noQuest) {
		int noRep;
		int numeroRep;
		do{
			numeroRep = lireEntier("Veuillez choisir la question pour laquelle vous souhaitez voter :");
			noRep = recupereNoReponse(noQuest,numeroRep);
		}while(noRep == -1);
		
		if(noRep != -1) {
			int choixVote;
			do {
				System.out.println("1) Vote positif");
				System.out.println("2) Vote négatif");
				choixVote = lireEntierEntre(1,2);
			}while(choixVote < 1 && choixVote > 2);
			boolean vote = choixVote == 1 ? true : false;
			
			try {
				voterReponse.setBoolean(1, vote);
				voterReponse.setInt(2, id_utilisateur_connecte);
				voterReponse.setInt(3, noRep);
				try(ResultSet rs = voterReponse.executeQuery()){
					System.out.println("Vote effectué avec succès !");
				}
			}catch(SQLException e) {
				System.out.println(e.getMessage());
			}
			
		}else {
			System.out.println("Vote échoué !");
		}
	}
	
	public void editerReponse(int noQuest){
		int numeroRep;
		int noRep;
		do{
			numeroRep = lireEntier("Veuillez choisir la question que vous souhaitez modifier :");
			noRep = recupereNoReponse(noQuest,numeroRep);
		}while(noRep == -1);
		try {
			modifierCorpsReponse.setInt(1, noRep);
			modifierCorpsReponse.setInt(2, id_utilisateur_connecte);
			viderBuffer();//vide le buffer car incompatibilité avec certaine lecture faite dans la méthode suivante
			modifierCorpsReponse.setString(3,lireLigne("Veuillez entrer le nouveau corps de la réponse : "));
			try (ResultSet rs = modifierCorpsReponse.executeQuery()){
				System.out.println("Modification éffectué avec succès !");
				System.out.println();
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	public void editerQuestion(int noQuest) {
		int choix;
		boolean termine = false;
		do {
			System.out.println("1) Modifier le corps de la question");
			System.out.println("2) Modifier le titre de la question");
			System.out.println("3) Modification terminées ");
			choix = lireEntierEntre(1,3);
			switch(choix) {
			case 1:
				modifierCorpsQuestion(noQuest);
				break;
			case 2:
				modifierTitreQuestion(noQuest);
				break;
			case 3:
				termine = true;
			}
		}while(!termine);
	}
	
	private void modifierTitreQuestion(int noQuest) {
		System.out.println("Modification du titre de la question "+noQuest+" : ");
		try {
			modifierTitreQuestion.setInt(1, noQuest);
			modifierTitreQuestion.setInt(2, id_utilisateur_connecte);
			modifierTitreQuestion.setString(3, lireLigne("Veuillez entrez le nouveau titre de votre question : "));
			try(ResultSet rs = modifierTitreQuestion.executeQuery()){
				System.out.println("Modification éffectué avec succès");
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void modifierCorpsQuestion(int noQuest) {
		System.out.println("Modification du corps de la question "+noQuest+" : ");
		try {
			modifierCorpsQuestion.setInt(1, noQuest);
			modifierCorpsQuestion.setInt(2, id_utilisateur_connecte);
			modifierCorpsQuestion.setString(3, lireLigne("Veuillez entrez le nouveau corps de votre question : "));
			try(ResultSet rs = modifierCorpsQuestion.executeQuery()){
				System.out.println("Modification éffectué avec succès");
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
}
