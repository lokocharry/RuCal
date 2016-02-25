package GCM;

import org.apache.http.message.BasicNameValuePair;

import common.Commons;

public class Device {

    public static void register(String deviceID, String registrationId, String name, String email, String password) {
        String url = Commons.API_URL + "register/";

        RestClient client = new RestClient(url);

        client.addPostParam(new BasicNameValuePair("dev_id", deviceID));
        client.addPostParam(new BasicNameValuePair("reg_id", registrationId));
        client.addPostParam(new BasicNameValuePair("name", name));
        client.addPostParam(new BasicNameValuePair("email", email));
        client.addPostParam(new BasicNameValuePair("password", password));
        client.execute();
    }

    public static void unregister(String deviceID) {
        String url = Commons.API_URL + "unregister/";

        RestClient client = new RestClient(url);

        client.addPostParam(new BasicNameValuePair( "dev_id", deviceID ));
        client.execute();
    }
}
