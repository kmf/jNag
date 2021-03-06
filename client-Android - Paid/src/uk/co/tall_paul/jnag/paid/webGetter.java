package uk.co.tall_paul.jnag.paid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.io.InputStream;

import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.os.Bundle;
import android.text.TextUtils;

public class webGetter {
	
	private Context context;
	private settingsClass sc;
	
	 public webGetter(Context myContext){
	        context = myContext;
	    } 
	 
	// always verify the host - dont check for certificate
	 final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
	         public boolean verify(String hostname, SSLSession session) {
	                 return true;
	         }
	 };

	 /**
	  * Trust every server - dont check for any certificate
	  */
	 private static void trustAllHosts() {
	         // Create a trust manager that does not validate certificate chains
	         TrustManager[] trustAllCerts = new TrustManager[] { 
	        		 new X509TrustManager() {
	        			 public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                        return new java.security.cert.X509Certificate[] {};
	        			 }
	        			 public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	        			 }

	                 public void checkServerTrusted(X509Certificate[] chain,
	                                 String authType) throws CertificateException {
	                 }
	         } };

	         // Install the all-trusting trust manager
	         try {
	                 SSLContext sc = SSLContext.getInstance("TLS");
	                 sc.init(null, trustAllCerts, new java.security.SecureRandom());
	                 HttpsURLConnection
	                                 .setDefaultSSLSocketFactory(sc.getSocketFactory());
	         } catch (Exception e) {
	                 //e.printStackTrace();
	         }
	 }


	 
  //post comment / acknowledgement 
  public void cmd(String cmd_url,String data){	  
	  //remove username / password from request if it's there
	  int start = cmd_url.indexOf("//");
	  int end = cmd_url.indexOf("@");
	  if (end > -1){
		String unwanted = cmd_url.substring(start+2,end+1);
		cmd_url = cmd_url.replaceAll(unwanted, "");
		Log.w("jNag",cmd_url);
	  }
	  StringBuilder sb = new StringBuilder();
      sb.append(data);
	  sc = new settingsClass(context);
	
		final String password = sc.getSetting("password");
		final String username = sc.getSetting("username");
		
		Authenticator.setDefault(new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username,password.toCharArray());
			}});
  HttpURLConnection c;    
  try {
		//try to connect using saved credentials and URL
  	if (cmd_url.toLowerCase().contains("https")){
  		trustAllHosts();
  		c = (HttpsURLConnection) new URL(cmd_url).openConnection();
  		((HttpsURLConnection) c).setHostnameVerifier(DO_NOT_VERIFY);
  	} else {
  		c = (HttpURLConnection) new URL(cmd_url).openConnection();
  	}
  	c.setRequestMethod( "POST" );
    c.setDoInput( true );
    c.setDoOutput( true );
    OutputStreamWriter wr = new OutputStreamWriter(c.getOutputStream());
                // this is were we're adding post data to the request
                wr.write(sb.toString());
    Log.d("jNag","writing " + sb.toString() + " to " + cmd_url);
	wr.flush();
	// Get the response
    BufferedReader rd = new BufferedReader(new InputStreamReader(c.getInputStream()));
    String line;
    while ((line = rd.readLine()) != null) {
        // Process line...
    }
    wr.close();
    rd.close();
	wr.close();
	c.disconnect();
	} catch (Exception e) {
		Log.d("jNag","connection error " + e.getLocalizedMessage() + "in webgetter.post");
	}
  }
	
	
	
	public String get(String parameters,int timeout){
		String returnedVal;
			//get url, username, password
				sc = new settingsClass(context);
				String data_url = sc.getSetting("data_url").trim();
				final String password = sc.getSetting("password");
				final String username = sc.getSetting("username");
				Log.d("jNag","Get: " + data_url + parameters.replace(" ", "").trim());
				if (data_url == "" || data_url == " " || data_url == null)
				{
					Log.d("jNag","data_url not set, returning nowt");
					return "";
				}
        	Authenticator.setDefault(new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username,password.toCharArray());
            }});
            HttpURLConnection c;    
            StringBuilder total = new StringBuilder();
            try {
    			//try to connect using saved credentials and URL
            	if (data_url.toLowerCase().contains("https")){
            		trustAllHosts();
            		c = (HttpsURLConnection) new URL(data_url + parameters).openConnection();
            		((HttpsURLConnection) c).setHostnameVerifier(DO_NOT_VERIFY);
            	} else {
            		c = (HttpURLConnection) new URL(data_url + parameters).openConnection();
            	}
            	c.setConnectTimeout(timeout);
            	c.setReadTimeout(timeout);
    			InputStream in = new BufferedInputStream(c.getInputStream(),40960);		    
    		    BufferedReader r = new BufferedReader(new InputStreamReader(in),40960);
    			String line;
    			while ((line = r.readLine()) != null) {
    			    total.append(line);
    			}
    			c.disconnect();
    		} catch (Exception e) {
    			Log.d("jNag","connection error " + e.getLocalizedMessage() + "in webgetter");
    		}
    		returnedVal = total.toString().replace("\\\"","");
    		Log.d("jNag","webGetter returning: " + returnedVal.replace("\\", "").trim());
    		return returnedVal.replace("\\", "").trim();
	}
	
	//this is an overload to provide a default timeout value
	public String get(String parameters){
		return get(parameters,60000);
	}
	
}
