package WorkerTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class LineCounter {

    // Callable task to count lines in a single file
    static class FileLineCounter implements Callable<Integer> {
        private final File file;

        public FileLineCounter(File file) {
            this.file = file;
        }

        @Override
        public Integer call() throws Exception {
            return countLines(file);
        }

        private int countLines(File file) throws IOException {
            int lines = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while (reader.readLine() != null) {
                    lines++;
                }
            }
            return lines;
        }
    }

    public static void main(String[] args) {

        String path = "D:\\TwitchDatabaseTables\\2024_06_09\\chatlogs1\\";
        File dir = new File(path);
        File [] directoryListing = dir.listFiles();

        List<File> files = Arrays.asList(directoryListing);

        // Create a thread pool with a fixed number of threads
        ExecutorService executor = Executors.newFixedThreadPool(files.size());

        // Submit tasks to the executor
        List<Future<Integer>> futures = null;
        try {
            futures = executor.invokeAll(files.stream().map(FileLineCounter::new).collect(Collectors.toList()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Shutdown the executor
        executor.shutdown();

        // Sum up the total number of lines
        int totalLines = 0;
        for (Future<Integer> future : futures) {
            try {
                totalLines += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Total number of lines: " + totalLines);
    }
}

