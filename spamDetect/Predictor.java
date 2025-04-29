package spamDetect;

public class Predictor {
    // how high the spam score must be compared to the ham score before an email will be considered spam
    public final double SPAM_THRESHOLD = 0;

    private EmailCategory spamCategory;
    private EmailCategory hamCategory;
    private EmailCategory allCategory;

    public Predictor(EmailCategory spam, EmailCategory ham, EmailCategory all) {
        spamCategory = spam;
        hamCategory = ham;
        allCategory = all;
    }

    public EmailCategory getSpamCategory() {
        return spamCategory;
    }

    public EmailCategory getHamCategory() {
        return hamCategory;
    }

    public EmailCategory getAllCategory() {
        return allCategory;
    }

    public double getSpamScore(EmailText email) {
        return spamCategory.scoreEmail(email) - allCategory.scoreEmail(email);
    }

    public double getHamScore(EmailText email) {
        return hamCategory.scoreEmail(email) - allCategory.scoreEmail(email);
    }

    public boolean predictIsSpam(EmailText email) {
        return (spamCategory.scoreEmail(email) - hamCategory.scoreEmail(email)) > SPAM_THRESHOLD;
    }
}
