import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.chat.events.channel.UserBanEvent;
import com.github.twitch4j.chat.events.channel.UserTimeoutEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;


public class mySQLFile {

    public static void createMainTable(Connection c, Statement stmt, String channelName) throws SQLException {
        stmt = c.createStatement();
        String sql = "CREATE TABLE " +channelName +
                " (USERID        INT        NOT NULL," +
                " USERNAME       CHAR(50)   NOT NULL, " +
                " CHANNELID      INT        NOT NULL, " +
                " CHANNELNAME    CHAR(100)  NOT NULL, " +
                " DATE           CHAR(30)   NOT NULL, " +
                " MESSAGE        TEXT       NOT NULL)" +
                " CHARACTER SET  utf8mb4" +
                " COLLATE        utf8mb4_general_ci";
        stmt.executeUpdate(sql);
        stmt.close();
    }

    public static void createIDTable(Connection c, Statement stmt) throws SQLException {
        stmt = c.createStatement();
        String sql = "CREATE TABLE userids" +
                " (USERID  INT PRIMARY KEY  NOT NULL," +
                " NAME           CHAR(100)  NOT NULL) " +
                " CHARACTER SET  utf8mb4" +
                " COLLATE        utf8mb4_general_ci";
        stmt.executeUpdate(sql);
        stmt.close();
    }

    public static void createSubsAndCheersTable(Connection c, Statement stmt) throws SQLException {
        stmt = c.createStatement();
        String sql = "CREATE TABLE SubsAndCheers" +
                " (USERID        INT        NOT NULL," +
                " USERNAME       CHAR(50)   NOT NULL, " +
                " CHANNELID      INT        NOT NULL, " +
                " CHANNELNAME    CHAR(100)  NOT NULL, " +
                " RECIPIENTID    INT        NOT NULL, " +
                " RECIPIENTNAME  CHAR(50)   NOT NULL, " +
                " DATE           CHAR(30)   NOT NULL, " +
                " SUBMONTH       INT        NOT NULL, " +
                " SUBTIER        INT        NOT NULL, " +
                " BADGES         TEXT       NOT NULL)" +
                " CHEERERTIER    INT        NOT NULL, " +
                " MESSAGE        TEXT       NOT NULL)" +
                " CHARACTER SET  utf8mb4" +
                " COLLATE        utf8mb4_general_ci";
        stmt.executeUpdate(sql);
        stmt.close();
    }

    public static void createTimeoutsAndBansTable(Connection c, Statement stmt) throws SQLException {
        stmt = c.createStatement();
        String sql = "CREATE TABLE userids" +
                " (USERID        INT        NOT NULL," +
                " USERNAME       CHAR(50)   NOT NULL, " +
                " CHANNELID      INT        NOT NULL, " +
                " CHANNELNAME    CHAR(100)  NOT NULL, " +
                " DATE           CHAR(30)   NOT NULL, " +
                " DURATION       INT        NOT NULL, " +
                " REASON         TEXT       NOT NULL)" +
                " MESSAGE        TEXT       NOT NULL)" +
                " CHARACTER SET  utf8mb4" +
                " COLLATE        utf8mb4_general_ci";
        stmt.executeUpdate(sql);
        stmt.close();
    }

    public static void insertData(Connection c, ChannelMessageEvent event, String channelName) throws SQLException {

        String date = mainFile.timeFormatDate.format(LocalDateTime.now().plusHours(mainFile.additionalHours));
        int userid = Integer.parseInt(event.getUser().getId());
        int channelid = Integer.parseInt(event.getChannel().getId());
        String channelname = event.getChannel().getName();
        String name = event.getUser().getName();
        String msg = event.getMessage();
    //    System.out.println(user.getUsers().get(0).getBroadcasterType());
    //    System.out.println(user.getUsers().get(0).getType());

        String sql = "INSERT INTO " +channelName +" (USERID,USERNAME,CHANNELID,CHANNELNAME,DATE,MESSAGE) VALUES (?,?,?,?,?,?);";
        try (PreparedStatement pstmt = c.prepareStatement(sql);) {

            String originalString = msg;
            InputStream inputStream = new ByteArrayInputStream(originalString.getBytes());

            pstmt.setInt(1, userid);
            pstmt.setString(2, name);
            pstmt.setInt(3, channelid);
            pstmt.setString(4, channelname);
            pstmt.setString(5, date);
            pstmt.setBytes(6, msg.getBytes(StandardCharsets.UTF_8));
         //   pstmt.setBinaryStream(5,inputStream);

            pstmt.executeUpdate();
        }
        c.commit();
    }

    public static void insertTimeoutData(Connection c, UserTimeoutEvent event, String lastMessage) throws SQLException {

        int userid = Integer.parseInt(event.getUser().getId());
        String username = event.getUser().getName();
        int channelid = Integer.parseInt(event.getChannel().getId());
        String channelname = event.getChannel().getName();
        String date = mainFile.timeFormatDate.format(LocalDateTime.now().plusHours(mainFile.additionalHours));
        int duration = event.getDuration();
        String reason = event.getReason();

        String sql = "INSERT INTO TimeoutsAndBans (USERID,USERNAME,CHANNELID,CHANNELNAME,DATE,DURATION,REASON,LASTMESSAGE) VALUES (?,?,?,?,?,?,?,?);";
        try (PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setInt(1, userid);
            pstmt.setString(2, username);
            pstmt.setInt(3, channelid);
            pstmt.setString(4, channelname);
            pstmt.setString(5, date);
            pstmt.setInt(6, duration);
            pstmt.setString(7, reason);
            pstmt.setBytes(8, lastMessage.getBytes(StandardCharsets.UTF_8));
            //   pstmt.setBinaryStream(5,inputStream);

            pstmt.executeUpdate();
        }
        c.commit();
    }

