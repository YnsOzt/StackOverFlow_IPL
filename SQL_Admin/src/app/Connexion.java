package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connexion {
	Connection conn;
	
	public Connexion() {
		verifierDriver();
		connexion();
	}
	
	public  void verifierDriver() {
		try {
			Class.forName("org.postgresql.Driver");
		}catch(ClassNotFoundException e) {
			System.out.println("Driver PostgreSQL manquant !");
			System.exit(1);
		}
	}
	
	
	public Connection connexion() {
		String url= "jdbc:postgresql://localhost:5432/projet";
		conn=null;
		try {
		  conn=DriverManager.getConnection(url,"postgres","sakirozturk213");
		} catch (SQLException e) {
			System.out.println("Impossible de joindre le server !");
			System.exit(1);
		}
		return conn;
	}
	
	public Connection getConnexion() {
		return conn;
	}
}
