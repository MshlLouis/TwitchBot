package DatabaseFileSystem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

public class mainEngine {

    static String mainPath = "D:\\TwitchDatabaseTables\\2024_06_09\\";
    static String databaseRawPath = mainPath +"Raw\\chatlogs1\\";
    static String databaseSortedPath = mainPath +"Sorted\\";
    static String databaseUserMessage = mainPath +"UserMessages\\";
    static String databaseAllHours = mainPath +"Channel Hour Analysis\\all\\";
    static ArrayList<ArrayList<String>> allEntries = new ArrayList<>();
    static ArrayList<String> userMessages = new ArrayList<>();
    static DecimalFormat df = new DecimalFormat("###.###");

    private static void ReadFileToArrayList(String fileName) throws IOException {

        ArrayList<String> tempList;
        try (FileInputStream fis = new FileInputStream(databaseRawPath +fileName);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            double counter = 0;

            while ((line = br.readLine()) != null) {
                if(counter != 0 && counter % 100000 == 0) {
                    System.out.println(counter / 10000000.0 *100);
                }
                String [] split = line.split(",");
                int idIndex = Integer.parseInt(split[0]);

                if(idIndex < 100) {
                    tempList = allEntries.get(0);
                    tempList.add(line);
                    allEntries.set(0,tempList);
                }
                else {
                    int decimals = Integer.parseInt(split[0].substring(0,3));
                    tempList = allEntries.get(decimals);
                    tempList.add(line);
                    allEntries.set(decimals,tempList);
                }
                counter++;
            }
        }
    }

    private static void printFile(int index, ArrayList<String> entries) throws IOException {
        String path = "";

        if(index == 0) {
            path = databaseSortedPath +"1-99.txt";
        }
        else {
            path = databaseSortedPath +index +".txt";
        }
        try (FileOutputStream fos = new FileOutputStream(path,true);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            for (String e : entries) {
                bw.write(e +"\n");
            }
        }
    }

    private static void addLogsToDirectory(String fileName) throws IOException {
        ReadFileToArrayList(fileName);

        for (int i = 0; i<allEntries.size(); i++) {
            ArrayList<String> temp = allEntries.get(i);
            if(!temp.isEmpty()) {
                printFile(i, temp);
            }
        }
    }

    private static void printIDMessagesToFile(String userID, String currUserName, String channelName, List<String> excluded) throws IOException {
        getUserMessages(userID);

        try (FileOutputStream fos = new FileOutputStream(databaseUserMessage +userID +"," +currUserName +".txt");
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            for (String uM : userMessages) {
                String [] split = uM.split(",");

                if (channelName == null) {
                    if(!excluded.contains(split[3])) {
                        bw.write(uM + "\n");
                    }
                } else {
                    if (split[3].equals(channelName)) {
                        bw.write(uM + "\n");
                    }
                }
            }
        }
    }

