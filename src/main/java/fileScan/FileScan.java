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
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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

import nObjectModel.Account;

public class FileScan {

	// var to store whether a file is infected(true means file is infected false
	// means its false)
	private static boolean fileInfected;
	// the file object which requires to be scanned
	private static File fileToScan;


	public static void  setFileToScan(File fileInput) {
		fileToScan = fileInput;
	}

	// this var is the id of the file when sent to the api for scanning
	private static String resource;
	// this var is response code of the reply coming from the api
	private static int responseStatus;

	public int getResponseStatus() {
		return new Integer(responseStatus);
	}

	// api for the file scan
	private static final String URL_FILESCAN = "https://www.virustotal.com/vtapi/v2/file/scan";
	// api for the file scan report
	private static final String URL_FILEREPORT = "https://www.virustotal.com/vtapi/v2/file/report";
	// api for the file scan report
	private static String APIKEY;

	// var to store the boolean on whether the file scan is able to proceed
	// after verification of the token
	// if false means the user is not allowed to fun the file scan
	private static boolean runningStatus;

	/**
	 * get method for runningStatus var.This is for other class files to know
	 * whether can proceed with the file status
	 * 
	 * @param
	 * @return runningStatus the boolean value of the running status
	 */
	public boolean isRunningStatus() {
		return new Boolean(runningStatus);
	}

	/**
	 * FileScan constructor method.
	 *
	 * @param file
	 *            file object which is going to be scanned
	 * @return
	 */
	public FileScan() throws IOException, JSONException, KeyManagementException, NoSuchAlgorithmException {

	}

	/**
	 * get method for fileInfected var.This is for other programs to know
	 * whether the file is infected or not
	 * 
	 * @param
	 * @return fileInfected the boolean value of the infection status
	 */
	public boolean isFileInfected() {
		return new Boolean(fileInfected);
	}

	/**
	 * This method makes an api request through a HTTP POST request to send the
	 * file to the api for scanning.Once the file is sent to the api for
	 * scanning,the api returns a resource id.This is then stored as a variable
	 * as above. The http post request is secure HTTPS call using the Apache
	 * HTTP Client Library
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
		builder.addTextBody("apikey", APIKEY);
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
	 * positives,the file would then be determined to be infected with virus The
	 * http post request is secure HTTPS call using the Apache HTTP Client
	 * Library
	 * 
	 * @param
	 * @return
	 */

	public void scanResults() throws IOException, JSONException, KeyManagementException, NoSuchAlgorithmException {
		SSLContext sslcontext = SSLContexts.custom().build();
		sslcontext.init(null, new X509TrustManager[] { new HttpsTrustManager() }, new SecureRandom());
		@SuppressWarnings("deprecation")
		SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

		CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(factory).build();
		HttpPost httpPost = new HttpPost(URL_FILEREPORT);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("apikey", APIKEY);
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
		APIKEY = "";
		fileToScan = null;
		resource=null;
		responseStatus=0;
		runningStatus=false;

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
	 * it the file scanned results are out.The http post request is secure HTTPS
	 * call using the Apache HTTP Client Library
	 * 
	 * @param
	 * @return
	 */

	public void checkResponseStatus()
			throws IOException, JSONException, KeyManagementException, NoSuchAlgorithmException {
		SSLContext sslcontext = SSLContexts.custom().build();
		sslcontext.init(null, new X509TrustManager[] { new HttpsTrustManager() }, new SecureRandom());
		@SuppressWarnings("deprecation")
		SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

		CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(factory).build();
		HttpPost httpPost = new HttpPost(URL_FILEREPORT);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("apikey", APIKEY);
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
	 * use for api requests.Since the API key is stored in the FreshDrive
	 * server,to reterive the API key from the server,the user will first be
	 * validated on his user token,if it is validated sucessfully the API key
	 * will be returned sucessfully and the running status will be set as
	 * true(which means the user is able to carry on with the file scanning
	 * 
	 * @param
	 * @return apiKey this is the api key which will be used for api requests
	 */

	protected static void getAPIkey() {

		try {
			URL url = new URL(nURLConstants.Constants.virusScanURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// Adding Header
			con.setRequestMethod("POST");

			// send Post
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes("username=" + Account.getAccount().getUsername() + "&user_token="
					+ Account.getAccount().get_token());
			out.flush();
			out.close();

			// Response from Server
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String response;

			while ((response = in.readLine()) != null) {
				APIKEY = response;
			}
			in.close();

			if (APIKEY.equals("unverified-token")) {
				runningStatus = false;
			} else {
				runningStatus = true;
			}

		} catch (MalformedURLException ex) {
			System.out.println("API URL error");
		} catch (IOException ex) {
			System.out.println("API IO error");
		} catch (Exception ex){
			System.out.println("API error");
		}

	}

	/**
	 * All the scanning starts here First the file which is passed in is being
	 * assigned to the var. Next it then tries get the API key to access the
	 * virustotal api If getting the API key is successfull,The runningStatus
	 * variable will be set as true.Then the method would then send the file for
	 * scanning and check the responseStatus of the file scan report
	 * 
	 * @throws IOException
	 * @throws JSONException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	public void startScan() throws IOException, JSONException, KeyManagementException, NoSuchAlgorithmException {
		getAPIkey();
		if (runningStatus) {
			scanFile();
			checkResponseStatus();
		}
	}

}
