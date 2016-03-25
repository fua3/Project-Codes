package org.pitt.serviceadapter.checkMessyCode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 检查是否存在乱码
 * @author Fu Baolai
 *
 */
public class CheckMessyCode {
	/**
	 * 是否是中文
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}
	/**
	 * 是否存在乱码
	 * @param strName 要验证的字符串
	 * @return 有乱码返回true，没有false
	 */
	public static boolean isMessyCode(String strName) {
		Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
		Matcher m = p.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = ch.length;
		float count = 0;
		char a='=';
		char w='?';
		char k1='<';
		char k2='>';
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {
				if (!isChinese(c)&&!(a==c)&&!(w==c)&&!(k1==c)&&!(k2==c)) {// 修改，原代码中=也算是乱码了，
					count = count + 1;
					System.out.print(c);
				}
			}
		}
		float result = count / chLength;
		if (result > 0) {// 原代码中>0.4 但是，先要求验证url，所以修改为>0
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) {
//		System.out.println(isMessyCode("*JTP.jar�ļ��JTP�ļ���ȡ��ͼƬ��Դ"));
//		System.out.println(isMessyCode("http://172.26.15.45/service/SisService/sisserver?ceshi='交接测试'&&user=gdtest&password=gdtest123&encode=GBK"));
//		System.out.println(isMessyCode("你好?????"));
//		System.out.println(isMessyCode("http://172.26.15.45/service/SisService/sisserver?ceshi='交接测试'&&user=gdtest&password=gdtest123&encode=GBK"));
		String aa="测试";
		try {
//			System.out.println(new String(aa.getBytes("gbk"),"utf-8"));
//			System.out.println(new String(aa.getBytes("utf-8"),"gbk"));
//			System.out.println(new String(new String(aa.getBytes("gbk"),"utf-8").getBytes("utf-8"),"gbk"));//?????
//			System.out.println(new String(aa.getBytes("gbk"),"gbk"));
//			System.out.println(new String(aa.getBytes("iso-8859-1"),"utf-8"));
			// gbk-->utf-8-->gbk
			System.out.println(new String(new String(aa.getBytes("gbk"),"utf-8").getBytes("utf-8"),"gbk"));
			// utf-8-->gbk-->utf-8
			System.out.println(new String(new String(aa.getBytes("utf-8"),"gbk").getBytes("gbk"),"utf-8"));
			System.out.println(new String(aa.getBytes("utf-8"),"gbk"));
		} catch (Exception e) {
			// TODO: handle exception
		}
	} 

}