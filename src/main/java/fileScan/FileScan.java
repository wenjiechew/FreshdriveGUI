/**
* This file scan class scans a file and informs the user if the file is infected.
* This class was built on the VirusTotal Public API v2.0(https://www.virustotal.com/en/documentation/public-api/)
* Any references to the  API is described more in the link above
*
* @author  Dhinakaran
* @version 1.0
* @since   2016-nov-03 
*/

package fileScan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class FileScan {

	// var to store whether a file is infected(true means file is infected false
	// means its false)
	private static boolean fileInfected;
	// the file object which requires to be scanned
	private static File fileToScan;
	// this var is the id of the file when sent to the api for scanning
	private static String resource;
	// this var is response code of the reply coming from the api
	private static int responseStatus;

	// api for the file scan
	private static final String URL_FILESCAN = "https://www.virustotal.com/vtapi/v2/file/scan";
	// api for the file scan report
	private static final String URL_FILEREPORT = "https://www.virustotal.com/vtapi/v2/file/report";

	/**
	 * FileScan constructor method.All the scanning starts here First the file
	 * which is passed in is being assigned Next it runs the
	 * scanFile,responsestatus and scanresults methods there is thread sleep
	 * here to wait for 1 minute before making an API call again to get the scan
	 * results. The reason is public API key is only allowed 4 requests/minute
	 *
	 * @param file
	 *            file object which is going to be scanned
	 * @return
	 */
	public FileScan(File file) throws IOException, JSONException, KeyManagementException, NoSuchAlgorithmException {
		FileScan.fileToScan = file;
		scanFile();
		checkResponseStatus();
		while (FileScan.responseStatus == 0) {
			// wait
			try {
				Thread.sleep(60000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			FileScan.checkResponseStatus();
		}
		FileScan.scanResults();

	}

	/**
	 * get method for fileInfected var.This is for other programs to know
	 * whether the file is infected or not
	 * 
	 * @param
	 * @return fileInfected the boolean value of the infection status
	 */
	public boolean isFileInfected() {
		return fileInfected;
	}

	/**
	 * This method makes an api request through a HTTP POST request to send the
	 * file to the api for scanning.Once the file is sent to the api for
	 * scanning,the api returns a resource id.This is then stored as a variable
	 * as above
	 * 
	 * @param
	 * @return
	 */

	protected static void scanFile()
			throws IOException, JSONException, KeyManagementException, NoSuchAlgorithmException {

		SSLContext sslcontext = SSLContexts.custom().build();
		sslcontext.init(null, new X509TrustManager[] { new HttpsTrustManager() }, new SecureRandom());
		@SuppressWarnings("deprecation")
		SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

		CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(factory).build();
		HttpPost httpPost = new HttpPost(URL_FILESCAN);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("apikey", getAPIkey());
		builder.addBinaryBody("file", fileToScan, ContentType.APPLICATION_OCTET_STREAM, "file.ext");

		HttpEntity multipart = builder.build();
		httpPost.setEntity(multipart);

		CloseableHttpResponse response1 = client.execute(httpPost);

		try {
			HttpEntity entity1 = response1.getEntity();
			String responseBody = responseAsString(response1);
			JSONObject jObject = new JSONObject(responseBody);
			FileScan.resource = jObject.getString("resource");
			EntityUtils.consume(entity1);
		} finally {
			response1.close();
			client.close();
		}
	}

	/**
	 * This makes an api request through a HTTP POST request for getting the
	 * file scan's results It passes in the API key anc the resource id which
	 * set above Next with the results which was given by the api,the method
	 * then counts the number of scan engines which reports that the file is
	 * virus infected through the positives JSON tag If there is more than 0
	 * positives,the file would then be determined to be infected with virus
	 * 
	 * @param
	 * @return
	 */

	protected static void scanResults()
			throws IOException, JSONException, KeyManagementException, NoSuchAlgorithmException {
		SSLContext sslcontext = SSLContexts.custom().build();
		sslcontext.init(null, new X509TrustManager[] { new HttpsTrustManager() }, new SecureRandom());
		@SuppressWarnings("deprecation")
		SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

		CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(factory).build();
		HttpPost httpPost = new HttpPost(URL_FILEREPORT);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("apikey", getAPIkey());
		builder.addTextBody("resource", FileScan.resource);

		HttpEntity multipart = builder.build();
		httpPost.setEntity(multipart);

		CloseableHttpResponse response1 = client.execute(httpPost);
		int positives;

		try {
			HttpEntity entity1 = response1.getEntity();
			String responseBody = responseAsString(response1);
			JSONObject jObject = new JSONObject(responseBody); // json
			positives = jObject.getInt("positives");
			EntityUtils.consume(entity1);
		} finally {
			response1.close();
			client.close();
		}
		if (positives > 0) {
			FileScan.fileInfected = true;
		} else {
			FileScan.fileInfected = false;
		}

	}

	/**
	 * This method gets the HTTP response and converts it as a string
	 * 
	 * @param response
	 *            A closeableHTTPresponse is passed in for the conversion
	 * @return string the converted string response
	 */
	protected static String responseAsString(CloseableHttpResponse response) throws IOException {
		return streamAsString(response.getEntity().getContent());
	}

	/**
	 * This makes an api request through a HTTP POST request for getting the
	 * file scan's results. It is essentially the same as the scanresults
	 * method. However instead of checking the response,this first checks
	 * whether the response status.If the response status is 0.It means the
	 * request rate for a minute has been met and the program need to wait until
	 * it the file scanned results are out
	 * 
	 * @param
	 * @return
	 */

	protected static void checkResponseStatus()
			throws IOException, JSONException, KeyManagementException, NoSuchAlgorithmException {
		SSLContext sslcontext = SSLContexts.custom().build();
		sslcontext.init(null, new X509TrustManager[] { new HttpsTrustManager() }, new SecureRandom());
		@SuppressWarnings("deprecation")
		SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

		CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(factory).build();
		HttpPost httpPost = new HttpPost(URL_FILEREPORT);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("apikey", getAPIkey());
		builder.addTextBody("resource", FileScan.resource);

		HttpEntity multipart = builder.build();
		httpPost.setEntity(multipart);

		CloseableHttpResponse response1 = client.execute(httpPost);
		try {
			HttpEntity entity1 = response1.getEntity();
			String responseBody = responseAsString(response1);
			JSONObject jObject = new JSONObject(responseBody); // json
			FileScan.responseStatus = jObject.getInt("response_code");
			EntityUtils.consume(entity1);
		} finally {
			response1.close();
			client.close();
		}

	}

	/**
	 * This method gets the InputStream and converts it to a string
	 * 
	 * @param inputStream
	 *            InputStream object for covnersion
	 * @return string the converted InputStream
	 */
	protected static String streamAsString(InputStream inputStream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "UTF-8");
		return writer.toString();
	}

	/**
	 * This method makes a http post request to get the virus scan API key to
	 * use for api requests
	 * 
	 * @param
	 * @return apiKey this is the api key which will be used for api requests
	 */

	protected static String getAPIkey() {
		String apiKey = null;
		try {
			URL url = new URL(nURLConstants.Constants.virusScanURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// Adding Header
			con.setRequestMethod("POST");

			// Response from Server
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String response;

			while ((response = in.readLine()) != null) {
				apiKey = response;
			}
			in.close();

			System.out.println(apiKey);
			return apiKey;
		} catch (MalformedURLException ex) {
			System.out.println(ex);
		} catch (IOException ex) {
			System.out.println(ex);
		}
		return apiKey;
	}

}
