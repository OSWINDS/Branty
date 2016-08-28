/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import static com.kallisto.Preprocess.preprocessForSentiWordNet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 *
 * @author alarv
 */
public class WEKA {
    private SWN3 swn;
    private Instances train;
    private FilteredClassifier fc;
    private Instances test;

    public WEKA() {
        try {
            train = DataSource.read("tweetsOutput.arff");
            int cIdx = train.numAttributes() - 1;
            train.setClassIndex(cIdx);


    //        Instances test = DataSource.read("test.arff");
    //        cIdx = test.numAttributes()-1;
    //        test.setClassIndex(cIdx);
            // filter
            SnowballStemmer stemmer = new SnowballStemmer();
            stemmer.setStemmer("english");
            StringToWordVector STWfilter = new StringToWordVector(1000);
    //        weka.filters.unsupervised.attribute.StringToWordVector -R first-last -W 1000 -prune-rate -1.0 -N 1 -stemmer weka.core.stemmers.SnowballStemmer -M 1 -tokenizer "weka.core.tokenizers.WordTokenizer -delimiters \" \\r\\n\\t.,;:\\\'\\\"()?!\""
            STWfilter.setUseStoplist(true);
            STWfilter.setIDFTransform(true);
            STWfilter.setTFTransform(true);
            STWfilter.setNormalizeDocLength(new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL, StringToWordVector.TAGS_FILTER));;
            STWfilter.setOutputWordCounts(true);
            STWfilter.setStemmer(stemmer);

            STWfilter.setTokenizer(new WordTokenizer());
            STWfilter.setInputFormat(train);
    //        Remove rm = new Remove();
    //        rm.setAttributeIndices("1");  // remove 1st attribute
            // classifier
//            LibLINEAR linearSVM = new LibLINEAR();
//            linearSVM.setSVMType(new SelectedTag(2,linearSVM.TAGS_SVMTYPE));
//            linearSVM.setConvertNominalToBinary(true);
            SMO smo = new SMO();
            // meta-classifier
            fc = new FilteredClassifier();
            fc.setFilter(STWfilter);
            fc.setClassifier(smo);
            // train and make predictions
            fc.buildClassifier(train);
            //classifier is build and ready to accept new data

            swn = new SWN3();
        } catch (Exception ex) {
            Logger.getLogger(WEKA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getTweetSentiment(String text) throws Exception {

//        JFrame frame = new JFrame();
//        frame.setVisible(true);
//        frame.setLocationRelativeTo(null);


//        String text = (String) JOptionPane.showInputDialog(
//                frame,
//                "Give tweet:",
//                "Tweet tester",
//                JOptionPane.PLAIN_MESSAGE,
//                null,
//                null,
//                "");
        if (text == null) {
            return null;
        }
        text = preprocessForSentiWordNet(text);
        String[] words = text.split(" ");
        double sum = 0;
        int posCount = 0, negCount = 0, neutralCount = 0;
        for (int i = 0; i < words.length; i++) {
            double wordSentiment = swn.getWordSentiment(words[i]);
            if (wordSentiment > 0) {
                posCount++;
            } else if (wordSentiment < 0) {
                negCount++;
            } else if (wordSentiment == 0) {
                neutralCount++;
            }
            sum += wordSentiment;
        }


        // Declare attributes
        Attribute Attribute1 = new Attribute("tweet", (ArrayList<String>) null);
        Attribute Attribute2 = new Attribute("sentiwordnetpositive");
        Attribute Attribute3 = new Attribute("sentiwordnetnegative");
        Attribute Attribute4 = new Attribute("sentiwordnetneutral");
        Attribute Attribute5 = new Attribute("sentiwordnettotals");

        // Declare the class attribute along with its values
        ArrayList<String> fvClassVal = new ArrayList<String>();
        fvClassVal.add("positive");
        fvClassVal.add("negative");
        fvClassVal.add("neutral");
        Attribute ClassAttribute = new Attribute("sentiment", fvClassVal);

        // Declare the feature vector
        ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
        fvWekaAttributes.add(Attribute1);
        fvWekaAttributes.add(Attribute2);
        fvWekaAttributes.add(Attribute3);
        fvWekaAttributes.add(Attribute4);
        fvWekaAttributes.add(Attribute5);
        fvWekaAttributes.add(ClassAttribute);

        // Create an empty training set
        test = new Instances("Rel", fvWekaAttributes, 10);
        // Set class index
        test.setClassIndex(5);

        // Create the instance
            
        Instance iExample = new SparseInstance(6);
        iExample.setValue((Attribute) fvWekaAttributes.get(0), text);
        iExample.setValue((Attribute) fvWekaAttributes.get(1), (double) 1.0 * posCount / words.length);
        iExample.setValue((Attribute) fvWekaAttributes.get(2), (double) 1.0 * negCount / words.length);
        iExample.setValue((Attribute) fvWekaAttributes.get(3), (double) 1.0 * neutralCount / words.length);
        iExample.setValue((Attribute) fvWekaAttributes.get(4), sum);
//        iExample.setValue((Attribute) fvWekaAttributes.elementAt(2), "?");

//        iExample.setClassMissing();
        iExample.setDataset(train);

        // add the instance
        test.add(iExample);

//        for (int i = 0; i < test.numInstances(); i++) {
//            System.out.println(test.instance(i));
        double pred = fc.classifyInstance(test.instance(0));
        //            System.out.print("ID: " + test.instance(i).value(0));
        //            System.out.print(", actual: " + test.classAttribute().value((int) test.instance(i).classValue()));
        double[] distributionForInstance = fc.distributionForInstance(test.instance(0));
        return test.classAttribute().value((int) pred);
//        }
    }
    
    public double [] getDistValue() throws Exception{
        return fc.distributionForInstance(test.instance(0));
    }
    /**
     * Performs the cross-validation. See Javadoc of class for information on
     * command-line parameters.
     *
     * @param args	the command-line parameters
     * @throws Exception	if something goes wrong
     */
    public void test() throws Exception {

        // load data
        Instances train = DataSource.read("secondTest.arff");

        StringToWordVector filter = new StringToWordVector(1000);
        filter.setOutputWordCounts(true);
        filter.setInputFormat(train);

        train = Filter.useFilter(train, filter);

        train.setClassIndex(1);


        Instances test = DataSource.read("firstTest.arff");
//        filter.setInputFormat(test);
        test = Filter.useFilter(test, filter);

        test.setClassIndex(1);
        if (!train.equalHeaders(test)) {
            throw new IllegalArgumentException(
                    "Train and test set are not compatible!");
        }

        // train classifier
        SMO cls = new SMO();
        cls.buildClassifier(train);

        // output predictions
        System.out.println("# - actual - predicted - error - distribution");
        for (int i = 0; i < test.numInstances(); i++) {
            double pred = cls.classifyInstance(test.instance(i));
            double[] dist = cls.distributionForInstance(test.instance(i));
            System.out.print((i + 1));
            System.out.print(" - ");
            System.out.print(test.instance(i).toString(test.classIndex()));
            System.out.print(" - ");
            System.out.print(test.classAttribute().value((int) pred));
            System.out.print(" - ");
            if (pred != test.instance(i).classValue()) {
                System.out.print("yes");
            } else {
                System.out.print("no");
            }
            System.out.print(" - ");
            System.out.print(Utils.arrayToString(dist));
            System.out.println();
        }
    }

    public void test2() throws Exception {
       /* //weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0"
        // loads data and set class index
        Instances data = DataSource.read("secondTest.arff");
        StringToWordVector STWfilter = new StringToWordVector(1000);
        STWfilter.setOutputWordCounts(true);
        STWfilter.setInputFormat(data);

        data = Filter.useFilter(data, STWfilter);

        data.setClassIndex(1);
//        data.
//        String clsIndex = "last";
//        if (clsIndex.length() == 0) {
//            clsIndex = "last";
//        }
//        if (clsIndex.equals("first")) {
//            data.setClassIndex(0);
//        } else if (clsIndex.equals("last")) {
//            data.setClassIndex(data.numAttributes() - 1);
//        } else {
//            data.setClassIndex(Integer.parseInt(clsIndex) - 1);
//        }

        //test data
        Instances test = DataSource.read("test.arff");
//        STWfilter.setInputFormat(test);
        test = Filter.useFilter(test, STWfilter);

        test.setClassIndex(1);



        // classifier
        String[] tmpOptions;
        String classname;
        tmpOptions = Utils.splitOptions("weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"");
        classname = tmpOptions[0];
        tmpOptions[0] = "";
        Classifier cls = (Classifier) Utils.forName(Classifier.class, classname, tmpOptions);

        // other options
        int seed = 1;
        int folds = 10;

        // randomize data
        Random rand = new Random(seed);
        Instances randData = new Instances(data);
        randData.randomize(rand);
        if (randData.classAttribute().isNominal()) {
            randData.stratify(folds);
        }

        // perform cross-validation and add predictions
        Instances predictedData = null;
        Evaluation eval = new Evaluation(randData);
        for (int n = 0; n < folds; n++) {
            Instances train = randData.trainCV(folds, n);

//            Instances test = randData.testCV(folds, n);
            // the above code is used by the StratifiedRemoveFolds filter, the
            // code below by the Explorer/Experimenter:
            // Instances train = randData.trainCV(folds, n, rand);

            // build and evaluate classifier
            Classifier clsCopy = Classifier.makeCopy(cls);
            clsCopy.buildClassifier(train);
            eval.evaluateModel(clsCopy, test);

            // add predictions
            AddClassification filter = new AddClassification();
            filter.setClassifier(cls);
            filter.setOutputClassification(true);
            filter.setOutputDistribution(true);
            filter.setOutputErrorFlag(true);
            filter.setInputFormat(train);
            Filter.useFilter(train, filter);  // trains the classifier
            Instances pred = Filter.useFilter(test, filter);  // perform predictions on test set
            if (predictedData == null) {
                predictedData = new Instances(pred, 0);
            }
            for (int j = 0; j < pred.numInstances(); j++) {
                predictedData.add(pred.instance(j));
            }
        }

        // output evaluation
        System.out.println();
        System.out.println("=== Setup ===");
        System.out.println("Classifier: " + cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions()));
        System.out.println("Dataset: " + data.relationName());
        System.out.println("Folds: " + folds);
        System.out.println("Seed: " + seed);
        System.out.println();
        System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));

        // output "enriched" dataset
        DataSink.write("resultsfromcode.arff", predictedData);

        for (int i = 0; i < predictedData.numInstances(); i++) {
            System.out.println(predictedData.instance(i).stringValue(1));
        }*/
    }

    public static void main(String[] args) throws Exception {
        WEKA weka = new WEKA();
        String text = "This is like the worst product I've ever seen";
        System.out.println(weka.getTweetSentiment(text));
    }
}
