import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseChatAnalyse {

    static AtomicInteger totalRows = new AtomicInteger();
    static AtomicInteger totalFilesProcessed = new AtomicInteger();

    private static void threadCountRows(File filepath) {

        int total = 0;
        try (FileInputStream fis = new FileInputStream(filepath);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while((line = br.readLine()) != null) {
                total++;
                System.out.println(total);
            }
            totalRows.getAndAdd(total);
            fis.close();
            isr.close();
            br.close();
            System.out.println("Finished another process!");
        }
        catch (IOException e) {

        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        String path = "E:\\TwitchDatabaseTables\\2024_06_09\\chatlogs1\\";
        File dir = new File(path);
        File [] directoryListing = dir.listFiles();


        Thread t1 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i != 0 && i %4 == 0) {
                    int total = 0;
                    try (FileInputStream fis = new FileInputStream(directoryListing[i]);
                         InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                         BufferedReader br = new BufferedReader(isr)) {

                        String line;
                        while((line = br.readLine()) != null) {
                            total++;
                        }
                        totalRows.getAndAdd(total);
                        fis.close();
                        isr.close();
                        br.close();
                        System.out.println("Finished another process!");
                    }
                    catch (IOException e) {

                    }
                    totalFilesProcessed.getAndIncrement();
                }
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i %4 == 1) {
                    int total = 0;
                    try (FileInputStream fis = new FileInputStream(directoryListing[i]);
                         InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                         BufferedReader br = new BufferedReader(isr)) {

                        String line;
                        while((line = br.readLine()) != null) {
                            total++;
                        }
                        totalRows.getAndAdd(total);
                        fis.close();
                        isr.close();
                        br.close();
                        System.out.println("Finished another process!");
                    }
                    catch (IOException e) {

                    }
                    totalFilesProcessed.getAndIncrement();
                }
            }
        });
        Thread t3 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i %4 == 2) {
                    int total = 0;
                    try (FileInputStream fis = new FileInputStream(directoryListing[i]);
                         InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                         BufferedReader br = new BufferedReader(isr)) {

                        String line;
                        while((line = br.readLine()) != null) {
                            total++;
                        }
                        totalRows.getAndAdd(total);
                        fis.close();
                        isr.close();
                        br.close();
                        System.out.println("Finished another process!");
                    }
                    catch (IOException e) {

                    }
                    totalFilesProcessed.getAndIncrement();
                }
            }
        });
        Thread t4 = new Thread(() -> {
            for (int i = 0; i< directoryListing.length; i++) {
                if(i %4 == 3) {
                    int total = 0;
                    try (FileInputStream fis = new FileInputStream(directoryListing[i]);
                         InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                         BufferedReader br = new BufferedReader(isr)) {

                        String line;
                        while((line = br.readLine()) != null) {
                            total++;
                        }
                        totalRows.getAndAdd(total);
                        fis.close();
                        isr.close();
                        br.close();
                        System.out.println("Finished another process!");
                    }
                    catch (IOException e) {

                    }
                    totalFilesProcessed.getAndIncrement();
                }
            }
        });

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


