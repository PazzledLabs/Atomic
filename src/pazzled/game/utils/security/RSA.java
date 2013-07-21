/*******************************************************************************
 * Copyright (c) 2013 venkat@pazzled.com.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     venkat@pazzled.com - Venkat
 ******************************************************************************/
package pazzled.game.utils.security;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pazzled.game.utils.common.Log;

import android.content.Context;
import android.util.Base64;

public class RSA {

	private static PublicKey publicKey;

	public static void InitializeKeyPair(Context context) {

		try {
			byte[] encodedPublicKey = new byte[1028];
			InputStream fn = context.getAssets().open("public.key");
			fn.read(encodedPublicKey);

			// Generate KeyPair.
			KeyFactory keyFactory = null;
			keyFactory = KeyFactory.getInstance("RSA");

			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
					encodedPublicKey);
			if (keyFactory != null)
				publicKey = keyFactory.generatePublic(publicKeySpec);

			/*
			 * File filePrivateKey = new File(context.getFilesDir() +
			 * "/private.key"); fis = new
			 * FileInputStream(filePrivateKey.getAbsolutePath()); byte[]
			 * encodedPrivateKey = new byte[(int) filePrivateKey.length()];
			 * fis.read(encodedPrivateKey); fis.close();
			 * 
			 * PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
			 * encodedPrivateKey); if (keyFactory != null) privateKey =
			 * keyFactory.generatePrivate(privateKeySpec);
			 */

		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static PublicKey getPublicKey() {
		return publicKey;
	}

	/*
	 * public static PrivateKey getPrivateKey() { return privateKey; }
	 */

	public static String Encrypt(final String plain) {

		PublicKey publicKey = getPublicKey();
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			int cipherBlockSize = cipher.getBlockSize();
			ByteArrayOutputStream bArrOut = new ByteArrayOutputStream();
			bArrOut.flush();
			int pos = 0;
			Log.i("ContentBufferLength", plain.length() + "");
			while (true) {
				if (cipherBlockSize > plain.length() - pos) {
					cipherBlockSize = plain.length() - pos;
				}
				Log.i("CipherBlockSize", cipherBlockSize + "");
				byte[] tmp = cipher.doFinal(plain.getBytes(), pos,
						cipherBlockSize);
				bArrOut.write(tmp);
				pos += cipherBlockSize;
				if (plain.length() <= pos) {
					break;
				}
			}
			bArrOut.flush();
			byte[] encryptedBuffer = bArrOut.toByteArray();
			bArrOut.close();

			return Base64.encodeToString(encryptedBuffer, Base64.DEFAULT);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * @TargetApi(Build.VERSION_CODES.GINGERBREAD) public static String
	 * Decrypt(final String encryptedString) {
	 * 
	 * PrivateKey privateKey = Utils.getPrivateKey(); Cipher cipher = null;
	 * String origString = ""; try { byte[] decryptedBytes = null; cipher =
	 * Cipher.getInstance("RSA/ECB/PKCS1Padding");
	 * cipher.init(Cipher.DECRYPT_MODE, privateKey); int cipherBlockSize =
	 * cipher.getBlockSize(); int pos = 0;
	 * 
	 * decryptedBytes = Base64.decode(encryptedString, Base64.DEFAULT); while
	 * (true) { if (pos == decryptedBytes.length) break; byte[] slice =
	 * Arrays.copyOfRange(decryptedBytes, pos, pos + cipherBlockSize); pos +=
	 * cipherBlockSize; origString += new String(cipher.doFinal(slice));
	 * 
	 * } } catch (NoSuchAlgorithmException e) { e.printStackTrace(); } catch
	 * (NoSuchPaddingException e) { e.printStackTrace(); } catch
	 * (InvalidKeyException e) { e.printStackTrace(); } catch
	 * (IllegalBlockSizeException e) { e.printStackTrace(); } catch
	 * (BadPaddingException e) { e.printStackTrace(); } return origString; }
	 */

}
