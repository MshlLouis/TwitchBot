package WorkerTest;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Worker1 implements Runnable
{
    private final File [] directoryListing;

    public Worker1(File [] directoryListing) {
        this.directoryListing = directoryListing;
    }

    static int count = 0;

    public void run()
    {
        for(int i = 0; i<directoryListing.length; i++) {
            if(i %4 == 0) {
                try (FileInputStream fis = new FileInputStream(directoryListing[i]);
                     InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                     BufferedReader br = new BufferedReader(isr)) {

                    String line;
                    while((line = br.readLine()) != null) {
                        count++;
                    }
                    fis.close();
                    isr.close();
                    br.close();
                    WorkerMain.totalRows += count;
                    count = 0;
                }
                catch (IOException e) {

                }
            }
        }
    }
}