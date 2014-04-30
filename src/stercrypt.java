import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.jasypt.util.text.BasicTextEncryptor;

/**
 * Uses Jasypt (http://www.jasypt.org)
 */

public class stercrypt {

	static String convertToHex(String what) {
		String res = new String();
		try {
			what = URLEncoder.encode(what,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		for (int i=0;i<what.length();i++) {
			byte one = what.getBytes()[i];
			String hexer = Integer.toHexString(one+127);
			if (hexer.length() < 2) {hexer = "0"+hexer;}
			res = res + hexer;
			System.out.println(i+"one="+(one+127)+","+hexer+","+res.length());
		}
		return res;
	}
	
	static String convertFromHex(String what) {
		String res = new String();
		byte[] tostrb = new byte[what.length()/2];
		int twomore = 0; int lasti=0;
		System.out.println("tw="+what.length());
		for (int i=0;i<what.length()/2;i++) {
			int two = Integer.parseInt(what.substring(twomore,twomore+2),16);
			twomore=twomore+2;
			System.out.println(i+"two="+two);
			two = two - 127;
			tostrb[i] = (byte)two;
			lasti = i;
		}
		System.out.println("i="+lasti);
		res = new String(tostrb);
		try {
			res = URLDecoder.decode(res,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	static byte[] convertToBytes(String val) {
		try {
			val = URLEncoder.encode(val,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return val.getBytes();
		
	}
	
	static String convertFromBytes(byte[] data) {
		String res = new String();
		try {
			res = URLDecoder.decode((new String(data)),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	static String swappositions(String content,int initialpos, int finalpos) {

		if (initialpos < 0) {return content;}
		if (finalpos < 0) {return content;}
		if (initialpos > content.length()-1) {return content;}
		if (finalpos > content.length()-1) {return content;}
		if (initialpos == finalpos) {return content;}
		if (initialpos > finalpos) {
			int swap = finalpos;
			finalpos = initialpos;
			initialpos = swap;
		}

		String res = "";
		char ip = content.charAt(initialpos);
		char fp = content.charAt(finalpos);
		res = new String(content);
		res = content.substring(0,initialpos)+fp+
				content.substring(initialpos+1,finalpos)+ip+
				content.substring(finalpos+1,content.length());
		return res;
	}
	
	static String mix(int[] key,String content, boolean useinreverse) {
		String res = new String(content);
		for (int i=0;i<key.length;i++) {
			if (useinreverse == false) {
				res = swappositions(res,key[i],i);	
			} else {
				res = swappositions(res,key[(key.length-1)-i],(key.length-1)-i);
			}
		}
		return res;
	}  

	static String EncryptWithPassword(String text, String password) {
		return EncryptWithPassword(text, password, 1);
	}
	
	static String EncryptWithPassword(String text, String password, int iterations) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(password);
		String res = new String(text);
		for (int i=0;i<iterations;i++) {
			res = textEncryptor.encrypt(res);
		}
		return res;
	}
	
	static String DecryptWithPassword(String text, String password) {
		return DecryptWithPassword(text, password, 1);
	}
	
	static String DecryptWithPassword(String text, String password, int iterations) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(password);
		String res = new String(text);
		for (int i=0;i<iterations;i++) {
			res = textEncryptor.decrypt(res);
		}
		return res;
	}
	
	// for test
	public static void main(String[] args) throws Exception {

		String c = "madamadmada&%$#$%##)=ŠČĆĐŽžđšćč!~`hfhf/[[2kfkf";
		int iterationcount = 2;
		
		c = EncryptWithPassword(c, "mojastikla",1);
		System.out.println(c);
		
		c = DecryptWithPassword(c, "mojastikla",1);
		System.out.println(c+" "+iterationcount);
		
		/*System.out.println(convertFromBytes(convertToBytes(c)));*/
		
		/*c = convertToHex(c);
		System.out.println("res is:"+c);
		c = convertFromHex(c);
		System.out.println("res is:"+c);*/
		
		/*for (int j=0;j<1;j++) {

			int[] key = new int[32000];
			Random generator = new Random();
			int swapvalue = key.length;
			if (swapvalue > c.length()) {swapvalue = c.length();}
			for (int i=0;i<swapvalue;i++) {
				key[i] = i;
			}
			for (int i=0;i<swapvalue;i++) {
				int nextone = generator.nextInt(swapvalue);
				int swap = key[i];
				key[i] = key[nextone];
				key[nextone] = swap;
			}


			c = mix(key,c,false);
			System.out.println(c);
			c = mix(key,c,true);
			System.out.println(c);	
		}*/

		//System.out.println(swappositions(c,1, 7));

		// get the key length, and either multiply the key or pad the value to the keylength

	}

}