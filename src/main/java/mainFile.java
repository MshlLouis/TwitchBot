import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.chat.events.channel.UserBanEvent;
import com.github.twitch4j.chat.events.channel.UserTimeoutEvent;
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.UserList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class mainFile {

    static String accessToken;
    static OAuth2Credential credential;
    static TwitchClient twitchClient;
    static String databaseURL;
    static String databaseUsername;
    static String databasePassword;
    static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    static DateTimeFormatter timeFormatDate = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    static AtomicLong totalMessageCount = new AtomicLong();
    static boolean locked10SecondAverage = false;
    static int additionalHours = 1;
    static char prefix = '!';
    static boolean tracking = false;
    static String trackedUser = "";
    static boolean started = false;
    static long startTimestamp;
    static int averageMessageLimit = 75;
    static HashSet<String> allActiveUsers = new HashSet<>();
    static Connection c = null;
    static Statement stmt = null;
    static int currentTableInt = 1;
    static String currentTableName = "chatlogs";
    static HashMap<String,String[]> lastMessages = new HashMap<>();
    static int lastMessagesTimer = 1800;
    static boolean printConcurrentModException = false;
    static HashMap<String,String> joinedChannelNamesAndIDs = new HashMap<>();
    static ArrayList<String> joinedChannels = new ArrayList<>();
    static String [] blockedUsersForSubs = {"43325871","237719657","1564983"};
    static boolean containsBlockedUser = false;

    public void setCredentials() throws IOException {
        ArrayList<String> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("credentials.txt"));
        String line;

        while ((line = br.readLine()) != null) {
            list.add(line);
        }

        if(list.size() != 6) {
            System.out.println("6 Credentials required, please check credentials.txt for inputs. Exiting program!");
            System.exit(1);
        }

        accessToken = list.get(0);
        credential = new OAuth2Credential("twitch", accessToken);
        twitchClient = TwitchClientBuilder.builder()
                .withClientId(list.get(1))
                .withClientSecret(list.get(2))
                .withEnableChat(true)
                .withEnableHelix(true)
                .withChatAccount(credential)
                .build();

        databaseURL = list.get(3);
        databaseUsername = list.get(4);
        databasePassword = list.get(5);
    }

    public static String getUserID(TwitchClient twitchClient, String name) {
        UserList user = twitchClient.getHelix().getUsers(accessToken,null, Collections.singletonList(name)).execute();
        String ID;

        try {
           ID = user.getUsers().get(0).getId();
        }
        catch (IndexOutOfBoundsException e) {
            return "###";
        }
        return ID;
    }

    public static String getDisplayName(TwitchClient twitchClient, String s) {
        UserList user = twitchClient.getHelix().getUsers(null, null, Collections.singletonList(s)).execute();
        return user.getUsers().get(0).getDisplayName();
    }

    public static String idToDisplayName(TwitchClient twitchClient, String s) {
        UserList user = twitchClient.getHelix().getUsers(null, Collections.singletonList(s), null).execute();
        return user.getUsers().get(0).getDisplayName();
    }

    public void getTotalMessageCount() {
        System.out.println(totalMessageCount +" total messages!");
    }

    public static void joinChannel(TwitchClient twitchClient, String channelName, mainFile object) throws SQLException {

        char [] charArray = channelName.toCharArray();
        ArrayList<String> allChannels = new ArrayList<>();
        String name = "";

        for(int i = 0; i<charArray.length; i++) {
            if(charArray[i] == ',') {
                allChannels.add(name);
                name = "";
            }
            else if(i == charArray.length-1) {
                name += charArray[i];
                allChannels.add(name);
                name = "";
            }
            else {
                name += charArray[i];
            }
        }

        for (String s : allChannels) {
            if(!object.checkJoinedStatus(s) && !getUserID(twitchClient, s).equals("###")) {

                twitchClient.getChat().joinChannel(s);
                String ID = getUserID(twitchClient, s);
                StreamList resultList = twitchClient.getHelix().getStreams(null, null, null, 1, null, null, Collections.singletonList(ID), null).execute();

                try {
                    System.out.println("Joined " +getDisplayName(twitchClient, s)  +" with " +resultList.getStreams().get(0).getViewerCount() +" Viewers!");
                    joinedChannels.add(s);
                    joinedChannelNamesAndIDs.put(ID,s);
                }
                catch (IndexOutOfBoundsException e) {
                    System.out.println(getDisplayName(twitchClient, s) +" not live, still joined!");
                    joinedChannels.add(s);
                    joinedChannelNamesAndIDs.put(ID,s);
                }
            }
            else if(getUserID(twitchClient, s).equals("###")) {
                System.out.println("Could not get ID for input " +s +"!");
            }
            else {
                System.out.println("Already joined " +getDisplayName(twitchClient, s) +"!");
            }
        }
    }

    public void leaveChannel(TwitchClient twitchClient, String channelName) {
        twitchClient.getChat().leaveChannel(channelName);
        System.out.println("Left channel " +channelName);
    }

    public void printChat(TwitchClient twitchClient) {
        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {

            try {
                String out = "[" +timeFormatDate.format(LocalDateTime.now().plusHours(additionalHours)) +"] " +"[" +event.getChannel().getName() +"] [" +event.getUser().getId() +"] " +event.getUser().getName() + ": " + event.getMessage();

                mySQLFile.insertData(c, event, currentTableName+currentTableInt);
                mySQLFile.insertDataIDs(c, event);
                addHashMapEntry(event.getMessage(), event.getUser().getId());

                totalMessageCount.getAndIncrement();
                if(tracking && event.getUser().getName().equals(trackedUser)) {
                    System.out.println(out);
                }
            }
            catch (SQLException e) {
                if(e.getMessage().contains("full")) {
                    System.out.println(e.getMessage());
                    currentTableInt++;
                    try {
                        mySQLFile.createMainTable(c, stmt, currentTableName+currentTableInt);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });
    }

    public void detectSubsAndCheers(TwitchClient twitchClient) {
        twitchClient.getEventManager().onEvent(IRCMessageEvent.class, event -> {
            if(event.getCommandType().equals("USERNOTICE")) {
                if(!event.getTagValue("msg-id").toString().contains("raid") && !event.getTagValue("msg-id").toString().contains("submysterygift")) {
                    for (String s : blockedUsersForSubs) {
                        if(event.getUserId().equals(s)) {
                            containsBlockedUser = true;
                        }
                    }
                    if(!containsBlockedUser) {
                        try {
                            mySQLFile.insertSubAndCheerData(c,event);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                }
            }
            containsBlockedUser = false;
        });
    }

    public void timeoutCheck(TwitchClient twitchClient) {

        twitchClient.getEventManager().onEvent(UserTimeoutEvent.class, event -> {

            String [] arr = lastMessages.get(event.getUser().getId());
            if(arr == null) {
                try {
                    mySQLFile.insertTimeoutData(c, event, "[#No last message#]");
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            else {
                String lastMsg = arr[1];
                try {
                    mySQLFile.insertTimeoutData(c, event, lastMsg);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    public void banCheck(TwitchClient twitchClient) {

        twitchClient.getEventManager().onEvent(UserBanEvent.class, event -> {

            String [] arr = lastMessages.get(event.getUser().getId());
            String out;
            if(arr == null) {
                try {
                    mySQLFile.insertBanData(c, event, "[#No last message#]");
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            else {
                String lastMsg = arr[1];
                try {
                    mySQLFile.insertBanData(c, event, lastMsg);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    public void addHashMapEntry(String lastMessage, String userID) {

        String [] arr = new String[2];
        arr[0] = System.currentTimeMillis()+"";
        arr[1] = lastMessage;
        lastMessages.put(userID,arr);

    }

    public static void deleteHashMapEntries(long currentMillis) {
        try {
            lastMessages.entrySet().removeIf(e -> currentMillis - Long.parseLong(e.getValue()[0]) > lastMessagesTimer*1000L);
        }
        catch (ConcurrentModificationException e) {
            if(printConcurrentModException) {
                System.out.println("Error: Couldn't remove entry from HashMap \"lastMessages\"!");
            }
        }
    }

    public void setLastMessageTimer(int input) {
        lastMessagesTimer = input;
    }

    public static void threadTimeoutMapControl() {
        Thread t7 = new Thread(new Runnable(){
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(lastMessagesTimer*1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    deleteHashMapEntries(System.currentTimeMillis());
                    //     System.out.println(lastMessages.size());
                }
            }
        });
        t7.start();
    }

    public static void threadCheckChannelIDs() {
        Thread t8 = new Thread(new Runnable(){
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(600000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread \"threadCheckChannelIDs\" was interrupted!");
                    }
                    checkChannelnameIDsMethod();
                }
            }
        });
        t8.start();
    }

    public static void checkChannelnameIDsMethod() {

        for(Map.Entry<String,String> m : joinedChannelNamesAndIDs.entrySet()) {
            String ID = getUserID(twitchClient, m.getValue());

            if(ID.equals("###")) {
                joinedChannels.remove(m.getValue());
                String newName = idToDisplayName(twitchClient,m.getKey()).toLowerCase(Locale.ROOT);
                System.out.println("Detected change in username for user: " +m.getValue()
                        +"\nChanged name to " +newName);
                joinedChannelNamesAndIDs.put(m.getKey(),newName);
                joinedChannels.add(newName);
                twitchClient.getChat().joinChannel(newName);

                try {
                    StreamList resultList = twitchClient.getHelix().getStreams(null, null, null, 1, null, null, Collections.singletonList(ID), null).execute();
                    System.out.println("Joined " +getDisplayName(twitchClient, newName)  +" with " +resultList.getStreams().get(0).getViewerCount() +" Viewers!");
                }
                catch (IndexOutOfBoundsException e) {
                    System.out.println(newName +" not live, still joined!");
                }
            }
        }
    }

    public int getHashSetSize() {
        return allActiveUsers.size();
    }

    public void getAllActiveUsers() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(String.valueOf("allActiveUsers.txt")));
        String line;

        while ((line = br.readLine()) != null) {
            allActiveUsers.add(line);
        }
    }

    public boolean checkJoinedStatus(String channelName) {
        for (String s : joinedChannels) {
            if(s.equals(channelName)) {
                return true;
            }
        }
        return false;
    }

    public void get10SecondAverage() {

        Thread t5 = new Thread(new Runnable(){
            @Override
            public void run() {
                int out = totalMessageCount.intValue();
                locked10SecondAverage = true;

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("[" +timeFormat.format(LocalTime.now().plusHours(additionalHours)) +"] " +(totalMessageCount.intValue()-out)/10.0 +"/s");
                locked10SecondAverage = false;
            }
        });
        t5.start();
    }

    public void get10SecondAverageEndless() {

        Thread t4 = new Thread(new Runnable(){
            @Override
            public void run() {
                while(true) {
                    int out = totalMessageCount.intValue();

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    double avg = (totalMessageCount.intValue()-out)/10.0;

                    if(avg > averageMessageLimit) {
                        System.out.println("[" +timeFormat.format(LocalTime.now().plusHours(additionalHours)) +"] " +"Warning: Average msgs/s exceed limit of " +averageMessageLimit +" [" +avg +"]");
                    }
                }
            }
        });
        t4.start();
    }

    public void setPrefix(char pre) {
        prefix = pre;
    }

    public void printTotalJoinedViewerCount(TwitchClient twitchClient) {

        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                int total = 0;

                for (String s : joinedChannels) {
                    try {
                        String ID = getUserID(twitchClient, s);
                        StreamList resultList = twitchClient.getHelix().getStreams(null, null, null, 1, null, null, Collections.singletonList(ID), null).execute();
                        total += resultList.getStreams().get(0).getViewerCount();
                        System.out.println(getDisplayName(twitchClient, s) + ": " + resultList.getStreams().get(0).getViewerCount());
                    } catch (IndexOutOfBoundsException ignored) {

                    }
                }

                System.out.println(total + " total joined viewers!");
            }
        });
        t3.start();
    }

    public void searchAndSaveUserMessages(TwitchClient twitchClient, String [] data) {

        Thread t2 = new Thread(() -> {

            String username = data[1];

            if(allActiveUsers.contains(getUserID(twitchClient, username))) {
                String path = "userLogs/" +getUserID(twitchClient, username) +".txt";

                BufferedWriter bw;
                int counter = 0;

                try {
                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("searchedUsers/" +username +" in " +data[2] +".txt"), StandardCharsets.UTF_8));

                    BufferedReader br = new BufferedReader(new FileReader(String.valueOf(path)));
                    String line;

                    while ((line = br.readLine()) != null) {
                        if(line.contains("[" +data[2] +"]")) {
                            bw.write(line +"\n");
                            counter++;
                        }
                    }
                    bw.close();
                    System.out.println("Successfully created file with " +counter +" entries!");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t2.start();
    }

    public void searchAllAndSaveUserMessages(TwitchClient twitchClient, String [] data) {

        Thread t6 = new Thread(() -> {
            String username = data[1];
            String path = "userLogs/" +getUserID(twitchClient, username)+".txt";
            BufferedWriter bw;
            int counter = 0;

            try {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("searchedUsers/" +username +" in all.txt"), StandardCharsets.UTF_8));

                try {
                    BufferedReader br = new BufferedReader(new FileReader(String.valueOf(path)));
                    String line;

                    while ((line = br.readLine()) != null) {
                        bw.write(line +"\n");
                        counter++;
                    }
                    bw.close();
                    System.out.println("Successfully created file with " +counter +" entries!");
                }
                catch (FileNotFoundException e) {
                    System.out.println("No file found for user " +username +", the bot hasn't caught any of their messages yet!");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t6.start();
    }

    public void getUptime() {

        long current = System.currentTimeMillis()-startTimestamp;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date resultDate = new Date(startTimestamp);
        System.out.println("---------------------\n" +sdf.format(resultDate) +" (+1H)");

        long multiplier = 24*60*60*1000;
        int [] time = new int[4];
        int [] multipliers = {1,24,60,60};
        String [] timeSlots = {"days", "hours", "minutes", "seconds"};

        for(int i = 0; i<time.length; i++) {
            multiplier /= multipliers[i];
            time[i] = (int) (current/multiplier);

            if((int) (current/multiplier) > 0) {
                current -= (int) (current/multiplier)*multiplier;
            }
        }
        for (int i = 0; i<time.length; i++) {
            System.out.println(time[i] +" " +timeSlots[i]);
        }
        System.out.println("---------------------");
    }

    public void getCommandInfo(String commandName) {

        commandInformation objectCommands = new commandInformation();
        System.out.println(objectCommands.switchMethods(commandName));

    }

    public static void main(String[] args) {

        mainFile object = new mainFile();

        try {
            object.setCredentials();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        object.printChat(twitchClient);
        object.detectSubsAndCheers(twitchClient);
        object.timeoutCheck(twitchClient);
        object.banCheck(twitchClient);
        threadTimeoutMapControl();
        threadCheckChannelIDs();
        startTimestamp = System.currentTimeMillis();
        object.get10SecondAverageEndless();
        try {
            object.getAllActiveUsers();
        } catch (IOException e) {
            System.out.println("No file for active viewers found, probably running the bot for the first time!");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            c = DriverManager
                    .getConnection("jdbc:mysql://"+databaseURL,
                            databaseUsername,databasePassword);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        }

        String commands = "start (st), join (j), leave (l), joined (jd), search (s), searchAll (sA), track (t), cancelTrack (cT), trackStatus (tS), 10AVG (avg), addHours (aH), setPrefix (sP), totalViewers (tV), totalMessages (tM), uptime (ut), setavglimit (sal), getUserSize (gus), commands (cmds), info, help (h)";
        System.out.println("Currently using +1 hour time addition. Use " +prefix +"addHours to adjust!\nCurrent prefix is \"" +prefix +"\"\nLimit for Average Messages per Second set to 75");

        Thread t1 = new Thread(new Runnable(){
            @Override
            public void run() {
                while(true) {
                    Scanner sc = new Scanner(System.in);
                    String string = sc.nextLine();
                    String [] split = string.split(" ");

                    if(split[0].charAt(0) == prefix) {
                        switch (split[0].substring(1)) {
                            case "start": case "st":
                                if(!started) {
                                    String pref = "abugoku9999,adeptthebest,amar,annitheduck,arrowcs," +
                                            "bastighg,bratishkinoff,breitenberg,chefstrobel,chess," +
                                            "derfruchtzwergtv,di1araas,edizderbreite,eliasn97,elotrix," +
                                            "elspreen,exsl95,fibii,filow,fritz_meinecke,gamerbrother," +
                                            "gebauermarc,gmhikaru,gothamchess,gronkh,handofblood," +
                                            "honeypuu,illojuan,inscope21tv,jayzumjiggy,k4yfour,kaicenat," +
                                            "kobrickrl,kuchentv,letshugotv,loserfruit,m0xyy,mckytv," +
                                            "mertabimula,missmikkaa,mizkif,montanablack88,mshl_louis," +
                                            "nihachu,niklaswilson,ninja,nmplol,noahzett28,nooreax,ohnepixel," +
                                            "papaplatte,pietsmiet,pokelawls,pokimane,psp1g,qtcinderella," +
                                            "quitelola,realmoji,reeze,revedtv,rewinside,rezo,ronnyberger," +
                                            "rumathra,scor_china,shlorox,shurjoka,sidneyeweka,skylinetvlive," +
                                            "sodapoppin,sparkofphoenixtv,stegi,summit1g,tanzverbot," +
                                            "therealknossi,tobifas_,trainwreckstv,trymacs,unsympathisch_tv," +
                                            "wichtiger,xqc,xrohat,xthesolutiontv,zackrawrr,zarbex,zastela";
                                    try {
                                        mySQLFile.createMainTable(c, stmt, "chatlogs2");
                                        mySQLFile.createIDTable(c, stmt);
                                    } catch (SQLException throwables) {
                                        System.out.println("Table already exists!");
                                    }
                                    try {
                                        joinChannel(twitchClient, pref, object);
                                    } catch (SQLException throwables) {
                                        throwables.printStackTrace();
                                    }
                                    started = true;
                                    System.out.println("Successfully finished joining!");
                                }
                                break;
                            case "join": case "j":
                                if(split.length == 2) {
                                    try {
                                        joinChannel(twitchClient, split[1], object);
                                    } catch (SQLException throwables) {
                                        throwables.printStackTrace();
                                    }
                                }
                                break;
                            case "leave": case "l":
                                if(split.length == 2 && object.checkJoinedStatus(split[1])) {
                                    object.leaveChannel(twitchClient, split[1]);
                                    joinedChannels.remove(split[1]);
                                }
                                break;
                            case "joined": case "jd":
                                for (String s : joinedChannels) {
                                    System.out.println(s);
                                }
                                System.out.println("Total Count: " +joinedChannels.size());
                                break;
                            case "search": case "s":
                                if(split.length == 3) {
                                    object.searchAndSaveUserMessages(twitchClient, split);
                                }
                                break;
                            case "searchAll": case "sA":
                                if(split.length == 2) {
                                    object.searchAllAndSaveUserMessages(twitchClient, split);
                                }
                                break;
                            case "track": case "t":
                                if(split.length == 2) {
                                    tracking = true;
                                    trackedUser = split[1];
                                }
                                break;
                            case "cancelTrack": case "cT":
                                tracking = false;
                                break;
                            case "trackStatus": case "tS":
                                System.out.println("Tracking set to " +tracking);
                                break;
                            case "10AVG": case "avg":
                                if(!locked10SecondAverage) {
                                    object.get10SecondAverage();
                                }
                                break;
                            case "addHours": case "aH":
                                if(split.length == 2) {
                                    try {
                                        additionalHours = Integer.parseInt(split[1]);
                                    }
                                    catch (NumberFormatException e) {
                                        System.out.println("Invalid number");
                                    }
                                }
                                break;
                            case "setPrefix": case "sP":
                                if(split.length == 2 && split[1].length() == 1) {
                                    object.setPrefix(split[1].charAt(0));
                                }
                                break;
                            case "totalViewers": case "tV":
                                object.printTotalJoinedViewerCount(twitchClient);
                                break;
                            case "totalMessages": case "tM":
                                object.getTotalMessageCount();
                                break;
                            case "uptime": case "ut":
                                object.getUptime();
                                break;
                            case "setavglimit": case "sal":
                                if(split.length == 2) {
                                    averageMessageLimit = Integer.parseInt(split[1]);
                                    System.out.println("Average Message Limit set to " +averageMessageLimit);
                                }
                                else if(split.length == 1) {
                                    System.out.println("Average Message Limit is " +averageMessageLimit);
                                }
                                break;
                            case "getUserSize": case "gus":
                                System.out.println(object.getHashSetSize());
                                break;
                            case "getLastMessageCount": case "glmc":
                                System.out.println(lastMessages.size());
                                break;
                            case "setLastMessageTimer": case "slmt":
                                if(split.length == 2) {
                                    int temp = lastMessagesTimer;
                                    try {
                                        temp = Integer.parseInt(split[1]);
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid input");
                                    }
                                    if(temp <= 86400 && temp > 0) {
                                        object.setLastMessageTimer(temp);
                                    }
                                    else {
                                        System.out.println("Number out of range, has to be between 1 and 86400!");
                                    }
                                }
                                else if(split.length == 1) {
                                    System.out.println("Last Message Timer is " +lastMessagesTimer);
                                }
                                break;
                            case "printConcurrentModException": case "pcme":
                                if(split.length == 2) {
                                    if(split[1].equals("false") || split[1].equals("f")) {
                                        printConcurrentModException = false;
                                        System.out.println("Set boolean \"printConcurrentModException\" to false");
                                    }
                                    else if(split[1].equals("true") || split[1].equals("t")) {
                                        printConcurrentModException = true;
                                        System.out.println("Set boolean \"printConcurrentModException\" to true");
                                    }
                                }
                                break;
                            case "commands": case "cmds":
                                System.out.println("Commands: " +commands);
                                break;
                            case "info":
                                if(split.length == 2) {
                                    object.getCommandInfo(split[1]);
                                }
                                break;
                            case "help": case "h":
                                System.out.println("------------------------------------------------------------\n" +
                                        "start: starts the bot with preferred channels\n" +
                                        "join [channel1,channel2,channel3...]: join specified channels\n" +
                                        "leave [channel]: leave specified channel\n" +
                                        "joined: show joined channels\n" +
                                        "search [username] [channel]: creates file in folder 'userLogs' with users messages in channel\n" +
                                        "searchAll [username]: creates file in folder 'userLogs' with user messages across all joined channels\n" +
                                        "track [username]: track user across all joined channels\n" +
                                        "cancelTrack: cancels tracking\n" +
                                        "trackStatus: prints current track status\n" +
                                        "10AVG: prints average #messages from last 10 seconds\n" +
                                        "addHours [number]: adds/subtracts hours from time format\n" +
                                        "setPrefix [prefix]: sets new prefix\n" +
                                        "totalViewers: prints total viewer count across all joined channels\n" +
                                        "totalMessages: prints total number of messages\n" +
                                        "uptime: prints the time of bootup and uptime\n" +
                                        "setavglimit [number]: sets the limit of avg messages per second before throwing warnings\n" +
                                        "getUserSize: prints the size of HashMap \"ActiveUsers\"\n" +
                                        "getLastMessageCount: prints the size of HashMap \"LastMessages\"\n" +
                                        "setLastMessageTimer [number]: sets the timer for resetting last messages (in seconds)\n" +
                                        "printConcurrentModException [false/true]: Debugging for HashMap modification\n" +
                                        "commands: show commands\n" +
                                        "info [commandName]: shows more detailed info on command\n" +
                                        "help: displays this page\n" +
                                        "------------------------------------------------------------");
                                break;
                            default:
                                System.out.println("Invalid command!");
                                break;
                        }
                    }
                }
            }
        });
        t1.start();
    }
}