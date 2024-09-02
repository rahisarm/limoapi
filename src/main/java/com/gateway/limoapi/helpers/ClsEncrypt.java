package com.gateway.limoapi.helpers;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec; 
public final class ClsEncrypt {
	
	
	private static ClsEncrypt instance = new ClsEncrypt(); 
	
	private Cipher cipher;
	private Key key;
	
//	private final BASE64Encoder b64Encoder = new BASE64Encoder();
//	private final Base64 b64Decoder = Base64.;
	
	public static ClsEncrypt getInstance() {
		
		return ClsEncrypt.instance;
	}	
	
	public ClsEncrypt() {
		
		try {
			this.cipher = Cipher.getInstance("AES");
			byte[] raw = { (byte) 0xA5, (byte) 0x01, (byte) 0x7B, (byte) 0xE5,
					(byte) 0x23, (byte) 0xCA, (byte) 0xD4, (byte) 0xD2,
					(byte) 0xC6, (byte) 0x5F, (byte) 0x7D, (byte) 0x8B,
					(byte) 0x0B, (byte) 0x9A, (byte) 0x3C, (byte) 0xF1 };
			this.key = new SecretKeySpec(raw, "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}
	
	public String encrypt(String aData) {
		String result = "";
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] utf8 = aData.getBytes("UTF8");
			byte[] encryptedData = cipher.doFinal(utf8);
			System.out.println(Base64.getEncoder().encode(encryptedData).toString());
			result = Base64.getEncoder().encodeToString(encryptedData);//this.b64Encoder.encode(encryptedData);
			
		} 
		catch (InvalidKeyException oException) { 			oException.printStackTrace(); } 
		catch (IllegalBlockSizeException oException) { 		oException.printStackTrace(); } 
		catch (BadPaddingException oException) { 			oException.printStackTrace(); } 
		catch (IOException oException) { 					oException.printStackTrace(); }
		return result;
	}
	
	public String decrypt(String aData) {
		String result = "";
			try {
				//System.out.println("==aData====="+aData.length());
				cipher.init(Cipher.DECRYPT_MODE, key);
				/*byte[] decodedData = Base64.decode(aData);//this.b64Decoder.decodeBuffer(aData);*/
				byte[] decodedData = Base64.getDecoder().decode(aData.getBytes("UTF-8"));
				//new Base64().decode(encryptedValue.getBytes())
				//System.out.println("===decodedData====="+decodedData.toString());
				byte[] utf8 = cipher.doFinal(decodedData);
				result = new String(utf8, "UTF8");
				//System.out.println("===result===="+result);
				
				
				
			} 
			catch (InvalidKeyException oException) { 			oException.printStackTrace(); } 
			 
			catch (IllegalBlockSizeException oException) { 		oException.printStackTrace(); } 
			catch (BadPaddingException oException) { 			oException.printStackTrace(); } 
			catch (UnsupportedEncodingException oException) { 	oException.printStackTrace(); }
		return result;
	}
	
	
	public  String txtCode(char[] input)
    {
        String algorithm = "SHA";
        String strRet = new String("");
                // Go through each character
                for (int i = 0; i < input.length; i++)
                {
                        strRet += input[i];
                }
        String password=strRet;
        //input.toString();
        Arrays.fill(input,'0');
        
        byte[] plainText = password.getBytes();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        md.reset();
        md.update(plainText);
        byte[] encodedPassword = md.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < encodedPassword.length; i++) {
            if ((encodedPassword[i] & 0xff) < 0x10) {
                sb.append("0");
            }
            sb.append(Long.toString(encodedPassword[i] & 0xff, 16));
        }
        return sb.toString();
}
	
	
	
}