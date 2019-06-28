package util;

public class Querries {
	public static final String seConnecter = "SELECT projet.connecter_utilisateur(?);";
	public static final String recupMDP = "SELECT projet.recupere_mot_de_passe(?);";
	public static final String recuperePseudoUtilisateur = "SELECT projet.recupere_pseudo(?)";
	public static final String recupereNoReponse = "SELECT projet.recupere_no_reponse(?, ?)";
	
	public static final String inscrireUtilisateur = "SELECT projet.cree_utilisateur(?, ?, ?);";
	
	
	public static final String verifierQuestion = "SELECT projet.verifier_no_question_valide(?);";
	public static final String verifierTag = "SELECT projet.verifier_tag_existant(?);";
	
	
	public static final String questionsPosee = "SELECT * FROM projet.questions_pose WHERE \"pseudo auteur question\" = (?)";
	public static final String questionsRepondu = "SELECT * FROM projet.voir_questions_repondu WHERE \"id auteur reponse\" = (?);";
	public static final String touteQuestionsPosee = "SELECT * FROM projet.questions_pose WHERE \"cloture\" = 'false'";
	public static final String questionsAvecTags = "SELECT * FROM projet.voir_questions_tag WHERE \"tag\" = (?) AND \"cloture\" = 'false'";
	public static final String voirTags = "SELECT * FROM projet.voir_tags";
	public static final String voirReponses = "SELECT * FROM projet.voir_reponse_question WHERE \"no_question\"=(?) ";
	
	public static final String introductionQuestion = "SELECT projet.cree_question(?, ?, ?);";
	
	public static final String ajouterTagsQuestion = "SELECT projet.ajouter_tag_question(?, ?, ?)";

	public static final String ajouterReponse = "SELECT projet.creer_reponse(?, ?, ?);";

	public static final String cloturerQuestion = "SELECT projet.cloturer_question(?,?);";
	
	public static final String voterReponse = "SELECT projet.inserer_vote(? ,?, ?);";
	
	public static final String modifierCorpsReponse = "SELECT projet.modifier_corps_reponse(?,?,?);";
	
	public static final String modifierTitreQuestion = "SELECT projet.modifier_titre_question(?,?,?);";
	
	public static final String modfifierCorpsQuestion = "SELECT projet.modifier_corps_question(?, ?, ?);";
}
