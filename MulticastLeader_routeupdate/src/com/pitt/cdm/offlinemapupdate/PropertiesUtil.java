package com.pitt.cdm.offlinemapupdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

public class PropertiesUtil {
	 
	 private static Properties urlProps;
	 private static final String path = "/sdcard/config.properties";
	 //private FileUtils fu = new FileUtils();
	 public static Properties getProperties(){
	  Properties props = new Properties();
	  try {
	   InputStream in = new FileInputStream(getSettingFile());
	   props.load(in);
	  } catch (Exception e1) {
	   // TODO Auto-generated catch block
	   e1.printStackTrace();
	  }	   
	  urlProps = props;
	  return urlProps;
	 }
	 
	 public static void setProperties( String param, String value ){
	  Properties props = new Properties();
	  try {
	   props.load(new FileInputStream( getSettingFile() ));
	   
	   OutputStream out = new FileOutputStream(path);
	   Enumeration<?> e = props.propertyNames();
	   if(e.hasMoreElements()){
	    while(e.hasMoreElements()){
	     String s = (String)e.nextElement();
	     if(!s.equals(param))
	      props.setProperty(s, props.getProperty(s));
	      //props.setProperty(s, value);
	    }
	   }
	   props.setProperty(param, value);
	   props.store(out, null);

	  } catch (IOException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  }
	 }
	 
	 private static File getSettingFile(){
	  File setting = new File(path);
	  if(!setting.exists())
		try {
			setting.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  return setting;
	 }
	}