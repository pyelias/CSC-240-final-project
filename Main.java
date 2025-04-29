
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import spamDetect.Email;
import spamDetect.EmailText;
import spamDetect.Predictor;
import spamDetect.PredictorBuilder;


public class Main {
    // this fraction of the data will be used for testing
    static final double TEST_DATA_RATIO = 0.2;

    public static void splitTrainTestData(File data, File train, File test) throws IOException {
        ArrayList<Email> emails = Email.readListFromFile(data);
        int testAmount = (int)(emails.size() * TEST_DATA_RATIO);

        ArrayList<Email> trainData = new ArrayList<>();
        ArrayList<EmailText> testData = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < emails.size(); i++) {
            if (rand.nextInt(emails.size() - i) <= testAmount) {
                testData.add(emails.get(i).getText());
            } else {
                trainData.add(emails.get(i));
            }
        }
        Email.writeListToFile(trainData, train);
        EmailText.writeListToFile(testData, test);
    }

    public static void makePredictions(File test, Predictor categories) throws IOException {
        ArrayList<EmailText> testData = EmailText.readListFromFile(test);
        ArrayList<Email> predictions = new ArrayList<>();
        for (EmailText text : testData) {
            
        }
    }

    public static void main(String[] args) throws IOException {
        splitTrainTestData(new File("spam_or_not_spam.csv"), new File("train.csv"), new File("test.csv"));

        List<Email> emails = Email.readListFromFile(new File("spam_or_not_spam.csv"));
        PredictorBuilder builder = new PredictorBuilder();
        builder.addEmails(emails);
        Predictor predictor = builder.build();

        predictor.getSpamCategory().writeToFile(new File("cat-spam.csv"));
        predictor.getHamCategory().writeToFile(new File("cat-ham.csv"));
        predictor.getAllCategory().writeToFile(new File("cat-all.csv"));

        EmailText testEmail = new EmailText("free click hyperlink now winner sir madam best bank investment remove special service");
        double spamScore = predictor.getSpamScore(testEmail);
        double hamScore = predictor.getHamScore(testEmail);
        System.out.println("spam score: " + spamScore);
        System.out.println("ham score: " + hamScore);
    }
}
