package WorkerTest;

import java.io.File;

public class WorkerMain {

    static int totalRows = 0;

    public static void main(String[] args) throws InterruptedException {

        String path = "D:\\TwitchDatabaseTables\\2024_06_09\\chatlogs1\\";
        File dir = new File(path);
        File[] directoryListing = dir.listFiles();


        Thread t1 = new Thread(new Worker1(directoryListing));
        Thread t2 = new Thread(new Worker2(directoryListing));
        Thread t3 = new Thread(new Worker3(directoryListing));
        Thread t4 = new Thread(new Worker4(directoryListing));


        t1.start();
        t2.start();
        t3.start();
        t4.start();

        while (true) {
            Thread.sleep(5000);
            System.out.println(totalRows);
        }
    }
}
