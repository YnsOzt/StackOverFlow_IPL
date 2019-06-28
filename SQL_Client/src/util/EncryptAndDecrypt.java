package util;
import securite.BCrypt;
public class EncryptAndDecrypt {
	
	public static String encrypt(String pswd) {
		return BCrypt.hashpw(pswd, BCrypt.gensalt());
	}
	
	public static boolean Decrypt(String pswd_clair, String pswd_hash) {
		if(pswd_clair == null)
			return false;
		return BCrypt.checkpw(pswd_clair, pswd_hash);
	}
}
