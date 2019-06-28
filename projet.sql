--CREATE TABLE
DROP SCHEMA IF EXISTS projet CASCADE;
CREATE SCHEMA projet;

CREATE TABLE projet.utilisateurs(
	id_utilisateur SERIAL PRIMARY KEY,
	email VARCHAR(50) NOT NULL UNIQUE CHECK (email SIMILAR TO '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}'),
	pseudo VARCHAR(20) NOT NULL UNIQUE CHECK (pseudo<>''),
	mot_de_passe VARCHAR(60) NOT NULL CHECK(mot_de_passe <> ''),
	status CHAR(1) NOT NULL CHECK(status IN ('N','A','M')) DEFAULT 'N',
	reputation INTEGER NOT NULL CHECK (reputation >= 0) DEFAULT 0,
	actif BOOLEAN NOT NULL DEFAULT('true')
);


CREATE TABLE projet.questions(
	no_question SERIAL PRIMARY KEY,
	titre VARCHAR(50) NOT NULL CHECK(titre <> '' AND titre <> ' '),
	corps VARCHAR(1000) NOT NULL CHECK(corps <> '' AND corps <> ' '),
	date_creation TIMESTAMP NOT NULL DEFAULT(NOW()),
	date_derniere_edition TIMESTAMP NULL CHECK(date_derniere_edition > date_creation),
	cloture BOOLEAN NOT NULL DEFAULT('false'),
	id_auteur_question INTEGER REFERENCES projet.utilisateurs (id_utilisateur) NOT NULL,
	id_editeur INTEGER REFERENCES projet.utilisateurs(id_utilisateur) NULL
);

CREATE TABLE projet.reponses(
	no_reponse SERIAL PRIMARY KEY,
	date_reponse TIMESTAMP NOT NULL DEFAULT( NOW() ),
	score INTEGER NOT NULL DEFAULT 0,
	id_auteur_reponse INTEGER REFERENCES projet.utilisateurs(id_utilisateur) NOT NULL,
	no_question INTEGER REFERENCES projet.questions(no_question) NOT NULL,
	reponse VARCHAR(200) NOT NULL CHECK(reponse <> '' AND reponse <> ' '),
	numero INTEGER NOT NULL CHECK(numero > 0)
);

CREATE TABLE projet.reponse_votes(
	type_vote BOOLEAN NOT NULL,
	date_vote TIMESTAMP NOT NULL DEFAULT( NOW() ),
	id_voteur INTEGER REFERENCES projet.utilisateurs(id_utilisateur) NOT NULL,
	no_reponse INTEGER REFERENCES projet.reponses(no_reponse) NOT NULL,
	PRIMARY KEY (id_voteur,no_reponse)
);

