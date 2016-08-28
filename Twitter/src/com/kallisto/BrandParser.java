/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author anna
 */
public class BrandParser {

    public static MongoDB db = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        db = new MongoDB();

        parseFanPageList();
    }

    private static void parseKelkoo() {
        Document doc;
        Elements brand = null, photo = null, categories = null;

        for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {
            try {
                doc = Jsoup.connect("http://www.kelkoo.co.uk/bz-" + alphabet + "-brands?currentPage=1&numPerPage=140").get();
                brand = doc.body().select("table.bd td.brand-name a");
                photo = doc.body().select("table.bd td.brand-name a img");
                categories = doc.body().select("table.bd td.popcats-per-brand ul");
                System.out.println("************************ LETTER " + alphabet + " *******************************");
//                db.insert();
//                printElements(brand, photo, categories);
            } catch (IOException ex) {
                Logger.getLogger(BrandParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void printElements(Elements brands, Elements photos, Elements categoriesWithCommas) {
        List<String> list = new ArrayList<>();
        System.out.println("--------------------Brands------------------");
        for (int i = 0; i < brands.size(); i++) {
            String brand = brands.get(i).text();
            String photo = photos.get(i).absUrl("src");
            String[] categories = categoriesWithCommas.get(i).text().split(",");

//            BasicDBObject subCategories = new BasicDBObject("subcategories", categories);

            BasicDBObject doc = new BasicDBObject("name", brand).
                    append("imgurl", photo).
                    append("subcategories", categories);
            db.insert(doc, "brands");
//            String SQL = "insert into brands(name,imgurl,subcategories) values('"+brand.text()+"','"+photo.absUrl("src")+"','"+category.text().replaceAll("'", "\'")+"')";
//            System.out.println(SQL);
//            db.insert(SQL);
        }
//        list.clear();
//        System.out.println("--------------------Photo------------------");
//        for (int i = 0; i < photo.size(); i++) {
//            Element para = photo.get(i);
//            list.add(para.absUrl("src"));
//            System.out.println(list.get(i));
//        }
//        list.clear();
//        System.out.println("--------------------Categories------------------");
//        for (int i = 0; i < categories.size(); i++) {
//            Element para = categories.get(i);
//            list.add(para.text());
//            System.out.println(list.get(i));
//        }
    }

    private static void parseFanPageList() {
        try {
            Document doc = Jsoup.connect("http://fanpagelist.com/category/brands/retail/")
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com").get();
            Elements links = doc.select("a > span.title"); // a with href
            Elements imgs = doc.select("img.ranking_profile_image"); // a with href



            for (int i = 0; i < links.size(); i++) {
                String brand = links.get(i).text();
                String photo = imgs.get(i).absUrl("src");
                String[] categories = {"Retail"};

                System.out.println(brand);
                System.out.println(photo);
                System.out.println(categories[0]);


                BasicDBObject query = new BasicDBObject("name", brand);
                DBCursor cursor = db.find(query, "brands");

                if (!cursor.hasNext()) {

                    BasicDBObject dbobject = new BasicDBObject("name", brand).
                            append("imgurl", photo).
                            append("subcategories", categories);
//                    db.insert(dbobject, "brands");
                }
            }


        } catch (IOException ex) {
            Logger.getLogger(BrandParser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}