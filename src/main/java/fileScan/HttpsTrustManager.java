/**
*This class file was generated with reference to (http://prasans.info/2014/06/making-https-call-using-apache-httpclient/)
*This class file is basically implementing X509TrustManager and auto genrating all the methods.HTTpsTrust manager will
*then trust all clients
*
*Please refer to the link (https://docs.oracle.com/javase/7/docs/api/javax/net/ssl/X509TrustManager.html) for an explanation of
*the X509TrustManager methods
*
* @author  Dhinakaran
* @version 1.0
* @since   2016-nov-14 
*/

package fileScan;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class HttpsTrustManager implements X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {


	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[] {};
	}

}
