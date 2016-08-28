/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextractor;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

/**
 *
 * @author alarv
 */
public class Results {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MongoDB db = new MongoDB("alarv", "220757", "features");
        BasicDBObject obj = (BasicDBObject) db.findOne(new BasicDBObject(), "features");
        System.out.println(obj);
    }

}
