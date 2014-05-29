package com.magic.nweb;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 * @version 1.0
 * @since 2014年5月29日 下午2:07:46
 */
public abstract class DigestUtils {

	private static final String MD5_ALGORITHM_NAME = "MD5";

	private static final char[] HEX_CHARS =
			{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	public static String sign(String src) {
		try {
			return novaSign(md5DigestAsHex(src.getBytes("UTF-8")), "");
		} catch (UnsupportedEncodingException e) {
			//ignore
		}
		return "";
	}
	
	private static String novaSign(String device, String md5str) {
		String shortStr = "BAB%#*#$AF!@$EHJG12D!$$%FDBNSDPF";
		String cKeyLongStr = "ewqruoirjlkvjD@SFASDFRRQEF#$QEFQRVBPIHGY~DGHYTU+_)_9SGAR#Q$#@DFGTHETR$#QEDWRET*^$UJNJULO*&IKUJYHTGRFEDS*(&MUNb;pqfwqla;bngs;aw-bpewdewwj6&*%$^&3789221";
		String resultStr = "";
		String cSourceStr = device + shortStr + md5str;
		int len = cSourceStr.length();
		int totalSum = 0;
		int mod = cKeyLongStr.length();
		for (int i = 0; i < len; i++) {
			totalSum += cSourceStr.charAt(i);
		}
		for (int i = 0; i < len; i++) {
			int value = cSourceStr.charAt(i);
			int geWei = value % 10;
			int shiWei = (value / 10) % 10;
			int encodeValue = cKeyLongStr.charAt(value) + cKeyLongStr.charAt(geWei)
					* cKeyLongStr.charAt(cKeyLongStr.length() - shiWei - 1);
			int baiWei, qianWei;
			geWei = encodeValue % 10;
			shiWei = (encodeValue / 10) % 10;
			baiWei = (encodeValue / 100) % 10;
			qianWei = (encodeValue / 1000) % 10;
			if (i % 2 == 0) {
				resultStr += cKeyLongStr.charAt((geWei + totalSum) % mod);
				resultStr += cKeyLongStr.charAt((shiWei * 2 + totalSum) % mod);
				resultStr += cKeyLongStr.charAt((baiWei * 3 + totalSum) % mod);
				resultStr += cKeyLongStr.charAt((qianWei * 7 + totalSum) % mod);
			} else {
				resultStr += cKeyLongStr.charAt((geWei * 3 + totalSum) % mod);
				resultStr += cKeyLongStr.charAt((shiWei * 4 + totalSum) % mod);
				resultStr += cKeyLongStr.charAt((baiWei * 7 + totalSum) % mod);
			}
		}
		return resultStr;
	}

	/**
	 * Calculate the MD5 digest of the given bytes.
	 * @param bytes the bytes to calculate the digest over
	 * @return the digest
	 */
	public static byte[] md5Digest(byte[] bytes) {
		return digest(MD5_ALGORITHM_NAME, bytes);
	}

	/**
	 * Return a hexadecimal string representation of the MD5 digest of the given
	 * bytes.
	 * @param bytes the bytes to calculate the digest over
	 * @return a hexadecimal digest string
	 */
	public static String md5DigestAsHex(byte[] bytes) {
		return digestAsHexString(MD5_ALGORITHM_NAME, bytes);
	}

	/**
	 * Append a hexadecimal string representation of the MD5 digest of the given
	 * bytes to the given {@link StringBuilder}.
	 * @param bytes the bytes to calculate the digest over
	 * @param builder the string builder to append the digest to
	 * @return the given string builder
	 */
	public static StringBuilder appendMd5DigestAsHex(byte[] bytes, StringBuilder builder) {
		return appendDigestAsHex(MD5_ALGORITHM_NAME, bytes, builder);
	}

	/**
	 * Creates a new {@link MessageDigest} with the given algorithm. Necessary
	 * because {@code MessageDigest} is not thread-safe.
	 */
	private static MessageDigest getDigest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", ex);
		}
	}

	private static byte[] digest(String algorithm, byte[] bytes) {
		return getDigest(algorithm).digest(bytes);
	}

	private static String digestAsHexString(String algorithm, byte[] bytes) {
		char[] hexDigest = digestAsHexChars(algorithm, bytes);
		return new String(hexDigest);
	}

	private static StringBuilder appendDigestAsHex(String algorithm, byte[] bytes, StringBuilder builder) {
		char[] hexDigest = digestAsHexChars(algorithm, bytes);
		return builder.append(hexDigest);
	}

	private static char[] digestAsHexChars(String algorithm, byte[] bytes) {
		byte[] digest = digest(algorithm, bytes);
		return encodeHex(digest);
	}

	private static char[] encodeHex(byte[] bytes) {
		char chars[] = new char[32];
		for (int i = 0; i < chars.length; i = i + 2) {
			byte b = bytes[i / 2];
			chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
			chars[i + 1] = HEX_CHARS[b & 0xf];
		}
		return chars;
	}

}