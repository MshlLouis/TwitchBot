package DatabaseFileSystem;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.UserList;

import java.io.*;
import java.util.*;

import static java.util.Collections.reverseOrder;

import java.util.concurrent.atomic.AtomicInteger;

public class analyzeChatData {

    static AtomicInteger totalFilesProcessed = new AtomicInteger();
    static AtomicInteger totalLines = new AtomicInteger();

    public static boolean checkPalindrome(char [] charArray) {
        for(int k = 0; k<charArray.length/2; k++) {
            if(charArray[k] != charArray[charArray.length-1-k]) {
                return false;
            }
        }
        return true;
    }

    public String getDisplayName(TwitchClient twitchClient, String s) {
        UserList user = twitchClient.getHelix().getUsers(null, Collections.singletonList(s), null).execute();
        return user.getUsers().get(0).getDisplayName();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        String path = "E:\\TwitchDatabaseTables\\2024_06_09\\chatlogs1\\";
        File dir = new File(path);
        File [] directoryListing = dir.listFiles();
        String [] names = new String[Objects.requireNonNull(directoryListing).length];
        int max = 0;
        Map<Integer, Integer> allCounts = new HashMap<>();
        analyzeChatData object = new analyzeChatData();

/*
        String temp = "";
            for (int i = 0; i< directoryListing.length; i++) {
          //  names[i] = directoryListing[1].getName().substring(0,directoryListing[1].getName().length()-4);
            temp = directoryListing[i].getName().substring(0,directoryListing[i].getName().length()-4);
            char [] charArray = temp.toCharArray();

            if(checkPalindrome(charArray)) {
                System.out.println(temp);
            }
        }

        System.out.println(object.getDisplayName(mainBot.mainFile.twitchClient, "91999919"));
        System.out.println(object.getDisplayName(mainBot.mainFile.twitchClient, "60011006"));
*/



        Thread t1 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i != 0 && i %8 == 0) {
                    doOperation(directoryListing, allCounts, i);
                }
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i %8 == 1) {
                    doOperation(directoryListing, allCounts, i);
                }
            }
        });
        Thread t3 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i %8 == 2) {
                    doOperation(directoryListing, allCounts, i);
                }
            }
        });
        Thread t4 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i %8 == 3) {
                    doOperation(directoryListing, allCounts, i);
                }
            }
        });
        Thread t5 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i %8 == 4) {
                    doOperation(directoryListing, allCounts, i);
                }
            }
        });
        Thread t6 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i %8 == 5) {
                    doOperation(directoryListing, allCounts, i);
                }
            }
        });
        Thread t7 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i %8 == 6) {
                    doOperation(directoryListing, allCounts, i);
                }
            }
        });
        Thread t8 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i %8 == 7) {
                    doOperation(directoryListing, allCounts, i);
                }
            }
        });

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();

        while (totalFilesProcessed.get() != directoryListing.length-1) {
            Thread.sleep(30000);
            System.out.println(totalFilesProcessed +" " + directoryListing.length);
        }

        try {
            allCounts.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(System.out::println);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        System.out.println("\nTotal Lines: " +totalLines);
    }

    private static void doOperation(File[] directoryListing, Map<Integer, Integer> allCounts, int i) {
        int counter = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(String.valueOf(directoryListing[i])));
            String line;

            while ((line = br.readLine()) != null) {
                counter++;
                totalLines.getAndIncrement();
            }
            if (i % 500 == 0) {
                System.out.println(i);
            }
            br.close();

            if(allCounts.containsKey(counter)) {
                allCounts.put(counter, allCounts.get(counter)+1);
            }
            else {
                allCounts.put(counter, 1);
            }
            totalFilesProcessed.getAndIncrement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}