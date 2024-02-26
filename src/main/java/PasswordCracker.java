import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class PasswordCracker {
    final static String validChars = "abcdefghijklmnopqrstuvwxyz0123456789";

    public static void main(String[] args) {

        //Example : 7dc5306fa703d673a980049fe9c0427f ----------------------> Output : 10bc

        Scanner scanner = new Scanner(System.in);
        System.out.println(" --------------------- Menu --------------------- ");
        System.out.println("1) Crack with brute force");
        System.out.println("2) Crack with word list");
        System.out.println("3) Generate rainbow table ");
        System.out.println("4) Crack with rainbow table lookup ");
        System.out.println("5) Quit");

        String outputFilePath = "rainbow_table.txt";
        boolean startLoop = true;
        do{
             int option = scanner.nextInt();
            switch (option) {
                case 1 -> {
                    System.out.print("Enter password length hash: ");
                    int passwordLength = Integer.parseInt(scanner.next());
                    System.out.print("Enter target hash: ");
                    String targetHash = scanner.next();
                    bruteForceAttack(targetHash, passwordLength);
                }
                case 2 -> {
                    System.out.print("Enter path to word list file: ");
                    String wordListPath = scanner.next();
                    System.out.print("Enter target hash: ");
                    String targetHash = scanner.next();
                    wordListAttack(wordListPath, targetHash);
                }
                case 3 -> {
                    System.out.print("Enter password length hash: ");
                    int passwordLength = Integer.parseInt(scanner.next());
                    generateRainbowTable(outputFilePath, passwordLength);
                }
                case 4 -> {
                    System.out.print("Enter target hash: ");
                    String targetHash = scanner.next();
                    rainbowTableLookup(outputFilePath, targetHash);
                }
                case 5 ->{
                    System.out.println("Quit the menu");
                    startLoop = false;
                }
                default -> System.out.println("Invalid option.");
            }
            System.out.println("--------------------- Try another time or quit --------------------- ");
        } while (startLoop);
        scanner.close();
    }

    public static void bruteForceAttack(String targetHash, int passwordLength) {
        for (String password : generatePasswords(passwordLength)) {
            String hashedPassword = md5(password);
            if (hashedPassword.equals(targetHash)) {
                System.out.println("Password found: " + password);
                return;
            }
        }
        System.out.println("Password not found by brute force.");
    }


    public static void wordListAttack(String wordListPath, String targetHash) {
        try {
            File wordListFile = new File(wordListPath);
            Scanner wordScanner = new Scanner(wordListFile);
            MessageDigest md = MessageDigest.getInstance("MD5");

            while (wordScanner.hasNextLine()) {
                String word = wordScanner.nextLine();
                md.update(word.getBytes());
                byte[] hashedBytes = md.digest();
                String hashedWord = bytesToHex(hashedBytes);

                if (hashedWord.equals(targetHash)) {
                    System.out.println("Password found: " + word);
                    wordScanner.close();
                    return;
                }
            }

            wordScanner.close();
            System.out.println("Password not found in word list.");
        } catch (FileNotFoundException e) {
            System.out.println("Word list file not found.");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("MD5 algorithm not supported.");
        }
    }


    public static void generateRainbowTable(String outputFilePath,int passwordLength) {

        if (outputFilePath == null || outputFilePath.isEmpty()) {
            System.out.println("Invalid output file path.");
            return;
        }
        try {
            PrintWriter writer = new PrintWriter(outputFilePath);
            MessageDigest md = MessageDigest.getInstance("MD5");

            for (String password : generatePasswords(passwordLength)) {
                md.update(password.getBytes());
                byte[] hashedBytes = md.digest();
                String hashedPassword = bytesToHex(hashedBytes);
                writer.println(hashedPassword + ":" + password);
            }

            writer.close();
            System.out.println("Rainbow table successfully generated and saved to " + outputFilePath);
        } catch (FileNotFoundException | NoSuchAlgorithmException e) {
            System.out.println("Error generating rainbow table: " + e.getMessage());
        }
    }

    private static Iterable<String> generatePasswords(int length) {
        List<String> passwords = new ArrayList<>();
        generatePasswordsHelper(length, validChars, "", passwords);
        return passwords;
    }

    private static void generatePasswordsHelper(int length, String validChars, String prefix, List<String> passwords) {
        if (length == 0) {
            passwords.add(prefix);
            return;
        }
        for (int i = 0; i < validChars.length(); i++) {
            String newPrefix = prefix + validChars.charAt(i);
            generatePasswordsHelper(length - 1, validChars, newPrefix, passwords);
        }
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashText = new StringBuilder(no.toString(16));
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }
            return hashText.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void rainbowTableLookup(String rainbowTablePath, String targetHash) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(rainbowTablePath));
            Map<String, String> rainbowTable = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                rainbowTable.put(parts[0], parts[1]);
            }
            reader.close();

            if (rainbowTable.containsKey(targetHash)) {
                System.out.println("Password found: " + rainbowTable.get(targetHash));
            } else {
                System.out.println("Password not found in rainbow table.");
            }
        } catch (IOException e) {
            System.out.println("Error reading rainbow table file.");
        }
    }
}