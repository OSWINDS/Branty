/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextractor;

import com.mongodb.BasicDBObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author alarv
 */
public class StockAPI {

    private static final String USER_AGENT = "Mozilla/5.0";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MongoDB db = new MongoDB("alarv", "220757");

        db = new MongoDB("alarv", "220757");
        String[] allNames = db.getAllNames("features");
        for (String name : allNames) {
            try {
                String url = "http://dev.markitondemand.com/Api/v2/Lookup/jsonp?input=";
                url += URLEncoder.encode(name, "UTF-8");
                url += "&callback=myFunction";
                String get = sendGet(url);
                if (!get.contains("{") || !get.contains("}")) {
                    continue;
                }
                System.out.println(get);
                int startIndex = get.indexOf("{");
                int endIndex = get.indexOf("}") + 1;
                String json = get.substring(startIndex, endIndex);
                if (json == null) {
                    continue;
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(StockAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // HTTP GET request
    private static String sendGet(String url) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("X-Originating-Ip", "79.107.243.18");

            int responseCode = con.getResponseCode();
//            System.out.println("\nSending 'GET' request to URL : " + url);
//            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            return response.toString();
        } catch (MalformedURLException ex) {
            Logger.getLogger(StockAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StockAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
