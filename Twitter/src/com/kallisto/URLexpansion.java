/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alarv
 */
public class URLexpansion {

    public static void main(String[] args) {
        MongoDB db = new MongoDB();
//        db.insertFieldInto(new BasicDBObject("name", "adidas"), new BasicDBObject("url", "anna.com"), "brands");



        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream("urls.txt");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                String[] split = strLine.split("\t");

                HttpURLConnection.setFollowRedirects(false);
                URL url = new URL(split[1]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                String newLocation = conn.getHeaderField("Location");

                System.out.print("name:" + split[0]);
                System.out.print(", shortened:" + split[1]);
                System.out.println(", url:" + newLocation);
                List<String> list = new ArrayList();
                list.add("@"+split[0]);
                BasicDBObject query = new BasicDBObject("profile", new BasicDBObject("$in", list));
                
                BasicDBList urlList = new BasicDBList();
                urlList.add(new BasicDBObject("shortened",split[1]));
                urlList.add(new BasicDBObject("expanded",newLocation));
                
                System.out.println(query);
                db.insertFieldInto(query, new BasicDBObject("url", urlList), "brands");

            }
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
}
