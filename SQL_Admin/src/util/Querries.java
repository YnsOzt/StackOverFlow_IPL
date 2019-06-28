package util;

public class Querries {
	public static final String voirUtilisateurs = "SELECT * FROM projet.voir_les_utilisateurs;";
	
	public static final String desactiverCompte = "SELECT projet.changer_actif_utilisateur(?);";
	
	public static final String verifierUtilisateur = "SELECT projet.verifier_no_utilisateur_valide(?);";

	public static final String ameliorerStatus = "SELECT projet.ameliorer_statut(?, ?);";
	
	public static final String ajouterTag = "SELECT projet.ajouter_tag(?);";
	
	public static final String questionsPosee = "SELECT * FROM projet.questions_pose WHERE \"id_auteur\" = (?) AND \"date creation\" BETWEEN ? AND ?";
	public static final String questionsRepondu = "SELECT * FROM projet.voir_questions_repondu WHERE \"id auteur reponse\" = (?) AND \"date creation\" BETWEEN ? AND ?;";
	
}
