package org;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class testCode {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// TODO Auto-generated method stub
//			String str = "≤‚ ‘";
//			System.out.println(java.nio.charset.Charset.forName("GBk")
//					.newEncoder().canEncode(str));
//			System.out.println(java.nio.charset.Charset.forName("utf-8")
//					.newEncoder().canEncode(str));
//			System.out.println(URLDecoder.decode(URLEncoder.encode(str, "utf-8"),
//					"utf-8"));
			String str="cccccc%20ssss zzzzzz<aaa>sssssss/";
//			System.out.println(URLEncoder.encode(" ","utf-8"));
//			System.out.println(URLEncoder.encode("+","utf-8"));
//			System.out.println(URLEncoder.encode("/","utf-8"));
			System.out.println(URLEncoder.encode("<","utf-8"));
			System.out.println(URLEncoder.encode(">","utf-8"));
//			System.out.println(URLEncoder.encode("(","utf-8"));
//			System.out.println(URLEncoder.encode(")","utf-8"));
//			System.out.println(URLEncoder.encode("&","utf-8"));
			str=str.replaceAll("<", "%3C");
			System.out.println(str);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 * test code
	 * @param str
	 * @return
	 */
	    public static String getEncoding(String str) {  
	        String encode = "GB2312";  
	        try {  
	            if (str.equals(new String(str.getBytes(encode), encode))) {  
	                String s = encode;  
	                return s;  
	            }  
	        } catch (Exception exception) {  
	        }  
	        encode = "ISO-8859-1";  
	        try {  
	            if (str.equals(new String(str.getBytes(encode), encode))) {  
	                String s1 = encode;  
	                return s1;  
	            }  
	       } catch (Exception exception1) {  
	        }  
	        encode = "UTF-8";  
	        try {  
	            if (str.equals(new String(str.getBytes(encode), encode))) {  
	                String s2 = encode;  
	                return s2;  
	            }  
	        } catch (Exception exception2) {  
	        }  
	        encode = "GBK";  
	        try {  
	           if (str.equals(new String(str.getBytes(encode), encode))) {  
	                String s3 = encode;  
	                return s3;  
	            }  
	        } catch (Exception exception3) {  
	        }  
	        return "";  
	    } 
}
