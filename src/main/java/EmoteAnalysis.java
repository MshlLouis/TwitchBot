import java.io.*;
import java.sql.*;
import java.util.*;

public class EmoteAnalysis {

    static HashMap<String,Integer> emotes = new HashMap<>();
    static String channelname = "papaplatte";
    static String emotePath = "C:\\Users\\louis\\Downloads\\TwitchEmoteStats\\"+channelname+"\\"+channelname+"Emotes.txt";
    static String emoteUniquePath = "C:\\Users\\louis\\Downloads\\TwitchEmoteStats\\"+channelname+"\\"+channelname+"EmotesUnique.txt";
    static String databasePath = "E:\\TwitchLogsSSMS\\TwitchBot Chat Database Backups\\Twitch_Chatlogs_24_10_2023.csv";

    public void readAndPrintEmoteFile() throws IOException {
        String line;
        HashSet<String> set = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(emotePath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(emoteUniquePath));

        while ((line = br.readLine()) != null) {
            set.add(line);
        }
        for (String s : set) {
            bw.write(s +"\n");
        }
        bw.close();
    }

    public void readUniqueEmoteFile() throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new FileReader(emoteUniquePath));

        while ((line = br.readLine()) != null) {
            emotes.put(line,0);
        }
    }

    public void emoteCounter(String channelname, int mode) throws IOException {
        String line;
        int a = 0;
        long counterBatches = 0;
        long counter = 0;
        BufferedReader br = new BufferedReader(new FileReader(databasePath));

        while ((line = br.readLine()) != null) {
            a++;
            if(a % 1_350_000 == 0) {
                System.out.println(a);
            }
            String [] split = line.split(",");
            if(split[2].equals(channelname)) {

                split = split[4].split(" ");

                counterBatches += split.length;
                counter++;

                if(mode == 0) {
                    HashSet<String> set = new HashSet<>(Arrays.asList(split));
                    for (String s : set) {
                        if(emotes.containsKey(s)) {
                            emotes.replace(s, emotes.get(s)+1);
                        }
                    }
                }
                else if(mode == 1) {
                    for (String s : split) {
                        if(emotes.containsKey(s)) {
                            emotes.replace(s, emotes.get(s)+1);
                        }
                    }
                }
            }
        }
        System.out.println("Batches: " +counterBatches +"\nMessages: " +counter +"\n--> " +(counterBatches/(double)counter));
        br.close();
    }

    private void printSortedEmotes() {
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        ArrayList<Integer> list = new ArrayList<>();
        ArrayList<String> list2 = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : emotes.entrySet()) {
            list.add(entry.getValue());
        }
        Collections.sort(list, Comparator.reverseOrder());
        for (Integer integer : list) {
            for (Map.Entry<String, Integer> entry : emotes.entrySet()) {
                if (entry.getValue().equals(integer)) {
                    sortedMap.put(entry.getKey(), integer);
                }
            }
        }
        sortedMap.forEach((key, value) -> list2.add(key +" " +value));

        for (int i = 0; i<list2.size(); i++) {
            System.out.println((i+1) +". " +list2.get(i));
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {

        EmoteAnalysis object = new EmoteAnalysis();

        object.readAndPrintEmoteFile();
        object.readUniqueEmoteFile();
        object.emoteCounter(channelname, 0); //Mode 0 or 1, 0 for Emote Message Count, 1 for Total Emote Count
        object.printSortedEmotes();
    }
}