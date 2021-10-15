package impl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

public class CCMImpl {

//	public static void main(String[] args) throws IllegalStateException, InvalidCipherTextException {
//		int maxKeySize = 0;
//		try {
//			maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
////	    System.out.println("Max Key Size for AES : " + maxKeySize);
//		String s1=ccmaes(true, "hello", "test123", "0");
//		System.out.println(s1);
//		System.out.println(ccmaes(false, s1, "test123", "0"));
//	}

	static String exc;

	public final static String ccmaes(boolean encrypt, String plain, String key, String iv) {
		int i = 0;
		try {
			try {
				i = Integer.parseInt(iv);
			} catch (Exception e) {

			}

			byte[] iv_vector = new byte[13];
			byte[] key_byte = new byte[32];
			byte[] input_buffer = null;
			byte[] output_buffer = null;
			byte[] temp_iv = intToBytes(i);
			for (int val = 0; (val < temp_iv.length); val++) {
				iv_vector[val] = temp_iv[val];
			}

			byte[] temp_key = key.getBytes(StandardCharsets.US_ASCII);
			for (int val = 0; (val < temp_key.length); val++) {
				key_byte[val] = temp_key[val];
			}

			if ((encrypt == true)) {
				input_buffer = plain.getBytes(StandardCharsets.US_ASCII);
			} else {
				input_buffer = plain.getBytes();
			}

			CipherParameters param = new AEADParameters(new KeyParameter(key_byte), 64, iv_vector, new byte[0]);

			CCMBlockCipher cipher = new CCMBlockCipher(new AESEngine());
			cipher.init(encrypt, param);
			output_buffer = new byte[cipher.getOutputSize(plain.length())];
			int len = cipher.processBytes(input_buffer, 0, input_buffer.length, output_buffer, 0);
			cipher.doFinal(output_buffer, len);
			// Return Hex string for encryption
			if (encrypt) {
				return new String(output_buffer);
			}

			// Return string for decrypt

			String utf8EncodedString = new String(output_buffer, StandardCharsets.UTF_8);

			utf8EncodedString = utf8EncodedString.replace("\\0", "");
			return utf8EncodedString;

		} catch (Exception e) {
			exc = e.getMessage();
		}
		return exc;
	}

	private static byte[] intToBytes(final int i) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(i);
		return bb.array();
	}
	
	
	public static String encode(String key, String data) throws Exception {
		  Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		  SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
		  sha256_HMAC.init(secret_key);

		  return Hex.toHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
		}

}
