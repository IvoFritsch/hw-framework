package restFramework.onesignal;

public class OnesignalConstants {
	
    private static String appId = null;
    private static String authorization = null;
    private static boolean finished = false;

    private static String userInfoURL = "https://onesignal.com/api/v1/players/%s?app_id=%s";
	
	
    public static String getAppID() {
        return appId;
    }

    public static String getAuthorization() {
        return authorization;
    }
	
    public static String getUserInfoURL( String userId ) {
        return String.format( userInfoURL, userId, appId );
    }

    public static void setAppId(String appId) {
        OnesignalConstants.appId = appId;
        if(OnesignalConstants.appId != null && OnesignalConstants.authorization != null)
            finished = true;
    }

    public static void setAuthorization(String authorization) {
        OnesignalConstants.authorization = authorization;
        if(OnesignalConstants.appId != null && OnesignalConstants.authorization != null)
            finished = true;
    }

    public static boolean isFinished() {
        return finished;
    }
    
}