/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import com.mongodb.BasicDBObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author alarv
 */
public class KloutAPI {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static MongoDB db;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        String kloutKey = "gxbgfm2dpd4z6684pjkxaek3";

        db = new MongoDB("alarv","220757");
        long[] allIds = db.getAllIds("final_brands");


        for (int i = 0; i < allIds.length; i++) {
            try {
                if (i % 10 == 0) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(KloutAPI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                String url = "http://api.klout.com/v2/user.json/";
                url += allIds[i];
                url += "/";
                url += "score";
                url += "?";
                url += "key=";
                url += kloutKey;
                
                
                System.out.println(url);
                String json = sendGet(url);
                if (json == null) {
                    continue;
                }
                JSONParser parser = new JSONParser();
//                System.out.println(json);
                JSONObject object = (JSONObject) parser.parse(json);
                double score = (double) object.get("score");
                System.out.println(score);
//                db.insertFieldInto(new BasicDBObject("user_id", allIds[i]), new BasicDBObject("klout_id", kloutID), "test");
            } catch (ParseException ex) {
                Logger.getLogger(KloutAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void getIDs() throws InterruptedException, ParseException{
        String kloutKey = "gxbgfm2dpd4z6684pjkxaek3";

        db = new MongoDB();
        long[] allIds = db.getAllIds("test");


        for (int i = 0; i < allIds.length; i++) {
            if (i % 10 == 0) {
                Thread.sleep(3000);
            }
            String url = "http://api.klout.com/v2/identity.json/tw/";
            url += allIds[i];
            url += "?";
            url += "key=";
            url += kloutKey;


            String json = sendGet(url);
            if (json == null) {
                continue;
            }
            JSONParser parser = new JSONParser();
            System.out.println(json);
            JSONObject object = (JSONObject) parser.parse(json);
            String kloutID = (String) object.get("id");
            System.out.println(kloutID);
            db.insertFieldInto(new BasicDBObject("user_id", allIds[i]), new BasicDBObject("klout_id", kloutID), "test");

//            JsonReader reader = new JsonReader(new StringReader(json));
//            handleObject(reader);
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
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

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

            Logger.getLogger(KloutAPI.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(KloutAPI.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
//    /**
//     * Handle an Object. Consume the first token which is BEGIN_OBJECT. Within
//     * the Object there could be array or non array tokens. We write handler
//     * methods for both. Noe the peek() method. It is used to find out the type
//     * of the next token without actually consuming it.
//     *
//     * @param reader
//     * @throws IOException
//     */
//    private static void handleObject(JsonReader reader) throws IOException {
//        reader.beginObject();
//        while (reader.hasNext()) {
//            JsonToken token = reader.peek();
//            if (token.equals(JsonToken.BEGIN_ARRAY)) {
//                handleArray(reader);
//            } else if (token.equals(JsonToken.END_ARRAY)) {
//                reader.endObject();
//                return;
//            } else {
//                handleNonArrayToken(reader, token);
//            }
//        }
//
//    }
//
//    /**
//     * Handle a json array. The first token would be JsonToken.BEGIN_ARRAY.
//     * Arrays may contain objects or primitives.
//     *
//     * @param reader
//     * @throws IOException
//     */
//    public static void handleArray(JsonReader reader) throws IOException {
//        reader.beginArray();
//        while (true) {
//            JsonToken token = reader.peek();
//            if (token.equals(JsonToken.END_ARRAY)) {
//                reader.endArray();
//                break;
//            } else if (token.equals(JsonToken.BEGIN_OBJECT)) {
//                handleObject(reader);
//            } else {
//                handleNonArrayToken(reader, token);
//            }
//        }
//    }
//
//    /**
//     * Handle non array non object tokens
//     *
//     * @param reader
//     * @param token
//     * @throws IOException
//     */
//    public static void handleNonArrayToken(JsonReader reader, JsonToken token) throws IOException {
//        if (token.equals(JsonToken.NAME)) {
//            System.out.print(reader.nextName() + ":");
//        } else if (token.equals(JsonToken.STRING)) {
//
////            db.insertFieldInto(new BasicDBObject("user_id",currentId), new BasicDBObject("klout_id",reader.nextString()), "test");
//            System.out.println(reader.nextString());
//        } else if (token.equals(JsonToken.NUMBER)) {
//            System.out.println(reader.nextDouble());
//        } else {
//            reader.skipValue();
//        }
//    }
}
