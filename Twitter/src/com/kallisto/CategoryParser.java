/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import static com.kallisto.BrandParser.db;
import com.mongodb.BasicDBObject;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author alarv
 */
public class CategoryParser {

    public static void main(String args[]) {
        db = new MongoDB();
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream("categoriesKELKOO.csv");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                if (!strLine.trim().equals("")) {
                    //                    System.out.println(strLine);
                    String[] split = strLine.replaceAll("\"", "").split(",");
                    System.out.print(split[0]+":");
                    ArrayList<String> list = new ArrayList<>();
                    for(int i = 1; i < split.length;i++){
                        list.add(split[i]);
                        System.out.print(split[i]+"\t");
                    }
                    System.out.println("");
                    
                    
                    BasicDBObject dbobject = new BasicDBObject("category_name", split[0]).
                            append("subcategories", list.isEmpty() ? new String[0] : list.toArray(new String[list.size()]));
                    db.insert(dbobject, "categories");
                } else {
                    System.out.println("w re pousth");
                }
                
            }
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
}