    public static void insertBanData(Connection c, UserBanEvent event, String lastMessage) throws SQLException {

        int userid = Integer.parseInt(event.getUser().getId());
        String username = event.getUser().getName();
        int channelid = Integer.parseInt(event.getChannel().getId());
        String channelname = event.getChannel().getName();
        String date = mainFile.timeFormatDate.format(LocalDateTime.now().plusHours(mainFile.additionalHours));
        int duration = -1;

        String sql = "INSERT INTO TimeoutsAndBans (USERID,USERNAME,CHANNELID,CHANNELNAME,DATE,DURATION,REASON,LASTMESSAGE) VALUES (?,?,?,?,?,?,?,?);";
        try (PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setInt(1, userid);
            pstmt.setString(2, username);
            pstmt.setInt(3, channelid);
            pstmt.setString(4, channelname);
            pstmt.setString(5, date);
            pstmt.setInt(6, duration);
            pstmt.setString(7, null);
            pstmt.setBytes(8, lastMessage.getBytes(StandardCharsets.UTF_8));
            //   pstmt.setBinaryStream(5,inputStream);

            pstmt.executeUpdate();
        }
        c.commit();
    }

    public static void insertSubAndCheerData(Connection c, IRCMessageEvent event) throws SQLException {

        int userid = Integer.parseInt(event.getUserId());
        String username = event.getUserName();
        int channelid = Integer.parseInt(event.getChannelId());
        String channelname = event.getChannelName().orElse("");
        int recipientid = Integer.parseInt(event.getTagValue("msg-param-recipient-id").orElse("-1"));
        String recipientname = event.getTagValue("msg-param-recipient-user-name").orElse("[#RESUB OR CHEER#]");
        int subMonth = event.getSubscriberMonths().orElse(-1);
        int subTier = event.getSubscriptionTier().orElse(-1);
        String badges = event.getBadges().toString();
        int cheererTier = event.getCheererTier().orElse(-1);
        String message = event.getMessage().orElse("[#No message#]");
        String date = mainFile.timeFormatDate.format(LocalDateTime.now().plusHours(mainFile.additionalHours));

        String sql = "INSERT INTO SubsAndCheers (USERID,USERNAME,CHANNELID,CHANNELNAME,RECIPIENTID,RECIPIENTNAME,DATE,SUBMONTH,SUBTIER,BADGES,CHEERERTIER,MESSAGE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";
        try (PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setInt(1, userid);
            pstmt.setString(2, username);
            pstmt.setInt(3, channelid);
            pstmt.setString(4, channelname);
            pstmt.setInt(5, recipientid);
            pstmt.setString(6, recipientname);
            pstmt.setString(7,date);
            pstmt.setInt(8, subMonth);
            pstmt.setInt(9, subTier);
            pstmt.setString(10, badges);
            pstmt.setInt(11, cheererTier);
            pstmt.setBytes(12, message.getBytes(StandardCharsets.UTF_8));
            //   pstmt.setBinaryStream(5,inputStream);

            pstmt.executeUpdate();
        }
        c.commit();
    }

    public static void insertAllData(Connection c, ChannelMessageEvent event, String channelName) throws SQLException {

        String date = mainFile.timeFormatDate.format(LocalDateTime.now().plusHours(mainFile.additionalHours));
        int id = Integer.parseInt(event.getUser().getId());
        String channelname = event.getChannel().getName();
        String username = event.getUser().getName();
        String msg = event.getMessage();
        //    System.out.println(user.getUsers().get(0).getBroadcasterType());
        //    System.out.println(user.getUsers().get(0).getType());

        String sql = "INSERT INTO " +channelName +" (USERID,USERNAME,CHANNELNAME,DATE,MESSAGE) VALUES (?,?,?,?,?);";
        try (PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setInt(1, id);
            pstmt.setString(2, username);
            pstmt.setString(3, channelname);
            pstmt.setString(4, date);
            pstmt.setBytes(5, msg.getBytes(StandardCharsets.UTF_8));

            pstmt.executeUpdate();
        }
        c.commit();
    }

    public static void insertDataIDs(Connection c, ChannelMessageEvent event) throws SQLException {

        int id = Integer.parseInt(event.getUser().getId());
        String name = event.getUser().getName();

        String sql = "INSERT INTO userids" +" (USERID,NAME) VALUES (?,?);";
        try (PreparedStatement pstmt = c.prepareStatement(sql);) {


            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            //   pstmt.setBinaryStream(5,inputStream);

            pstmt.executeUpdate();
        }
        catch (Exception ignored) {

        }
        c.commit();
    }

    public static void main( String args[] ) throws SQLException {

//        createMainTable(mainFile.c, null, "chatlogs1");
//        createIDTable(mainFile.c, null);
//        createSubsAndCheersTable(mainFile.c, null);
//        createTimeoutsAndBansTable(mainFile.c, null);

    }
}