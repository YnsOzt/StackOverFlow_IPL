package app;

import static util.Util.lireEntier;
import static util.Util.lireLigne;
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
	
	private PreparedStatement desactiverCompte, voirUtilisateurs, verifierUtilisateur,
							  ameliorerStatus, ajouterTag, questionsPosee, questionsRepondu;
	
	public DBManager(Connection conn) {
		this.conn = conn;
		try {
			desactiverCompte = conn.prepareStatement(Querries.desactiverCompte);
			voirUtilisateurs = conn.prepareStatement(Querries.voirUtilisateurs);
			verifierUtilisateur = conn.prepareStatement(Querries.verifierUtilisateur);
			ajouterTag = conn.prepareStatement(Querries.ajouterTag);
			ameliorerStatus = conn.prepareStatement(Querries.ameliorerStatus);
			questionsPosee = conn.prepareStatement(Querries.questionsPosee);
			questionsRepondu = conn.prepareStatement(Querries.questionsRepondu);
		}catch(SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void voirUtilisateurs() {
		int id_user;
		String email;
		String pseudo;
		String status;
		int reputation;
		Boolean actif;
		try {
			System.out.println("+----------------+---------------------------+-----------------+------------+-----------------+-----------------+");
			System.out.printf("| %-14s | %-25s | %-15s | %-10s | %-15s | %-15s |\n","Id utilisateur","Email","Pseudo","status","Réputation","Actif");
			System.out.println("+----------------+---------------------------+-----------------+------------+-----------------+-----------------+");
			try(ResultSet rs = voirUtilisateurs.executeQuery()){
				while(rs.next()) {
					id_user = rs.getInt(1);
					email = rs.getString(2);
					pseudo = rs.getString(3);
					status = rs.getString(4);
					reputation = rs.getInt(5);
					actif = rs.getBoolean(6);
					System.out.printf("| %-14s | %-25s | %-15s | %-10s | %-15s | %-15s |\n",id_user,email,pseudo,status,reputation,actif);
					System.out.println("+----------------+---------------------------+-----------------+------------+-----------------+-----------------+");
				}
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private boolean verifierUtilisateur(int id) {
		boolean ret = false;
		try {
			verifierUtilisateur.setInt(1, id);
			try(ResultSet rs = verifierUtilisateur.executeQuery()){
				rs.next();
				ret = rs.getBoolean(1);
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public void desactiverCompteUtilisateur() {
		voirUtilisateurs();
		int choix;
		do {
			choix = lireEntier("Veuillez entrez l'id de l'utilisateur que vous avez séléctionner : ");
		}while(!verifierUtilisateur(choix));
			
		try {
			desactiverCompte.setInt(1, choix);
			try(ResultSet rs = desactiverCompte.executeQuery()){
				System.out.println("Le compte " + choix + " a bien été désactivé !");
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public void ameliorerStatusUtilisateur() {
		voirUtilisateurs();
		int choix;
		do {
			choix = lireEntier("Veuillez entrez l'id de l'utilisateur que vous avez séléctionner : ");
		}while(!verifierUtilisateur(choix));
		
		try {
			ameliorerStatus.setInt(1, choix);
			ameliorerStatus.setString(2, lireString("Entrez le rang [N/A/M] que vous souhaitez lui donner :"));
			try(ResultSet rs = ameliorerStatus.executeQuery()){
				rs.next();
				System.out.println("Le status de l'utilisateur "+choix+ " a bien été mis à jour");
			}
			
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public void creeTag() {
		try {
			String nomTag = lireString("Veuillez insérer le nom du tag à crée");
			ajouterTag.setString(1, nomTag);
			try (ResultSet rs = ajouterTag.executeQuery()){
				rs.next();
				System.out.println("Le tag "+ nomTag + " a bien été crée sous l'id "+rs.getString(1));
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void visualierQuestionsPose(int idUtilisateur, Timestamp dateDebut, Timestamp dateFin) {
		System.out.println("Voici les différentes questions que l'utilisateur "+idUtilisateur+ " a posées :");
	
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
			
			questionsPosee.setInt(1, idUtilisateur);
			questionsPosee.setTimestamp(2, dateDebut);
			questionsPosee.setTimestamp(3, dateFin);
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
	
	
	public void visualierQuestionsRepondu(int idUtilisateur, Timestamp dateDebut, Timestamp dateFin) {
		System.out.println("Voici les différentes questions auxquelles l'utilisateur"+ idUtilisateur + ": ");
	
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
			
			questionsRepondu.setInt(1, idUtilisateur);
			questionsRepondu.setTimestamp(2, dateDebut);
			questionsRepondu.setTimestamp(3, dateFin);
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
	
	public void voirHistorique() {
		String dateDebut, dateFin;
		voirUtilisateurs();
		int choix;
		do {
			choix = lireEntier("Veuillez entrez l'id de l'utilisateur que vous avez séléctionner : ");
		}while(!verifierUtilisateur(choix));
		viderBuffer();
		dateDebut = lireLigne("Veuillez entrez la date de début (YYYY-MM-DD) : ") + " 00:00:00";
		Timestamp tDebut = Timestamp.valueOf(dateDebut);
		dateFin = lireLigne("Veuillez entrez la date de fin (YYYY-MM-DD) : ")+ " 00:00:00";
		Timestamp tFin = Timestamp.valueOf(dateFin);;
		
		
		visualierQuestionsPose(choix, tDebut, tFin);
		
		visualierQuestionsRepondu(choix, tDebut, tFin);
		
	}
	
	
}
