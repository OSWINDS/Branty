/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextractor;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class MongoDB {

//    private static DBCollection coll;
    private DB db = null;

    MongoDB() {

        //database connection here
    }

    MongoDB(String username, String password) {
	//database connection here
    }
    
    
    MongoDB(String username, String password, String features) {
	//database connection here
    }

    /**
     * @param args the command line arguments
     */
    public boolean insert(BasicDBObject dbObject, String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        collection.insert(dbObject);
        return true;
//        BasicDBObject query = new BasicDBObject("text", text);
//        DBCursor cursor = coll.find(query);
//        if (!cursor.hasNext()) {
//            coll.insert(doc);
//        }
//        cursor.close();
    }

    public boolean insertFieldInto(BasicDBObject query, BasicDBObject field, String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.append("$set", field);
        collection.update(query, newDocument);
        return true;
    }

    public DBCursor find(BasicDBObject query, String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        return collection.find(query);
    }

    public DBObject findOne(BasicDBObject query, String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        return collection.findOne(query);
    }

    DBCursor findWithField(BasicDBObject query, BasicDBObject field, String collectionName) {
        return db.getCollection(collectionName).find(query, field);
    }

    public void updateTweets(BasicDBObject tweetToInsert, String objectToInsertOn, String collectionName) {
        DBCollection collection = db.getCollection(collectionName);

        BasicDBObject searchQuery = new BasicDBObject().append("name", objectToInsertOn);
        List<String> list = new ArrayList();
        list.add("@" + objectToInsertOn);
        BasicDBObject profileQuery = new BasicDBObject("profile", new BasicDBObject("$in", list));

        BasicDBList or = new BasicDBList();
        or.add(searchQuery);
        or.add(profileQuery);

        DBCursor cursor = collection.find(new BasicDBObject("$or", or));
        if (!cursor.hasNext()) {//if empty could not find name
            return;
        }
        BasicDBList tweetsInDB = (BasicDBList) cursor.next().get("tweets");

        if (tweetsInDB == null || tweetsInDB.isEmpty()) {
            tweetsInDB = new BasicDBList();
        }

        if (!tweetsInDB.contains(tweetToInsert)) {
            tweetsInDB.add(tweetToInsert);
        }

        BasicDBObject newDocument = new BasicDBObject();
        newDocument.append("$set", new BasicDBObject().append("tweets", tweetsInDB));

        collection.update(searchQuery, newDocument);
    }

    public void setProfilesList(DBObject brand) {
        Object profiles = brand.get("profile");

        if (profiles instanceof BasicDBList) {
            return;
        } else if (profiles instanceof String) {
            BasicDBObject searchQuery = new BasicDBObject().append("name", brand.get("name"))
                    .append("imgurl", brand.get("imgurl"));
            DBCollection collection = db.getCollection("brands");
            BasicDBList profilesToUpdate = new BasicDBList();
            profilesToUpdate.add(profiles);

            BasicDBObject newDocument = new BasicDBObject();
            newDocument.append("$set", new BasicDBObject().append("profile", profilesToUpdate));
//            System.out.println("peirazw to " + profiles);
            collection.update(searchQuery, newDocument);
            return;
        } else {
            try {
                throw new Exception("problem recognizing file type");
            } catch (Exception ex) {
                Logger.getLogger(MongoDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Object getProfilesList(DBObject brand) {
        Object profiles = brand.get("profile");
        if (profiles instanceof BasicDBList) {
            return "list!";
        } else if (profiles instanceof BasicDBObject) {
            return "basic object!";
        } else if (profiles instanceof DBObject) {
            return "object!";
        } else if (profiles instanceof String) {
            return "string!" + profiles;
        }
        return "dafuq";
    }

    public void updateBrandsWithOne(ArrayList<String> duplicates, BasicDBObject toKeep, String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        BasicDBList profilesToAddToArray = new BasicDBList();

        profilesToAddToArray.add(toKeep.getString("profile"));
        for (int duplicate = 0; duplicate < duplicates.size(); duplicate++) {
            DBCursor cursorDuplicates = (DBCursor) collection.find(new BasicDBObject("name", duplicates.get(duplicate)));
            if (cursorDuplicates.hasNext()) {
                DBObject toRemove = (DBObject) cursorDuplicates.next();
                profilesToAddToArray.add((String) toRemove.get("profile"));
                System.out.println(toRemove.get("profile"));
                collection.remove(toRemove);
            }
        }

        BasicDBObject newDocument = new BasicDBObject();
        newDocument.append("$set", new BasicDBObject().append("profile", profilesToAddToArray));
        BasicDBObject searchQuery = new BasicDBObject().append("name", toKeep.getString("name"));

        collection.update(searchQuery, newDocument);

//        BasicDBObject searchQuery = new BasicDBObject().append("name", toKeep);
    }

    public long[] getAllIds(String collectionName) {
        BasicDBObject query = new BasicDBObject();
        DBCollection collection = db.getCollection(collectionName);
        DBCursor cursor = collection.find(query, new BasicDBObject("user_id", 1));
        ArrayList<Long> listofids = new ArrayList();

        while (cursor.hasNext()) {
            BasicDBObject next = (BasicDBObject) cursor.next();
            long userID = (long) next.getLong("user_id");
            listofids.add(userID);
        }
        long[] brandArray = new long[listofids.size()];
        for (int i = 0; i < listofids.size(); i++) {
            brandArray[i] = (long) listofids.get(i);
        }
        return brandArray;
    }

    public String[] getAllNamesAndProfiles(String collectionName) {
        int counter = 0;
        BasicDBObject query = new BasicDBObject();
        DBCollection collection = db.getCollection(collectionName);
        DBCursor cursor = collection.find(query);
        ArrayList<BasicDBList> listofprofiles = new ArrayList();

        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            BasicDBList profile = ((BasicDBList) next.get("profile"));
            profile.add(next.get("name"));
            listofprofiles.add(profile);
            counter += ((BasicDBList) next.get("profile")).size();
        }
        String[] brandArray = new String[counter];
        counter = 0;
        for (int i = 0; i < listofprofiles.size(); i++) {
            for (int j = 0; j < listofprofiles.get(i).size(); j++) {
                brandArray[counter] = (String) listofprofiles.get(i).get(j).toString();
                counter++;
            }
        }

        return brandArray;
    }

    public String[] getAllNames(String collectionName) {
        BasicDBObject query = new BasicDBObject();
        DBCollection collection = db.getCollection(collectionName);
        DBCursor cursor = collection.find(query, new BasicDBObject("name", 1));
        ArrayList<String> names = new ArrayList();

        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            String name = (String) next.get("name");
            names.add(name);
        }
        String[] brandArray = new String[names.size()];
        for (int i = 0; i < names.size(); i++) {
            brandArray[i] = (String) names.get(i).toString();
        }

        return brandArray;
    }

    public String[] getAllProfiles(String collectionName) {
        int counter = 0;
        BasicDBObject query = new BasicDBObject();
        DBCollection collection = db.getCollection(collectionName);
        DBCursor cursor = collection.find(query, new BasicDBObject("profile", 1));
        ArrayList<BasicDBList> listofprofiles = new ArrayList();

        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            BasicDBList profile = ((BasicDBList) next.get("profile"));
//            if (profile.size() <= 1) {
//                continue;
//            }
            listofprofiles.add(profile);
            counter += ((BasicDBList) next.get("profile")).size();
        }
        String[] brandArray = new String[counter];
        counter = 0;
        for (int i = 0; i < listofprofiles.size(); i++) {
            for (int j = 0; j < listofprofiles.get(i).size(); j++) {
                brandArray[counter] = (String) listofprofiles.get(i).get(j).toString().replaceAll("@", "");
                counter++;
            }
        }

        return brandArray;
    }

    String[] getAllQueryParameters(String collectionName) {
        BasicDBObject query = new BasicDBObject();
        DBCollection collection = db.getCollection(collectionName);
        DBCursor cursor = collection.find(query);
        ArrayList<BasicDBList> listofprofiles = new ArrayList();

        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            BasicDBList profile = ((BasicDBList) next.get("profile"));
            profile.add(0, (String) next.get("name"));

            listofprofiles.add(profile);
//            counter += ((BasicDBList) next.get("profile")).size();
        }
        String[] brandArray = new String[listofprofiles.size()];
        for (int i = 0; i < listofprofiles.size(); i++) {
            brandArray[i] = (String) listofprofiles.get(i).get(0).toString();
            for (int j = 1; j < listofprofiles.get(i).size() && j < 5; j++) {
                brandArray[i] += "," + listofprofiles.get(i).get(j).toString();
            }
        }

        return brandArray;
    }

//    public HashMap<String, BasicDBList> getBrands(String collectionName) {
//        int counter = 0;
//        BasicDBObject query = new BasicDBObject();
//        DBCollection collection = db.getCollection(collectionName);
//        DBCursor cursor = collection.find(query);
//        HashMap<String, Integer> map = new HashMap();
//        while (cursor.hasNext()) {
//            DBObject next = cursor.next();
//            String name = (String) ((BasicDBList) next.get("profile")).get(0);
//            if (next.get("tweets") != null) {
//                map.put(name, ((BasicDBList) next.get("tweets")).size());
//            } else {
//                map.put(name, 0);
//            }
//            //counter += ((BasicDBList) next.get("profile")).size();
//            counter++;
//        }
//        return null;
//
//    }
    void findSubcategory(String subcategory, String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject(new BasicDBObject("subcategories", "test"));
        DBCursor cursor = collection.find(query);
    }

    public boolean updateFeatures(String brandName, BasicDBObject features) {
        DBCollection collection = db.getCollection("features");
        BasicDBObject listItem = new BasicDBObject("features", features);
        BasicDBObject newDocument = new BasicDBObject("$push", listItem);
        BasicDBObject searchQuery = new BasicDBObject("name", brandName);
        collection.update(searchQuery, newDocument);
        return true;
    }

    public static void main(String[] args) throws UnknownHostException {

        MongoDB db = new MongoDB("alarv", "220757");
        DBObject findOne = db.findOne(new BasicDBObject("category", "Technology"), "features");
        System.out.println(findOne.get("name"));
        db.insertFieldInto(new BasicDBObject("_id",findOne.get("_id")), new BasicDBObject("test","Test"), "features");
        
        
//        while (cursor.hasNext()) {
//            DBObject next = cursor.next();
//            BasicDBList featuresList = (BasicDBList) next.get("features");
//            
//            System.out.print(next.get("name")+",");
//            
//            BasicDBObject features = (BasicDBObject) featuresList.get(featuresList.size() - 1);
//            Set<Map.Entry<String, Object>> entrySet = features.entrySet();
//            for (Map.Entry<String, Object> obj : entrySet) {
//                switch (obj.getKey()) {
//                    case "date":
//                        continue;
//                    case "followers_div_friends":
//                        System.out.println(obj.getValue());
//                        break;
//                    default:
//                        System.out.print(obj.getValue()+",");
//                        break;
//                }
//            }
//
////            for(int i = 0;i < features.size();i++){
////                System.out.println(features.get(i));
////            }
//        }
        //testing MongoDB.
        //setProfilesList
        //        MongoDB db = new MongoDB("alarv", "pokemondota666", 1);
        //        MongoDB db1 = new MongoDB();
        //
        //        BasicDBObject obj = new BasicDBObject();
        //
        //        DBCursor find = db.find(obj, "brands");
        //        while (find.hasNext()) {
        //            DBObject next = find.next();
        ////            System.out.println(find.next());
        //            BasicDBObject toInsert = new BasicDBObject()
        //                    .append("category", next.get("category"))
        //                    .append("imgurl", next.get("imgurl"))
        //                    .append("name", next.get("name"))
        //                    .append("profile", next.get("profile"));
        ////            System.out.println(toInsert);
        //            db1.insert(toInsert, "brands");
        //        }
        //        DBCursor find = db.find(new BasicDBObject(), "brands");
        //        while (find.hasNext()) {
        //            db.setProfilesList(find.next());
        //        }
        //        BasicDBList profiles = (BasicDBList) db.findOne(
        //                new BasicDBObject("name", "Nike"), "brands").get("profile");
        //        String buildQuery = "Nike";
        //        for (int a = 0; a < profiles.size(); a++) {
        //            buildQuery += " OR " + profiles.get(a);
        //        }
        //        System.out.println(buildQuery);
        //        BasicDBObject query = new BasicDBObject();
        //        BasicDBObject field = new BasicDBObject();
        //        field.put("text", 1);
        //        DBCursor cursor = coll.find(query, field);
        //        while (cursor.hasNext()) {
        //            BasicDBObject obj = (BasicDBObject) cursor.next();
        //            String text = obj.getString("text");
        //            if (!text.contains("\n")) {
        //                System.out.println(text);
        //            }
        //        }
        //        mongoDB.Insert("alarv","Oh hai db","22/10/2013");
        //        BasicDBObject doc = new BasicDBObject("name", "MongoDB").
        //                append("type", "database").
        //                append("count", 1).
        //                append("info", new BasicDBObject("x", 203).append("y", 102));
        //
        //        mongoDB.insert(doc, "categories");
    }

}
