package vpn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DESEncoder {

	private Cipher ecipher;
	private Cipher dcipher;
	private SecretKey key;

	/**
	 * Create a DES encoder/decoder with a given key
	 * @param keyStr Key size needs at least 8 characters
	 */
	public DESEncoder (String keyStr) {
		try {			
			ecipher = Cipher.getInstance("DES");
			dcipher = Cipher.getInstance("DES");

			DESKeySpec dks = new DESKeySpec(keyStr.getBytes());
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
			key = skf.generateSecret(dks);
			
			ecipher.init(Cipher.ENCRYPT_MODE, key);
			dcipher.init(Cipher.DECRYPT_MODE, key);
			
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	public String encrypt(String str) throws BadPaddingException {
		    byte[] utf8;
			try {
				utf8 = str.getBytes("UTF8");
				byte[] enc = ecipher.doFinal(utf8);
			    return new sun.misc.BASE64Encoder().encode(enc);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			}
		return null;
	}

	public String decrypt(String str) throws BadPaddingException {
		    byte[] dec;
			try {
				dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
			    byte[] utf8 = dcipher.doFinal(dec);
			    return new String(utf8, "UTF8");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			}	
		return null;
	}
	
	public static void main(String argp[]) throws BadPaddingException {
		// testing
		String plain = "hahahaha";
		String key = "hahahah";
		DESEncoder d = new DESEncoder(key);
		String encrypt = d.encrypt(plain);
		String decrypt = d.decrypt(encrypt);
		
		System.out.println("plain: " + plain);
		System.out.println("key: " + key);
		System.out.println("encrypt: " + encrypt);
		System.out.println("decrypt: " + decrypt);		
	}
}