package spamDetect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EmailCategory {
    private final Map<String, Double> wordProbs;

    public EmailCategory(Map<String, Double> wordProbs) {
        this.wordProbs = wordProbs;
    }

    public static EmailCategory readFromFile(File file) throws IOException {
        Map<String, Double> wordProbs = new HashMap<>();
        
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader)) {
            reader.lines().forEach(line -> {
                String[] parts = line.split(",");
                String word = parts[0];
                double prob = Double.parseDouble(parts[1]);
                wordProbs.put(word, prob);
            });
        }
        return new EmailCategory(wordProbs);
    }

    public void writeToFile(File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter writer = new BufferedWriter(fileWriter)) {
            for (Map.Entry<String, Double> entry : wordProbs.entrySet()) {
                writer.write(entry.getKey() + ',' + entry.getValue() + '\n');
            }
        }
    }

    /* measure how well an email fits this category
     * can't really measure the distance between 2 emails
     * we need more than that to establish probabilities
     */
    public double scoreEmail(EmailText email) {
        double score = 0;
        for (Map.Entry<String, Double> entry : wordProbs.entrySet()) {
            String word = entry.getKey();
            double prob = entry.getValue();
            if (email.hasWord(word)) {
                score += Math.log(prob);
            } else {
                score += Math.log(1 - prob);
            }
        }
        return score;
    }
}
