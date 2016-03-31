package com.pitt.cdm.arcgis.android.networkanalysis;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import com.pitt.cdm.arcgis.android.maps.R;
import android.util.Log;
import android.widget.Toast;

public class NetworkService {
	 
    private static String TAG = "NetworkService";
     
    //private static String url_ip = ServerUrl.SERVER_ADRESS+"UserInfoServlet?";
    private static String url_ip = R.string.coordinator_url+"notification.do?action=send&broadcast=N&username=2&title=leader&message=update&uri=";
    /**
     * é‡Šæ”¾èµ„æº�
     */
    public static void cancel() {
        Log.i(TAG, "cancel!");
        // if(conn != null) {
        // conn.cancel();
        // }
    }
 
    //æ— å�‚æ•°ä¼ é€’çš„
        public static String getPostResult(String url){
             
           //url =  url_ip;
            //åˆ›å»ºhttpè¯·æ±‚å¯¹è±¡
            HttpPost post = new HttpPost(url);
             
            //åˆ›å»ºHttpParamsä»¥ç”¨æ�¥è®¾ç½®HTTPå�‚æ•°
            BasicHttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,20 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
 
            //åˆ›å»ºç½‘ç»œè®¿é—®å¤„ç�†å¯¹è±¡
            HttpClient httpClient = new DefaultHttpClient(httpParams);
            try{
                //æ‰§è¡Œè¯·æ±‚å�‚æ•°ï¿½?
                HttpResponse response = httpClient.execute(post);
                //åˆ¤æ–­æ˜¯å�¦è¯·æ±‚æˆ�åŠŸ
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    //èŽ·å¾—å“�åº”ä¿¡æ�¯
                    String content = EntityUtils.toString(response.getEntity());
                    return content;
                } else {
                    //ç½‘è¿žæŽ¥å¤±è´¥ï¼Œä½¿ç”¨Toastæ˜¾ç¤ºæ��ç¤ºä¿¡æ�¯
                     
                }
                 
            }catch(Exception e) {
                e.printStackTrace();
                return "{\"status\":406,\"resultMsg\":\"time out\"}";
            } finally {
                //é‡Šæ”¾ç½‘ç»œè¿žæŽ¥èµ„æº�
                httpClient.getConnectionManager().shutdown();
            }
            return "{\"status\":405,\"resultMsg\":\"time out\"}";
             
        }
       //æœ‰å�‚æ•°ä¼ é€’çš„
        public static String getPostResult(String url, List<NameValuePair> paramList){
            UrlEncodedFormEntity entity = null;
            try {
                entity = new UrlEncodedFormEntity(paramList,"utf-8");
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }  
         
            //åˆ›å»ºhttpè¯·æ±‚å¯¹è±¡
            HttpPost post = new HttpPost(url);
            BasicHttpParams httpParams = new BasicHttpParams();
             
            HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
            post.setEntity(entity);
            //åˆ›å»ºç½‘ç»œè®¿é—®å¤„ç�†å¯¹è±¡
            HttpClient httpClient = new DefaultHttpClient(httpParams);
            try{
                //æ‰§è¡Œè¯·æ±‚å�‚æ•°ï¿½?
                HttpResponse response = httpClient.execute(post);
                //åˆ¤æ–­æ˜¯å�¦è¯·æ±‚æˆ�åŠŸ
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    //èŽ·å¾—å“�åº”ä¿¡æ�¯
                    String content = EntityUtils.toString(response.getEntity(),"UTF-8");
                   
                    return "OK";
                } else {
                    //ç½‘è¿žæŽ¥å¤±è´¥ï¼Œä½¿ç”¨Toastæ˜¾ç¤ºæ��ç¤ºä¿¡æ�¯
                     
                }
                 
            }catch(Exception e) {
                e.printStackTrace();
                return "{\"status\":405,\"resultMsg\":\"time out\"}";
            } finally {
                //é‡Šæ”¾ç½‘ç»œè¿žæŽ¥èµ„æº�
                httpClient.getConnectionManager().shutdown();
            }
            return "{\"status\":405,\"resultMsg\":\"time out\"}";
             
        }
}
