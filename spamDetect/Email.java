package spamDetect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Email {
    private EmailText text;
    private boolean isSpam;

    public Email(EmailText text, boolean isSpam) {
        this.text = text;
        this.isSpam = isSpam;
    }

    public EmailText getText() {
        return text;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public static ArrayList<Email> readListFromFile(File file) throws IOException {
        ArrayList<Email> emails = new ArrayList<>();
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader)) {
            // skip header
            reader.readLine();

            reader.lines().forEach(line -> {

                String[] parts = line.split(",");
                EmailText text = new EmailText(parts[0]);
                boolean isSpam = parts[1].equals("1");
                emails.add(new Email(text, isSpam));
            });
        }
        return emails;
    }

    public static void writeListToFile(ArrayList<Email> emails, File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("email,label\n");
            for (Email email : emails) {
                writer.write(email.getText().getText() + "," + (email.isSpam() ? "1" : "0") + "\n");
            }
        }
    }
}