CREATE TABLE projet.tags(
	id_tag SERIAL PRIMARY KEY,
	intitule VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE projet.tags_questions(
	no_question INTEGER REFERENCES projet.questions(no_question) NOT NULL,
	id_tag INTEGER REFERENCES projet.tags (id_tag) NOT NULL,
	PRIMARY KEY (no_question, id_tag)
);

-- fonctions 


CREATE OR REPLACE FUNCTION projet.verifier_no_question_valide(INTEGER) RETURNS BOOLEAN as $$
DECLARE
	no_quest ALIAS FOR $1;
BEGIN
	IF NOT EXISTS(SELECT * FROM projet.questions WHERE no_question = no_quest AND cloture = 'false')THEN
		RETURN 'false';
	END IF;
	return 'true';
END;

$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION projet.verifier_tag_existant(VARCHAR(10)) RETURNS BOOLEAN as $$
DECLARE
	alias_tag ALIAS FOR $1;
BEGIN
	IF NOT EXISTS(SELECT * FROM projet.tags WHERE intitule = alias_tag)THEN
		RETURN 'false';
	END IF;
	return 'true';
END;

$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION projet.verifier_no_utilisateur_valide(INTEGER) RETURNS BOOLEAN AS $$
DECLARE
	id_user ALIAS FOR $1;
BEGIN
	IF NOT EXISTS(SELECT * FROM projet.utilisateurs WHERE id_utilisateur = id_user) THEN
		RETURN 'false';
	END IF;

	RETURN 'true';
END;

$$ LANGUAGE plpgsql;

--fonction rendre inactif le compte de l'utilisateur
--integer -> id du compte à changer
CREATE OR REPLACE FUNCTION projet.changer_actif_utilisateur(INTEGER) RETURNS BOOLEAN AS $$
DECLARE
	utilisateur ALIAS FOR $1;
	
	actif_utlisateur_actuel BOOLEAN;
BEGIN 
	
	SELECT actif FROM projet.utilisateurs WHERE id_utilisateur = utilisateur INTO actif_utlisateur_actuel;

	--exception si le compte est déjà dans l'état souhaité
	IF(actif_utlisateur_actuel = 'false')THEN
		RAISE 'le compte est déjà désactivé';
	END IF;


		
	UPDATE projet.utilisateurs SET actif= 'false' WHERE id_utilisateur = utilisateur;
		
	RETURN 'true';
		
END;

$$ LANGUAGE plpgsql;

--Fonction encoder un utilisateur :: return l'id de l'utilisateur crée
CREATE OR REPLACE FUNCTION projet.cree_utilisateur(VARCHAR(50), VARCHAR(20), VARCHAR(20)) RETURNS INTEGER AS $$
DECLARE 
	mail ALIAS FOR $1;
	login ALIAS FOR $2;
	mdp ALIAS FOR $3;
	id_user INTEGER; 
BEGIN 
	-- Selection du l'user via son pseudo (existent ?)
	IF((SELECT u.pseudo FROM projet.utilisateurs u WHERE u.pseudo = login) != NULL)THEN
		RAISE 'utilisateur déja présent';
	END IF;

	--Création de l'utilisateur dans le cas contraire
	INSERT INTO projet.utilisateurs (email, pseudo,mot_de_passe) VALUES (mail, login, mdp) RETURNING id_utilisateur INTO id_user;

	RETURN id_user;

END; 
$$ LANGUAGE plpgsql;

-- fonction ajouter une nouvelle question
CREATE OR REPLACE FUNCTION projet.cree_question(VARCHAR(50), VARCHAR(1000), INTEGER) RETURNS INTEGER AS $$
DECLARE 
	titre_one ALIAS FOR $1;
	corps_one ALIAS FOR $2;
	id_auteur ALIAS FOR $3;
	nr_question INTEGER;

BEGIN 
	--
	INSERT INTO projet.questions (titre, corps, id_auteur_question) VALUES (titre_one, corps_one, id_auteur) RETURNING no_question INTO nr_question;

	RETURN nr_question;

END;
$$ LANGUAGE plpgsql;

--fonction qui vérifie les informations de connexion de l'utilisateur
CREATE OR REPLACE FUNCTION projet.connecter_utilisateur(VARCHAR(20)) RETURNS INTEGER AS $$
DECLARE
	login ALIAS FOR $1;
	id_user INTEGER;
	is_utlisateur_actif BOOLEAN;
BEGIN
	SELECT U.id_utilisateur, U.actif FROM projet.utilisateurs U WHERE U.pseudo = login INTO id_user,is_utlisateur_actif;
	IF(id_user = null) THEN
		RAISE 'Vos données sont incorrectes';
	ELSEIF(is_utlisateur_actif = 'false') THEN
		RAISE 'Cet utilisateur n''est pas actif';
	END IF;

	RETURN id_user;

END;
$$ LANGUAGE plpgsql;

--fonction qui récupère le mot de passe de l'utilisateur en paramètre
CREATE OR REPLACE FUNCTION projet.recupere_mot_de_passe(VARCHAR(60)) RETURNS VARCHAR(60) as $$
DECLARE
	login ALIAS FOR $1;
	id_user INTEGER;
	mdp VARCHAR(60);
BEGIN
	SELECT U.id_utilisateur, U.mot_de_passe FROM projet.utilisateurs U WHERE U.pseudo = login INTO id_user,mdp;
	IF(id_user = null) THEN
		RAISE 'Cet utilisateur n''existe pas';
	END IF;

	return mdp;
END;
$$ LANGUAGE plpgsql;


-- fonction introduire une nouvelle réponse à une question
CREATE OR REPLACE FUNCTION projet.creer_reponse(INTEGER, INTEGER, VARCHAR(200)) RETURNS INTEGER AS $$
DECLARE 
	id_createur ALIAS FOR $1;
	nr_question ALIAS FOR $2;
	txt ALIAS FOR $3;
	
	numb INTEGER;
	no_rep INTEGER;
	
BEGIN 
	
	-- Vérifier que la question n'est pas cloturé
	IF((SELECT Q.cloture FROM projet.questions Q WHERE Q.no_question = nr_question) = 'true') THEN
		RAISE 'Cette question est cloturé, vous ne pouvez plus réponse à celle ci';
	END IF;
	

	-- Selectionner le numero actuel dans le table des réponses

	IF( (SELECT COUNT(*) FROM projet.reponses WHERE no_question = nr_question) < 1)THEN
		numb:=0;
	ELSE
		SELECT MAX(numero) FROM projet.reponses WHERE no_question = nr_question INTO numb;
	END IF;
	
	-- Insérer 
	INSERT INTO projet.reponses VALUES (DEFAULT, DEFAULT, DEFAULT, id_createur, nr_question, txt, numb+1)  RETURNING no_reponse INTO no_rep;
	
	RETURN no_rep;
	
END;

$$ LANGUAGE plpgsql;

-- fonction introduire un vote pour une réponse

CREATE OR REPLACE FUNCTION projet.inserer_vote(BOOLEAN ,INTEGER, INTEGER) RETURNS BOOLEAN AS $$
DECLARE 
	type_v ALIAS FOR $1;
	voteur ALIAS FOR $2;
	num_rep ALIAS FOR $3;

	auteur_reponse INTEGER;
	status_voteur CHAR(1);
	date_dernier_vote TIMESTAMP;
BEGIN 
	--récupère le status de l'utilisateur 
	SELECT status FROM projet.utilisateurs WHERE id_utilisateur = voteur INTO status_voteur;

	SELECT id_auteur_reponse FROM projet.reponses WHERE no_reponse = num_rep INTO auteur_reponse;

	IF(auteur_reponse = voteur) THEN
		RAISE 'Vous ne pouvez pas voter pour votre propre réponse';
	END IF;

	IF(status_voteur = 'N') THEN
		RAISE 'Le voteur n''as pas le status suffisant pour voter';
	ELSEIF(type_v = 'false' AND status_voteur = 'A') THEN
		RAISE 'Le voteur n''as pas le status suffisant pour faire un vote négatif';
	END IF;

	--on récupère la date du dernier vote
	IF(status_voteur = 'A')THEN
		SELECT MAX (date_vote) FROM projet.reponse_votes WHERE id_voteur = voteur INTO date_dernier_vote;
		IF( date_dernier_vote > NOW() - INTERVAL '24 hours'  )THEN
			RAISE 'L''utilisateur ne peut voter car l''interval de 24 heures n''est pas respecté'; 
		END IF;
	END IF;

	

	INSERT INTO projet.reponse_votes VALUES (type_v, NOW(), voteur, num_rep);


	RETURN 'true';

END;

$$ LANGUAGE plpgsql;

--fonction qui recupère le pseudo correspondant à l'Id fourni en paramètre
CREATE OR REPLACE FUNCTION projet.recupere_pseudo(INTEGER) RETURNS VARCHAR(20) AS $$
DECLARE
	id_user ALIAS FOR $1;
	pseudo_utilisateur VARCHAR(20);
BEGIN
	SELECT pseudo FROM projet.utilisateurs WHERE id_utilisateur = id_user INTO pseudo_utilisateur;

	IF(pseudo_utilisateur = NULL)THEN
		raise 'Cet utilisateur n''éxiste pas';
	END IF;

	RETURN pseudo_utilisateur;
END;

$$ LANGUAGE plpgsql;

-- Fonction pour rajouter un tag à une question 

CREATE OR REPLACE FUNCTION projet.ajouter_tag_question(INTEGER,INTEGER, VARCHAR(10) ) RETURNS INTEGER AS $$ 
DECLARE 
	utilisateur ALIAS FOR $1;
	question ALIAS FOR $2;
	tag ALIAS FOR $3;

	cloture_question BOOLEAN;
	tag_ INTEGER;
	tag_two INTEGER;
	auteur INTEGER;
	tag_id INTEGER;
	tag_a_return INTEGER;

BEGIN 

-- Vérifier que l'auteur de la question est bien l'utilisateur 
	SELECT quest.id_auteur_question FROM  projet.questions quest WHERE quest.no_question = question INTO auteur; 
	IF( auteur != utilisateur ) THEN
		RAISE 'On ne peut pas ajouter un tag à une question qui ne nous appartient pas';
	END IF;


-- Vérifier que le tag entré correspond bien à un tag existant
	IF NOT EXISTS(SELECT * FROM projet.tags WHERE intitule = tag)THEN
		RAISE 'Tag inexistant';
	END IF;

-- Vérifier que la question n'est pas cloturé
	IF((SELECT Q.cloture FROM projet.questions Q WHERE Q.no_question = question) = 'true') THEN
		RAISE 'Cette question est cloturé, vous ne pouvez pas ajouter de tag à celle ci';
	END IF;

-- Vérifier si il y a bien moins de 5 tags pour cette question
	IF((SELECT COUNT(tq.id_tag) FROM projet.tags_questions tq WHERE tq.no_question = question) > 4) THEN
		RAISE 'Le nombre de tag maximum présent pour cette question est déja atteind';
	END IF;

-- Récuperer l'id du tag en fonction du tag en paramètre 
	SELECT tg.id_tag FROM projet.tags tg WHERE tg.intitule = tag INTO tag_two;


-- Vérifier que le tag n'est pas déja utilisé pour cette question 
	SELECT tq.id_tag FROM projet.tags_questions tq WHERE tq.no_question = question AND tq.id_tag = tag_two INTO tag_;
		IF(tag_two = NULL ) THEN RAISE 'Ce tag est déja utilisé pour cette question';
	END IF;

-- Si les tests sont ok 
	INSERT INTO projet.tags_questions VALUES (question, tag_two) RETURNING id_tag INTO tag_a_return; 

	RETURN tag_a_return;

END; 
$$ LANGUAGE plpgsql;


--cloture la question correspondant a l'id fourni en paramètre
CREATE OR REPLACE FUNCTION projet.cloturer_question(INTEGER,INTEGER) RETURNS BOOLEAN AS $$ 
DECLARE
	num_quest ALIAS FOR $1;
	id_auteur_cloture ALIAS FOR $2;

	status_aut_cloture CHAR(1);
BEGIN
	SELECT status FROM projet.utilisateurs WHERE id_utilisateur = id_auteur_cloture INTO status_aut_cloture;
	IF('M' <> status_aut_cloture)THEN
		RAISE 'Vous n''avez pas le status n''écessaire pour cloturer une question';
	END IF;


	IF NOT EXISTS(SELECT * FROM projet.questions WHERE no_question = num_quest)THEN
		RAISE 'Ce numéro de question ne correspond à aucune question';
	END IF;

	IF('true' = (SELECT cloture FROM projet.questions WHERE no_question = num_quest)) THEN
		RAISE 'Cette question est déjà cloturé';
	END IF;

	UPDATE projet.questions
	SET cloture = 'true'
	WHERE no_question = num_quest;

	RETURN 'true';
END;
$$ LANGUAGE plpgsql;


--fonction qui récupère la PK de la réponse(numéro en param) de la question(numéro en param)
CREATE OR REPLACE FUNCTION projet.recupere_no_reponse(INTEGER, INTEGER) RETURNS INTEGER AS $$
DECLARE
	no_quest ALIAS FOR $1;
	no ALIAS FOR $2;

	no_rep INTEGER;
BEGIN
	

	IF NOT EXISTS(SELECT no_reponse FROM projet.reponses WHERE no_question = no_quest AND numero = no) THEN
		RAISE 'Cette réponse n''existe pas';
	END IF;

	SELECT no_reponse FROM projet.reponses WHERE no_question = no_quest AND numero = no INTO no_rep;

	RETURN no_rep;
END;
$$ LANGUAGE plpgsql;


--fonction qui permet de modifier le corps de la question
CREATE OR REPLACE FUNCTION projet.modifier_corps_question(INTEGER, INTEGER, VARCHAR(1000)) RETURNS VOID AS $$
DECLARE
	no_quest ALIAS FOR $1;
	no_editeur ALIAS FOR $2;
	nouv_corps ALIAS FOR $3;

	status_editeur CHAR(1);
	id_aut_quest INTEGER;
BEGIN
	SELECT status FROM projet.utilisateurs WHERE id_utilisateur = no_editeur INTO status_editeur;
	SELECT id_auteur_question FROM projet.questions WHERE no_question = no_quest INTO id_aut_quest;
	IF('N' = status_editeur AND no_editeur != id_aut_quest)THEN
		RAISE 'Vous n''avez pas le rang nécessaire pour modifier la question des autres';
	END IF;

	UPDATE projet.questions
	SET corps = nouv_corps , id_editeur = no_editeur, date_derniere_edition = NOW()
	WHERE no_question = no_quest;

	RETURN;
END;

$$ LANGUAGE plpgsql;

--fonction qui permet de modifier le titre de la question
CREATE OR REPLACE FUNCTION projet.modifier_titre_question(INTEGER, INTEGER, VARCHAR(1000)) RETURNS VOID AS $$
DECLARE
	no_quest ALIAS FOR $1;
	no_editeur ALIAS FOR $2;
	nouv_titre ALIAS FOR $3;

	status_editeur CHAR(1);
	id_aut_quest INTEGER;
BEGIN
	SELECT status FROM projet.utilisateurs WHERE id_utilisateur = no_editeur INTO status_editeur;
	SELECT id_auteur_question FROM projet.questions WHERE no_question = no_quest INTO id_aut_quest;
	IF('N' = status_editeur AND no_editeur != id_aut_quest)THEN
		RAISE 'Vous n''avez pas le rang nécessaire pour modifier la question des autres';
	END IF;

	UPDATE projet.questions
	SET titre = nouv_titre , id_editeur = no_editeur, date_derniere_edition = NOW()
	WHERE no_question = no_quest;

	RETURN;
END;
$$ LANGUAGE plpgsql;


--fonction qui permet de modifier le corps de la réposne
CREATE OR REPLACE FUNCTION projet.modifier_corps_reponse(INTEGER,INTEGER,VARCHAR(200)) RETURNS VOID AS $$
DECLARE
	no_rep ALIAS FOR $1;
	no_editeur ALIAS FOR $2;
	nouv_corps ALIAS FOR $3;

	status_editeur CHAR(1);
	id_aut_rep INTEGER;
BEGIN
	SELECT status FROM projet.utilisateurs WHERE id_utilisateur = no_editeur INTO status_editeur;
	SELECT id_auteur_reponse FROM projet.reponses WHERE no_reponse = no_rep INTO id_aut_rep;
	IF('N' = status_editeur AND no_editeur != id_aut_rep)THEN
		RAISE 'Vous n''avez pas le rang nécessaire pour modifier la question des autres';
	END IF;

	UPDATE projet.reponses
	SET reponse = nouv_corps
	WHERE no_reponse = no_rep;

	RETURN;
END;
$$ LANGUAGE plpgsql;



-- Fonction amiliorer statut utilisateur 

CREATE OR REPLACE FUNCTION projet.ameliorer_statut(INTEGER, CHAR(1)) RETURNS VOID AS $$
DECLARE 
	id_user ALIAS FOR $1; 
	futur_status ALIAS FOR $2; 

	status_actuel CHAR(1);

BEGIN 
	-- selectionner le statut actuel de l'utilisateur 
	SELECT status FROM projet.utilisateurs WHERE id_utilisateur = id_user INTO status_actuel;

	-- Vérifier le statut étant inférieur à celui encodé 
	IF(futur_status = 'N') THEN RAISE 'Vous ne pouvez pas réduire le statut de l''utilisateur';
	ELSEIF(futur_status = status_actuel )THEN RAISE 'Vous devez changer le statut par un autre statut supérieur';
	ELSEIF(status_actuel = 'M' AND futur_status = 'A') THEN RAISE 'Vous ne pouvez pas réduire le status';
	END IF;

	UPDATE projet.utilisateurs
	SET status = futur_status
	WHERE id_utilisateur = id_user;

	RETURN;
END; 

$$ LANGUAGE plpgsql;


-- Fonction ajouter un tag en tant qu'administrateur

CREATE OR REPLACE FUNCTION projet.ajouter_tag(VARCHAR(10)) RETURNS INTEGER AS $$
DECLARE 
	nom ALIAS FOR $1; 

	tag INTEGER;

-- Vérifier que le tag entrer n'est pas déja présent dans la DB 

BEGIN 
	IF EXISTS (SELECT id_tag FROM projet.tags WHERE intitule = nom) THEN RAISE 'l''intitule existe déja';
	END IF; 

	INSERT INTO projet.tags (id_tag,intitule) VALUES (DEFAULT, nom) RETURNING id_tag INTO tag; 

	RETURN tag; 

END; 
$$ LANGUAGE plpgsql;


--triggers

--trigger maj réputation ,le status  et le score de la question
CREATE OR REPLACE FUNCTION projet.maj_reputaion_status_scoreQuestion() RETURNS TRIGGER AS $$
DECLARE
	ancienne_reputation INTEGER;
	nouvelle_reputation INTEGER;
	id_utilisateur_a_maj INTEGER;
	satus_utilisateur_a_maj CHAR(1);
	score_reponse INTEGER;
BEGIN
	--récupère le score de la question
		SELECT R.score
		FROM projet.reponses R
		WHERE R.no_reponse = NEW.no_reponse
		INTO score_reponse;


	IF(NEW.type_vote = 'true')
	THEN
		--récupère l'id de l'utiisateur à mettre à jour
		SELECT R.id_auteur_reponse
		FROM projet.reponses R
		WHERE R.no_reponse = NEW.no_reponse
		INTO id_utilisateur_a_maj;

		--récupère l'ancienne réputation de l'utilisateur à maj
		SELECT U.reputation
		FROM projet.utilisateurs U
		WHERE U.id_utilisateur = id_utilisateur_a_maj
		INTO ancienne_reputation;

		nouvelle_reputation := ancienne_reputation + 5;

		--récupère le status de l'utilisateur à maj
		SELECT U.status
		FROM projet.utilisateurs U
		WHERE U.id_utilisateur = id_utilisateur_a_maj
		INTO satus_utilisateur_a_maj;

		--check le status de l'utilisateur et le màj si nécessaire
		IF( (satus_utilisateur_a_maj = 'A') AND (nouvelle_reputation > 100) ) THEN
			satus_utilisateur_a_maj = 'M';
		ELSEIF( (satus_utilisateur_a_maj = 'N') AND (nouvelle_reputation > 50)) THEN
			satus_utilisateur_a_maj = 'A';
		END IF;		


		--met à jour tous les informations de l'utilisateur
		UPDATE projet.utilisateurs
		SET reputation = nouvelle_reputation , status = satus_utilisateur_a_maj
		WHERE id_utilisateur = id_utilisateur_a_maj;

		--on incrémente le compteur car la question a eu un vote positif
		score_reponse := score_reponse + 1;

	ELSE
		--on décrémente le compteur car la question a eu un vote négatif
		score_reponse := score_reponse - 1;
	END IF;

	--met à jour le score de la question
	UPDATE projet.reponses
	SET score = score_reponse
	WHERE no_reponse = NEW.no_reponse;
		
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_reputation_satus_scoreQuestion
AFTER INSERT ON projet.reponse_votes FOR EACH ROW
EXECUTE PROCEDURE projet.maj_reputaion_status_scoreQuestion();


--VIEWS
--views pour les différentes questions que l'utilisateur a rédigé
CREATE VIEW projet.questions_pose AS
	SELECT U1.pseudo as "pseudo auteur question",Q.no_question as "numero question",Q.titre as "titre question"
	,Q.date_creation as "date creation", U2.pseudo as "pseudo de l'éditeur",
	Q.date_derniere_edition as "date dernière edition",Q.cloture as "cloture", Q.corps as "question",
	 U1.id_utilisateur AS "id_auteur"

	FROM projet.utilisateurs U1 LEFT OUTER JOIN projet.questions Q ON Q.id_auteur_question = U1.id_utilisateur
	 LEFT OUTER JOIN projet.utilisateurs U2 ON Q.id_editeur = U2.id_utilisateur
	WHERE Q.no_question IS NOT NULL

	ORDER BY Q.date_creation;

--views qui séléctionne les questions concernant un tag
CREATE VIEW projet.voir_questions_tag AS
	SELECT DISTINCT U1.pseudo AS "pseudo_createur", Q.no_question AS "no_question", Q.titre AS "titre"
							, Q.date_creation AS "date_creation", U2.pseudo AS "pseudo_editeur",
							 Q.date_derniere_edition AS "date_derniere_edition", T.intitule AS "tag", Q.cloture AS "cloture"
							 , Q.corps as "question"
	FROM  projet.tags_questions TG, projet.tags T, projet.tags_questions TQ,projet.utilisateurs U1,projet.questions Q LEFT OUTER JOIN
	projet.utilisateurs U2 ON Q.id_editeur = U2.id_utilisateur
	WHERE TG.no_question = Q.no_question AND U1.id_utilisateur = Q.id_auteur_question
		  AND Q.no_question IS NOT NULL AND T.id_tag = TQ.id_tag AND TQ.no_question = Q.no_question
	ORDER BY Q.date_creation;

--view qui affiche les questions auxquelles l'utilisateur a repondu
CREATE VIEW projet.voir_questions_repondu AS 
	SELECT DISTINCT U1.pseudo as "pseudo auteur question",Q.no_question as "numero question",Q.titre as "titre question"
	,Q.date_creation as "date creation", U2.pseudo as "pseudo de l'éditeur", Q.date_derniere_edition as "date dernière edition",
	U3.id_utilisateur as "id auteur reponse", Q.corps as "question"
	FROM projet.utilisateurs U1, projet.reponses R , projet.utilisateurs U3, projet.questions Q
	LEFT OUTER JOIN projet.utilisateurs U2 ON Q.id_editeur = U2.id_utilisateur
	WHERE Q.id_auteur_question = U1.id_utilisateur AND Q.no_question IS NOT NULL AND R.no_question = Q.no_question AND R.id_auteur_reponse = U3.id_utilisateur
	ORDER BY Q.date_creation;

--view qui affiche tous les tags
CREATE VIEW projet.voir_tags AS
	SELECT intitule FROM projet.tags;

--view qui affiche toute les réponses des questions
CREATE VIEW projet.voir_reponse_question AS
	SELECT R.numero, R.date_reponse, R.id_auteur_reponse, R.score, R.reponse, R.no_question as "no_question"
	FROM projet.reponses R
	ORDER BY R.score DESC, R.date_reponse;


CREATE VIEW projet.voir_les_utilisateurs AS
	SELECT U.id_utilisateur AS "Id Utilisateur", U.email AS "Email", U.pseudo AS "Pseudo", U.status AS "Status",
	U.reputation as "Reputation", U.actif AS "Actif"
	FROM projet.utilisateurs U
	ORDER BY U.id_utilisateur;

