package spamDetect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EmailText {
    private String text;
    // classification only cares about which words are present
    // not how many times they appear or in what order
    private final HashSet<String> words;

    public EmailText(String text) {
        this.text = text;
        words = new HashSet<>();
        for (String word : text.split("\s+")) {
            word = sanitizeWord(word);
            if (word.isEmpty()) {
                continue;
            }
            words.add(word);
        }
    }

    public static String sanitizeWord(String word) {
        String res = "";
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (Character.isAlphabetic(c)) {
                res += Character.toLowerCase(c);
            }
        }
        return res;
    }

    public String getText() {
        return text;
    }

    public Set<String> getWords() {
        return words;
    }

    public boolean hasWord(String word) {
        return words.contains(word);
    }

    public static ArrayList<EmailText> readListFromFile(File file) throws IOException {
        ArrayList<EmailText> emails = new ArrayList<>();
        try (FileReader fileReader = new FileReader(file);
                BufferedReader reader = new BufferedReader(fileReader)) {
            // skip header
            reader.readLine();

            reader.lines().forEach(line -> {
                emails.add(new EmailText(line));
            });
        }
        return emails;
    }

    public static void writeListToFile(ArrayList<EmailText> emails, File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("email\n");
            for (EmailText email : emails) {
                writer.write(email.getText() + "\n");
            }
        }
    }
}
