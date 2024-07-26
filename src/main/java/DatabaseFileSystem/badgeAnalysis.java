package DatabaseFileSystem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class badgeAnalysis {

    static String mainPath = "D:\\TwitchDatabaseTables\\2024_06_09\\";

    private static HashSet<String> saveAllBadges(String fileName) throws IOException {
        HashSet<String> badges = new HashSet<>();

        try (FileInputStream fis = new FileInputStream(mainPath +fileName);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\"","").replace("{","").replace("}","");
                String [] split = line.split(",");

                for (String s : split) {
                    String [] split2 = s.split("=");
                    badges.add(split2[0].trim());
                }
            }
        }
        return badges;
    }

    private static void printBadges(HashSet<String> badges) {
        List<String> list = new ArrayList<>(badges);
        Collections.sort(list);

        for (int i = 1; i<list.size(); i++) {
            System.out.println(i +" " +list.get(i));
        }
    }

    public static void main(String[] args) throws IOException {

        printBadges(saveAllBadges("allBadges.csv"));

    }
}
