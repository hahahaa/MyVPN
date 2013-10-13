package vpn;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream;

public class DESEncoder {

	private Cipher ecipher;
	private Cipher dcipher;
	private SecretKey key;

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

	public String encrypt(String str) {
		try {
			byte[] utf8 = str.getBytes("UTF8");
			byte[] enc = ecipher.doFinal(utf8);
			enc = BASE64EncoderStream.encode(enc);
			return new String(enc);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String decrypt(String str) {
		try {
			byte[] dec = BASE64DecoderStream.decode(str.getBytes());
			byte[] utf8 = dcipher.doFinal(dec);
			return new String(utf8, "UTF8");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}