/**
 * The constants class file is to hold the URLs for each the servets in the freshdriveserver
 * 
 */

package nURLConstants;

public class Constants {
	public final static String hostname = "localhost";

	private final static String mainURL ="https://"+ hostname + ":8443/FreshdriveServer/";

	public final static String loginURL = mainURL + "Login";
	public final static String logoutURL = mainURL + "Logout";
	public final static String regURL = mainURL + "Register";
	public final static String sharingURL = mainURL + "ShareFile";
	public final static String uploadURL = mainURL + "Upload";
	public final static String sharingListURL = mainURL + "SharingList";
	public final static String ownershipURL = mainURL + "ValidateOwner";

	public final static String retrieveURL = mainURL + "Retrieve";
	public final static String downloadURL = mainURL + "Download";

	public final static String otpURL = mainURL + "VerifyOTP";
	public final static String virusScanURL = mainURL + "ScanFile";
	public final static String deleteURL = mainURL + "Delete";

}
