
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import spamDetect.Email;
import spamDetect.EmailCategoryBuilder;
import spamDetect.EmailText;


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

    public static void main(String[] args) throws IOException {
        splitTrainTestData(new File("spam_or_not_spam.csv"), new File("train.csv"), new File("test.csv"));

        List<Email> emails = Email.readListFromFile(new File("spam_or_not_spam.csv"));
        EmailCategoryBuilder builder = new EmailCategoryBuilder();
        builder.addEmails(emails);
        EmailCategoryBuilder.Result categories = builder.build();

        categories.spam.writeToFile(new File("cat-spam.csv"));
        categories.ham.writeToFile(new File("cat-ham.csv"));
        categories.all.writeToFile(new File("cat-all.csv"));

        EmailText testEmail = new EmailText("free click hyperlink now winner sir madam best bank investment remove special service");
        double spamScore = categories.spam.scoreEmail(testEmail);
        double hamScore = categories.ham.scoreEmail(testEmail);
        double allScore = categories.all.scoreEmail(testEmail);
        System.out.println(spamScore + " " + hamScore + " " + allScore);
        System.out.println("spam score: " + (spamScore - allScore));
        System.out.println("ham score: " + (hamScore - allScore));
    }
}