    private static void getUserMessages(String userID) throws IOException {

        userMessages.clear();
        try (FileInputStream fis = new FileInputStream(databaseSortedPath +userID.substring(0,3) +".txt");
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            String line;

            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");

                if (split[0].equals(userID)) {
                    userMessages.add(line);
                }
            }
        }
    }

    private static void countChannelMessages(String userID) throws IOException {

        getUserMessages(userID);
        HashMap<String, Integer> allCounts = new HashMap<>();

        for (String uM : userMessages) {
            String [] split = uM.split(",");

            try {
                int number = allCounts.get(split[3]);
                allCounts.put(split[3],number+1);
            }
            catch (NullPointerException e) {
                allCounts.put(split[3],1);
            }
        }

        List<Map.Entry<String, Integer> > list = new LinkedList<>(allCounts.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);

        for (Map.Entry<String, Integer> a : list) {
            System.out.println(a);
        }
    }

    private static int [] countMessagesByHour(String userID, String currUserName, String path, boolean print) throws IOException {

        int [] allCounts = new int[24];
        double total = 0;

        try (FileInputStream fis = new FileInputStream(path +userID +"," +currUserName +".txt");
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            String line;

            while ((line = br.readLine()) != null) {
                String timeS = line.split(",")[4].split(" ")[1].substring(0,2);
                int time = Integer.parseInt(timeS.substring(0,2));
                allCounts[time]++;
                total++;
            }
        }
        if(print) {
            printHours(allCounts, total);
        }
        return allCounts;
    }

    private static void createStreamerProfile(String userID, String channelName) throws IOException {
        int [] allCounts = new int [24];
        double total = 0;

        int [] temp = countMessagesByHour(userID, channelName, mainPath,false);

        for(int k = 0; k<24; k++) {
            allCounts[k] += temp[k];
            total += temp[k];
        }
        createProfileFile(channelName, allCounts, total);
    }

    private static void createProfileFile(String channelName, int[] allCounts, double total) throws IOException{

        try (FileOutputStream fos = new FileOutputStream(mainPath +channelName +" profile.txt");
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            for (int i : allCounts) {
                bw.write(df.format(i/total*100) +"\n");
            }
        }
    }

    private static void printHours(int[] allCounts, double total) {

        double [] max = {0,0};
        double [] min = {999999999,0};

        for (int i = 0; i<24; i++) {
            System.out.println(i +":\t" +allCounts[i]);
            if(allCounts[i] > max[0]) {
                max[0] = allCounts[i];
                max[1] = i;
            }
            if(allCounts[i] < min[0]) {
                min[0] = allCounts[i];
                min[1] = i;
            }
        }
        System.out.println("\nMax: " +(int) max[0] +" (" +(int) max[1] +")");
        System.out.println("Min: " +(int) min[0] +" (" +(int) min[1] +")\n");

        for (int i = 0; i<24; i++) {
            System.out.print(i +":\t");
            for(int k = 0; k<(allCounts[i]/max[0]*100); k++) {
                System.out.print("|");
            }
            System.out.print("  " +df.format(allCounts[i]/total*100) +" %");
            System.out.println();
        }
    }

    private static double [] calcPercentages(int [] allCounts, double total) {
        double [] values = new double [24];
        for (int i = 0; i<24; i++) {
            values[i] = allCounts[i]/total*100;
        }
        return values;
    }

    private static int calcTotal (int [] allCounts) {
        int total = 0;
        for (int i : allCounts) {
            total += i;
        }
        return total;
    }

    private static double calcHoursDifferences (String userID, String currUserName, String channelname) throws IOException {

        int [] allHours = countMessagesByHour(userID, currUserName, databaseUserMessage,false);
        double total = calcTotal(allHours);
        double [] percentagesUser = calcPercentages(allHours, total);
        ArrayList <Double> percentagesChannel = new ArrayList<>();
        double totalAvg = 0;

        try (FileInputStream fis = new FileInputStream(databaseAllHours +channelname);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            String line;

            while ((line = br.readLine()) != null) {
                percentagesChannel.add(Double.parseDouble(line));
            }
        }

        for(int i = 0; i<24; i++) {
            totalAvg += Math.abs(percentagesUser[i]-percentagesChannel.get(i));
        }
        return totalAvg/24.0;
    }

    private static void calcAllHourDifferences (String userID, String currUserName) throws IOException {

        HashMap<String, Double> map = new HashMap<>();

        File dir = new File(databaseAllHours);
        File [] directoryListing = dir.listFiles();

        for (File f : directoryListing) {
            double diff = Double.parseDouble(df.format(calcHoursDifferences(userID, currUserName, f.getName())));
            String out = f.getName().substring(0,f.getName().length()-4) +": " +diff +" avg difference!";
            map.put(out, diff);
        }

        List<Map.Entry<String, Double> > list = new LinkedList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        for (Map.Entry<String, Double> s : list) {
            System.out.println(s.getKey());
        }
    }

    private static void countAllChannelMessages () throws IOException {
        HashMap<String,Integer> counter = new HashMap<>();
        File dir = new File(databaseRawPath);
        File [] directoryListing = dir.listFiles();

        for (File f : directoryListing) {
            try (FileInputStream fis = new FileInputStream(f);
                 InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {

                String line;

                while ((line = br.readLine()) != null) {
                    String [] split = line.split(",");
                    String channelname = split[3];
                    try {
                        int num = counter.get(channelname);
                        counter.put(channelname, num+1);
                    }
                    catch (NullPointerException e) {
                        counter.put(channelname, 1);
                    }
                }
            }
            System.out.println("Finished scanning " +f.getName());
        }

        List<Map.Entry<String, Integer> > list = new LinkedList<>(counter.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);

        for (Map.Entry<String, Integer> l : list) {
            System.out.println(l.getKey() +" " +l.getValue());
        }
    }

    public static void main(String[] args) throws IOException {

        String [] items = {};
        String userID = "";
        String userName = "";
        List<String> excluded = new LinkedList<>(Arrays.asList(items));

        for(int i = 0; i<1000; i++) {
            allEntries.add(i,new ArrayList<>());
        }

//        addLogsToDirectory("chatlogs1_37().csv");
//        createStreamerProfile("","");
//        countAllChannelMessages();
//
//
//        printIDMessagesToFile(userID, userName,null, excluded);
//        countChannelMessages(userID);
//        countMessagesByHour(userID, userName, databaseUserMessage,true);
//        calcAllHourDifferences(userID, userName);
    }
}