/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextractor;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import java.util.ArrayList;
import java.util.Arrays;
import org.tartarus.snowball.ext.porterStemmer;

/**
 *
 * @author Alex
 */
public class Preprocess {

    public static String[] preprocess(String text) {
        String finaltext = "";
        String[] stopWords = {"a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "amoungst", "amount", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway", "anywhere", "are", "around", "as", "at", "back", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom", "but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven", "else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own", "part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", "the"};

        //to lowercase
        text = text.toLowerCase();

        //remove stop words
        for (int i = 0; i < stopWords.length; i++) {
            text = text.replaceAll(" " + stopWords[i] + " ", " ");
            text = text.replaceAll("^" + stopWords[i] + " ", " ");
            text = text.replaceAll(" " + stopWords[i] + "$", " ");
        }

        //replace references
        text = text.replaceAll("@[^\\s]+", " ");

        //replace urls
        text = text.replaceAll("http:[^\\s]+", " ");

        //keep emoticons for later use(join with final text)
        int[] emoticons = keepSpecialCharacters(text);

        //replace all punctuation, hashtags and digits with space
        text = text.replaceAll("\\p{Punct}|\\d", " ");

        //replace weird characters
        text = text.replaceAll("[^\\x20-\\x7e]", "");

        //stem terms
        String[] a = stemTerms(text);
        for (int i = 0; i < a.length; i++) {
            finaltext += a[i] + " ";
        }
        //rejoin emoticons, !, ? with stemmed text
//        for (int i = 0; i < emoticons.size(); i++) {
//            finaltext += emoticons.get(i) + " ";
//        }

        finaltext = finaltext.toLowerCase();
        //finaltext = finaltext.replaceAll("\\s", " ");
        finaltext = finaltext.replaceAll("( )+", " ");
        finaltext = finaltext.trim();

        String[] finalVector = new String[emoticons.length + 1];
        finalVector[0] = finaltext;
        for (int i = 1; i < finalVector.length; i++) {
            finalVector[i] = String.valueOf(emoticons[i - 1]);
        }

        
        
        return finalVector;
    }

    public static String preprocessForSentiWordNet(String text) {
        String finaltext = "";
//        String[] stopWords = {"a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "amoungst", "amount", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway", "anywhere", "are", "around", "as", "at", "back", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom", "but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven", "else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own", "part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", "the"};

        //to lowercase
        text = text.toLowerCase();

        //remove stop words
//        for (int i = 0; i < stopWords.length; i++) {
//            text = text.replaceAll(" " + stopWords[i] + " ", " ");
//            text = text.replaceAll("^" + stopWords[i] + " ", " ");
//            text = text.replaceAll(" " + stopWords[i] + "$", " ");
//        }

        //replace references
        text = text.replaceAll("@[^\\s]+", " ");

        //replace urls
        text = text.replaceAll("http:[^\\s]+", " ");

        //keep emoticons for later use(join with final text)
        String[] emoticons = keepSpecialCharactersString(text);

        //replace all punctuation, hashtags and digits with space
        text = text.replaceAll("\\p{Punct}|\\d", " ");

        //replace weird characters
        text = text.replaceAll("[^\\x20-\\x7e]", "");

        //stem terms
//        String[] a = stemTerms(text);
        String[] a = text.split("\\s+");
        for (int i = 0; i < a.length; i++) {
            finaltext += a[i] + " ";
        }

        finaltext = finaltext.toLowerCase();
        //finaltext = finaltext.replaceAll("\\s", " ");
        finaltext = finaltext.replaceAll("( )+", " ");
//        finaltext = finaltext.trim();

        //rejoin emoticons, !, ? with stemmed text
//        String[] finalVector = new String[emoticons.length + 1];
//        finalVector[0] = finaltext;
//        for (int i = 1; i < finalVector.length; i++) {
//            finalVector[i] = String.valueOf(emoticons[i - 1]);
//        }
        
        for(int i = 0;i < emoticons.length;i++){
            finaltext+= emoticons[i] + " ";
        }
        finaltext = finaltext.trim();

//        String returnString = "";
//        for(int i = 0;i < finalVector.length;i++){
//            returnString += finalVector[i];
//        }

        return finaltext;
    }

    static String stemTerm(String term) {
        porterStemmer stem = new porterStemmer();
        stem.setCurrent(term);
        stem.stem();
        return stem.getCurrent();
    }

    static String[] stemTerms(String term) {
        String[] terms = term.split("\\s+");

        for (int i = 0; i < terms.length; i++) {
            terms[i] = stemTerm(terms[i]);
        }
        return terms;
    }

    private static int[] keepSpecialCharacters(String text) {
        ArrayList<String> emoticons = new ArrayList();
        int[] emoticonsArray = new int[11];
        Arrays.fill(emoticonsArray, 0);

        String[] happyspecialCharacters = {":-)", ":)", ":o)", ":]", ":3", ":c)", ":>", "=]", "8)", "=)", ":}", ":^)", ":っ)", ":'-)", ":')"};
        for (int i = 0; i < happyspecialCharacters.length; i++) {
            if (text.contains(happyspecialCharacters[i])) {
                emoticons.add(":)");
                emoticonsArray[0]++;
            }
        }
        String[] laughingspecialCharacters = {":-D", ":D", "8-D", "8D", "x-D", "xD", "X-D", "XD", "=-D", "=D", "=-3", "=3", "B^D"};
        for (int i = 0; i < laughingspecialCharacters.length; i++) {
            if (text.contains(laughingspecialCharacters[i])) {
                emoticons.add(":D");
                emoticonsArray[1]++;
            }
        }
        String[] angryspecialCharacters = {":-||", ":@"};
        for (int i = 0; i < angryspecialCharacters.length; i++) {
            if (text.contains(angryspecialCharacters[i])) {
                emoticons.add(":@");
                emoticonsArray[2]++;
            }
        }
        String[] sadspecialCharacters = {">:[", ":-(", ":(", ":-c", ":c", ":-<", ":っC", ":<", ":-[", ":[", ":{", ":'-(", ":'("};
        for (int i = 0; i < sadspecialCharacters.length; i++) {
            if (text.contains(sadspecialCharacters[i])) {
                emoticons.add(":(");
                emoticonsArray[3]++;
            }
        }
        String[] surprisespecialCharacters = {">:O", ":-O", ":O", "°o°", "°O°", ":O", "o_O", "o_0", "o.O", "8-0"};
        for (int i = 0; i < surprisespecialCharacters.length; i++) {
            if (text.contains(surprisespecialCharacters[i])) {
                emoticons.add(":O");
                emoticonsArray[4]++;
            }
        }
        String[] disgustspecialCharacters = {"D:<", "D:", "D8", "D;", "D=", "DX", "v.v", "D-':"};
        for (int i = 0; i < disgustspecialCharacters.length; i++) {
            if (text.contains(disgustspecialCharacters[i])) {
                emoticons.add("D:");
                emoticonsArray[5]++;
            }
        }
        String[] winkspecialCharacters = {";-)", ";)", "*-)", "*)", ";-]", ";]", ";D", ";^)", ":-,"};
        for (int i = 0; i < winkspecialCharacters.length; i++) {
            if (text.contains(winkspecialCharacters[i])) {
                emoticons.add(";)");
                emoticonsArray[6]++;
            }
        }
        String[] cheekyspecialCharacters = {">:P", ":-P", ":P", "X-P", "x-p", "xp", "XP", ":-p", ":p", "=p", ":-Þ", ":Þ", ":-b", ":b"};
        for (int i = 0; i < cheekyspecialCharacters.length; i++) {
            if (text.contains(cheekyspecialCharacters[i])) {
                emoticons.add(":P");
                emoticonsArray[7]++;
            }
        }
        String[] annoyedspecialCharacters = {">:\\", ">:/", ":-/", ":-.", ":/", ":\\", "=/", "=\\", ":L", "=L", ":S", ">.<", "-_-", "-.-", "-__-"};
        for (int i = 0; i < annoyedspecialCharacters.length; i++) {
            if (text.contains(annoyedspecialCharacters[i])) {
                emoticons.add(":/");
                emoticonsArray[8]++;
            }
        }

        int exclCount = text.length() - text.replaceAll("!", "").length();
        emoticonsArray[9] = exclCount;
        int quesCount = text.length() - text.replaceAll("\\?", "").length();
        emoticonsArray[10] = quesCount;
        for (int i = 0; i < exclCount; i++) {
            emoticons.add("!");

        }
        for (int i = 0; i < quesCount; i++) {
            emoticons.add("?");
        }


        return emoticonsArray;

    }
    private static String[] keepSpecialCharactersString(String text) {
        ArrayList<String> emoticons = new ArrayList();
        int[] emoticonsArray = new int[11];
        Arrays.fill(emoticonsArray, 0);

        String[] happyspecialCharacters = {":-)", ":)", ":o)", ":]", ":3", ":c)", ":>", "=]", "8)", "=)", ":}", ":^)", ":っ)", ":'-)", ":')"};
        for (int i = 0; i < happyspecialCharacters.length; i++) {
            if (text.contains(happyspecialCharacters[i])) {
                emoticons.add("happyspecialcharacter");
                emoticonsArray[0]++;
            }
        }
        String[] laughingspecialCharacters = {":-D", ":D", "8-D", "8D", "x-D", "xD", "X-D", "XD", "=-D", "=D", "=-3", "=3", "B^D"};
        for (int i = 0; i < laughingspecialCharacters.length; i++) {
            if (text.contains(laughingspecialCharacters[i])) {
                emoticons.add("laughingspecialcharacter");
                emoticonsArray[1]++;
            }
        }
        String[] angryspecialCharacters = {":-||", ":@"};
        for (int i = 0; i < angryspecialCharacters.length; i++) {
            if (text.contains(angryspecialCharacters[i])) {
                emoticons.add("angryspecialcharacter");
                emoticonsArray[2]++;
            }
        }
        String[] sadspecialCharacters = {">:[", ":-(", ":(", ":-c", ":c", ":-<", ":っC", ":<", ":-[", ":[", ":{", ":'-(", ":'("};
        for (int i = 0; i < sadspecialCharacters.length; i++) {
            if (text.contains(sadspecialCharacters[i])) {
                emoticons.add("sadspecialcharacter");
                emoticonsArray[3]++;
            }
        }
        String[] surprisespecialCharacters = {">:O", ":-O", ":O", "°o°", "°O°", ":O", "o_O", "o_0", "o.O", "8-0"};
        for (int i = 0; i < surprisespecialCharacters.length; i++) {
            if (text.contains(surprisespecialCharacters[i])) {
                emoticons.add("surprisespecialcharacter");
                emoticonsArray[4]++;
            }
        }
        String[] disgustspecialCharacters = {"D:<", "D:", "D8", "D;", "D=", "DX", "v.v", "D-':"};
        for (int i = 0; i < disgustspecialCharacters.length; i++) {
            if (text.contains(disgustspecialCharacters[i])) {
                emoticons.add("disgustspecialcharacter");
                emoticonsArray[5]++;
            }
        }
        String[] winkspecialCharacters = {";-)", ";)", "*-)", "*)", ";-]", ";]", ";D", ";^)", ":-,"};
        for (int i = 0; i < winkspecialCharacters.length; i++) {
            if (text.contains(winkspecialCharacters[i])) {
                emoticons.add("winkspecialcharacter");
                emoticonsArray[6]++;
            }
        }
        String[] cheekyspecialCharacters = {">:P", ":-P", ":P", "X-P", "x-p", "xp", "XP", ":-p", ":p", "=p", ":-Þ", ":Þ", ":-b", ":b"};
        for (int i = 0; i < cheekyspecialCharacters.length; i++) {
            if (text.contains(cheekyspecialCharacters[i])) {
                emoticons.add("cheekyspecialcharacter");
                emoticonsArray[7]++;
            }
        }
        String[] annoyedspecialCharacters = {">:\\", ">:/", ":-/", ":-.", ":/", ":\\", "=/", "=\\", ":L", "=L", ":S", ">.<", "-_-", "-.-", "-__-"};
        for (int i = 0; i < annoyedspecialCharacters.length; i++) {
            if (text.contains(annoyedspecialCharacters[i])) {
                emoticons.add("annoyedspecialcharacter");
                emoticonsArray[8]++;
            }
        }

        int exclCount = text.length() - text.replaceAll("!", "").length();
        emoticonsArray[9] = exclCount;
        int quesCount = text.length() - text.replaceAll("\\?", "").length();
        emoticonsArray[10] = quesCount;
        for (int i = 0; i < exclCount; i++) {
            emoticons.add("exclamationmarkcharacter");

        }
        for (int i = 0; i < quesCount; i++) {
            emoticons.add("questionmarkcharacter");
        }

        
        
        return emoticons.toArray(new String[emoticons.size()]);

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MongoDB db = new MongoDB();
        DBCursor find = db.find(new BasicDBObject("tweets", new BasicDBObject("$exists", true)), "final_brands");

        while (find.hasNext()) {
            BasicDBList tweets = (BasicDBList) find.next().get("tweets");
            if(tweets == null) continue;
            for (int i = 0; i < tweets.size(); i++) {
                BasicDBObject tweet = (BasicDBObject) tweets.get(i);
                String tweetText = (String) tweet.get("text");
                System.out.println("####\n"+tweetText);
                System.out.println(preprocessForSentiWordNet(tweetText));
                System.out.println("####");
            }
        }
    }
}
