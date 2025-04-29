package spamDetect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class WordFreqCounter {
    private HashMap<String, Integer> counts;
    private int totalCount;

    public WordFreqCounter() {
        counts = new HashMap<>();
        totalCount = 0;
    }

    private void add(String word) {
        counts.compute(word, (w, n) -> (n == null) ? 1 : n + 1);
    }

    public void add(Set<String> words) {
        for (String word : words) {
            add(word);
        }
        totalCount += 1;
    }

    public int getCount(String word) {
        return counts.getOrDefault(word, 0);
    }

    public int getTotalCount() {
        return totalCount;
    }
}

public class PredictorBuilder {
    // word must appear this many times to be used as a feature
    private final int MIN_COUNT = 30;
    // word must appear at different rates in spam vs ham to be used as a feature
    private final double MIN_PROB_DIFFERENCE = 0.5;

    private WordFreqCounter spamCounts, hamCounts;
    private HashSet<String> words;

    public class Result {
        public EmailCategory spam;
        public EmailCategory ham;
        public EmailCategory all;
    }

    public PredictorBuilder() {
        spamCounts = new WordFreqCounter();
        hamCounts = new WordFreqCounter();
        words = new HashSet<>();
    }

    public void addEmail(Email email) {
        Set<String> emailWords = email.getText().getWords();
        words.addAll(emailWords);
        if (email.isSpam()) {
            spamCounts.add(emailWords);
        } else {
            hamCounts.add(emailWords);
        }
    }

    public void addEmails(Iterable<Email> emails) {
        for (Email email : emails) {
            addEmail(email);
        }
    }

    private double probDifference(double a, double b) {
        return Math.abs(Math.log(a) - Math.log(b));
    }

    private boolean shouldWordBeFeature(String word) {
        int spamCount = spamCounts.getCount(word);
        int hamCount = hamCounts.getCount(word);
        if (word.equals("enlarge")) {
            System.out.println(spamCount + " " + hamCount);
        }
        if (spamCount + hamCount < MIN_COUNT) {
            return false;
        }

        double spamFreq = (spamCount + 1.0) / (spamCounts.getTotalCount() + 1);
        double hamFreq = (hamCount + 1.0) / (hamCounts.getTotalCount() + 1);
        if (word.equals("enlarge")) {
            System.out.println(spamFreq + " " + hamFreq);
        }
        if (probDifference(spamFreq, hamFreq) < MIN_PROB_DIFFERENCE && probDifference(1 - spamFreq, 1 - hamFreq) < MIN_PROB_DIFFERENCE) {
            return false;
        }

        return true;
    }

    private HashSet<String> getFeatureWords() {
        HashSet<String> features = new HashSet<>(words);
        features.removeIf(w -> !shouldWordBeFeature(w));
        return features;
    }

    public Predictor build() {
        HashMap<String, Double> spamProbs = new HashMap<>();
        HashMap<String, Double> hamProbs = new HashMap<>();
        HashMap<String, Double> allProbs = new HashMap<>();

        int spamTotal = spamCounts.getTotalCount();
        int hamTotal = hamCounts.getTotalCount();
        for (String word : getFeatureWords()) {
            int spamCount = spamCounts.getCount(word);
            int hamCount = hamCounts.getCount(word);
            // add 1 so we don't divide-by-zero on rare words
            spamProbs.put(word, (spamCount + 1.0) / (spamTotal + 2));
            hamProbs.put(word, (hamCount + 1.0) / (hamTotal + 2));
            allProbs.put(word, (spamCount + hamCount + 2.0) / (spamTotal + hamTotal + 4));
        }
        
        EmailCategory spam = new EmailCategory(spamProbs);
        EmailCategory ham = new EmailCategory(hamProbs);
        EmailCategory all = new EmailCategory(allProbs);
        return new Predictor(spam, ham, all);
    }
}
