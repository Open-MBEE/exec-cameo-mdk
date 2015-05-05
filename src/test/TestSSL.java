import gov.nasa.jpl.mbee.web.EasyX509TrustManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;

import javax.net.ssl.SSLContext;


public class TestSSL {

	public static void main(String[] args) {
		/*Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", easyhttps);
		HttpClient httpClient = new HttpClient();

		GetMethod pm = new GetMethod("https://docgen.jpl.nasa.gov/staging-docweb/servers/");			
		try {
			HttpClient client = new HttpClient();
			client.executeMethod(pm);
			
			System.out.println(pm.getResponseBodyAsString());
			
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
		
			return;
		} finally{
			pm.releaseConnection();
		}
		*/
		for (Provider p: java.security.Security.getProviders()) {
			System.out.println(p.getName());
		}
		SSLContext c = null;
		try {
			c = SSLContext.getInstance("SSL");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			c.init(null, new TrustManager[]{new EasyX509TrustManager(null)}, new SecureRandom());
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			URL url = new URL("https://docgen.jpl.nasa.gov/staging-docweb/servers/");
			HttpURLConnection conn;
		      BufferedReader rd;
		      String line;
		      String result = "";
		      try {
		        HttpsURLConnection.setDefaultSSLSocketFactory(c.getSocketFactory());
		         conn = (HttpURLConnection) url.openConnection();
		         conn.setRequestMethod("GET");
		         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		         while ((line = rd.readLine()) != null) {
		            result += line;
		         }
		         rd.close();
		      } catch (Exception e) {
		         e.printStackTrace();
		      }
		      System.out.println(result);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
