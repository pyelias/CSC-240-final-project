
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import spamDetect.Email;
import spamDetect.EmailCategory;
import spamDetect.EmailText;
import spamDetect.Predictor;
import spamDetect.PredictorBuilder;


public class Main {
    // this fraction of the data will be used for testing
    static final double TEST_DATA_RATIO = 0.2;

    // default files to store things in
    static final String DATA_FILE = "spam_or_not_spam.csv";
    static final String TRAIN_FILE = "train.csv";
    static final String TEST_FILE = "test.csv";
    static final String UNLABELED_TEST_FILE = "unlabeled_test.csv";
    static final String TEST_OUT_FILE = "predictions.csv";
    static final String SPAM_MODEL_FILE = "cat-spam.csv";
    static final String HAM_MODEL_FILE = "cat-ham.csv";
    static final String ALL_MODEL_FILE = "cat-all.csv";

    public static void splitTrainTestData(ArrayList<Email> data, ArrayList<Email> testOut, ArrayList<Email> trainOut) throws IOException {
        int testAmount = (int)(data.size() * TEST_DATA_RATIO);

        Random rand = new Random();
        for (int i = 0; i < data.size(); i++) {
            if (rand.nextInt(data.size() - i) <= testAmount) {
                testOut.add(data.get(i));
                testAmount -= 1;
            } else {
                trainOut.add(data.get(i));
            }
        }
    }

    public static ArrayList<EmailText> removeLabels(ArrayList<Email> emails) {
        ArrayList<EmailText> result = new ArrayList<>();
        for (Email email : emails) {
            result.add(email.getText());
        }
        return result;
    }

    public static ArrayList<Email> makePredictions(ArrayList<EmailText> data, Predictor predictor) throws IOException {
        ArrayList<Email> predictions = new ArrayList<>();
        for (EmailText text : data) {
            boolean prediction = predictor.predictIsSpam(text);
            predictions.add(new Email(text, prediction));
        }
        return predictions;
    }

    public static Predictor readModelFromFiles() throws IOException {
        EmailCategory spamCat = EmailCategory.readFromFile(new File(SPAM_MODEL_FILE));
        EmailCategory hamCat = EmailCategory.readFromFile(new File(HAM_MODEL_FILE));
        EmailCategory allCat = EmailCategory.readFromFile(new File(ALL_MODEL_FILE));
        return new Predictor(spamCat, hamCat, allCat);
    }

    public static double evaluateModel(Predictor predictor, ArrayList<Email> data) {
        int spamCorrectCount = 0;
        int spamCount = 0;
        int hamCorrectCount = 0;
        int hamCount = 0;
        for (Email email : data) {
            boolean prediction = predictor.predictIsSpam(email.getText());
            if (email.isSpam()) {
                if (prediction) {
                    spamCorrectCount += 1;
                }
                spamCount += 1;
            } else {
                if (!prediction) {
                    hamCorrectCount += 1;
                }
                hamCount += 1;
            }
        }
        double spamAccuracy = (double)spamCorrectCount / spamCount;
        double hamAccuracy = (double)hamCorrectCount / hamCount;
        // make sure to weight each type of email equally
        // since there are fewer spam mails in the dataset
        return (spamAccuracy + hamAccuracy) / 2;
    }

    public static void cliHelp() {
        System.out.println("expected one of these commands:");
        System.out.println("  splitData [source train test unlabeled_test]");
        System.out.println("  buildModel [train]");
        System.out.println("  predict [unlabeled_test predict_out]");
        System.out.println("  evaluate [labeled_test]");
        System.out.println("[] is optional parameters");
    }

    public static void cliSplitData(String[] args) throws IOException {
        String dataFile;
        String trainFile;
        String labeledTestFile ;
        String unlabeledTestFile;
        switch (args.length) {
            case 1 -> {
                dataFile = DATA_FILE;
                trainFile = TRAIN_FILE;
                labeledTestFile = TEST_FILE;
                unlabeledTestFile = UNLABELED_TEST_FILE;
            }
            case 5 -> {
                dataFile = args[1];
                trainFile = args[2];
                labeledTestFile = args[3];
                unlabeledTestFile = args[4];
            }
            default -> {
                System.out.println("splitData needs 0 or 4 parameters");
                return;
            }
        }

        ArrayList<Email> data = Email.readListFromFile(new File(dataFile));
        ArrayList<Email> trainData = new ArrayList<>();
        ArrayList<Email> testData = new ArrayList<>();
        splitTrainTestData(data, trainData, testData);
        Email.writeListToFile(trainData, new File(trainFile));
        Email.writeListToFile(testData, new File(labeledTestFile));
        EmailText.writeListToFile(removeLabels(testData), new File(unlabeledTestFile));
    }

    public static void cliBuildModel(String[] args) throws IOException {
        String trainFile;
        switch (args.length) {
            case 1 -> {
                trainFile = TRAIN_FILE;
            }
            case 2 -> {
                trainFile = args[1];
            }
            default -> {
                System.out.println("buildModel needs 0 or 1 parameters");
                return;
            }
        }

        ArrayList<Email> emails = Email.readListFromFile(new File(trainFile));
        PredictorBuilder builder = new PredictorBuilder();
        builder.addEmails(emails);
        Predictor predictor = builder.build();

        predictor.getSpamCategory().writeToFile(new File(SPAM_MODEL_FILE));
        predictor.getHamCategory().writeToFile(new File(HAM_MODEL_FILE));
        predictor.getAllCategory().writeToFile(new File(ALL_MODEL_FILE));
    }

    public static void cliPredict(String[] args) throws IOException {
        String testFile;
        String testOutFile;
        switch (args.length) {
            case 1 -> {
                testFile = UNLABELED_TEST_FILE;
                testOutFile = TEST_OUT_FILE;
            }
            case 3 -> {
                testFile = args[1];
                testOutFile = args[2];
            }
            default -> {
                System.out.println("predict needs 0 or 2 parameters");
                return;
            }
        }

        Predictor predictor = readModelFromFiles();
        ArrayList<EmailText> testData = EmailText.readListFromFile(new File(testFile));
        ArrayList<Email> predictions = makePredictions(testData, predictor);
        Email.writeListToFile(predictions, new File(testOutFile));
    }

    public static void cliEvaluate(String[] args) throws IOException {
        String testFile;
        switch (args.length) {
            case 1 -> {
                testFile = TEST_FILE;
            }
            case 2 -> {
                testFile = args[1];
            }
            default -> {
                System.out.println("evaluate needs 0 or 1 parameters");
                return;
            }
        }

        Predictor predictor = readModelFromFiles();
        ArrayList<Email> testData = Email.readListFromFile(new File(testFile));
        double accuracy = evaluateModel(predictor, testData);
        System.out.println("accuracy: " + Math.round(100 * accuracy) + "%");
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            cliHelp();
            return;
        }

        switch (args[0]) {
            case "splitData" -> {
                cliSplitData(args);
            }
            case "buildModel" -> {
                cliBuildModel(args);
            }
            case "predict" -> {
                cliPredict(args);
            }
            case "evaluate" -> {
                cliEvaluate(args);
            }
            default -> {
                cliHelp();
            }
        }
    }
}
