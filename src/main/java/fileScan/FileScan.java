package fileScan;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.File;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;

public class FileScan {

	private static boolean fileInfected;
	private static File fileToScan;
	private static String resource;
	private static int responseStatus;
	

	public FileScan(File file) throws IOException, JSONException {
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

	public boolean isFileInfected() {
		return fileInfected;
	}

	private static final String URL_FILESCAN = "https://www.virustotal.com/vtapi/v2/file/scan";
	private static final String URL_FILEREPORT = "https://www.virustotal.com/vtapi/v2/file/report";
	private static final String API_KEY = "8eae5cfab4d8d18cd4a15b1aac7dbd5710558e2667a8394805160f819896a3b4";

	protected static void scanFile() throws IOException, JSONException {

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(URL_FILESCAN);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("apikey", API_KEY);
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

	protected static void scanResults() throws IOException, JSONException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(URL_FILEREPORT);
		int positives;
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("apikey", API_KEY);
		builder.addTextBody("resource", FileScan.resource);

		HttpEntity multipart = builder.build();
		httpPost.setEntity(multipart);

		CloseableHttpResponse response1 = client.execute(httpPost);
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

	protected static void scanResultsTest() throws IOException, JSONException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(URL_FILEREPORT);
		boolean infected = false;
		int positives;
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("apikey", API_KEY);
		builder.addTextBody("resource", "8b7142fda0dd7bce9d76e6f561fbc5163bf4c0c8a81bc4391cd1e1bf7f940f1c");

		HttpEntity multipart = builder.build();
		httpPost.setEntity(multipart);

		CloseableHttpResponse response1 = client.execute(httpPost);
		try {
			HttpEntity entity1 = response1.getEntity();
			String responseBody = responseAsString(response1);
			int responseStatus = response1.getStatusLine().getStatusCode();
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

	protected static String responseAsString(CloseableHttpResponse response) throws IOException {
		return streamAsString(response.getEntity().getContent());
	}

	protected static void checkResponseStatus() throws IOException, JSONException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(URL_FILEREPORT);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("apikey", API_KEY);
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

	protected static String streamAsString(InputStream inputStream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "UTF-8");
		return writer.toString();
	}

}
