package mainBot;

public class commandInformation {

    public String switchMethods(String command) {

        switch (command) {
            case "start":
                return start();
            case "join":
                return join();
            case "leave":
                return leave();
            case "joined":
                return joined();
            case "track":
                return track();
            case "cancelTrack":
                return cancelTrack();
            case "trackStatus":
                return trackStatus();
            case "avg":
                return avg();
            case "addHours":
                return addHours();
            case "setPrefix":
                return setPrefix();
            case "totalViewers":
                return totalViewers();
            case "totalMessages":
                return totalMessages();
            case "uptime":
                return uptime();
            case "sal":
                return sal();
            case "gus":
                return gus();
            case "glmc":
                return glmc();
            case "slmt":
                return slmt();
            case "pcme":
                return pcme();
            case "commands":
                return commands();
            case "help":
                return help();
            default:
                return "Command " +command +" doesn't exist!";
        }
    }
    public String start() {
        return "Syntax: " +mainFile.prefix +"start\n"
                +"This command is used to start the bot using a preset of channels to join to avoid a long \"join\" command.";
    }
    public String join() {
        return "Syntax: " +mainFile.prefix +"join [channel1],[channel2],[channel3]...\n"
                +"This command is used to join a channel, multiple joins are separated using ','.";
    }
    public String leave() {
        return "Syntax: " +mainFile.prefix +"leave [channel]\n"
                +"This command is used to leave a channel, only 1 channel is supported for now.";
    }
    public String joined() {
        return "Syntax: " +mainFile.prefix +"joined\n"
                +"This command is used to see what channels have been joined plus the total number.";
    }
    public String track() {
        return "Syntax: " +mainFile.prefix +"track [username]\n"
                +"This command is used to track a given usernames messages live across all joined channels.";
    }
    public String cancelTrack() {
        return "Syntax: " +mainFile.prefix +"cancelTrack\n"
                +"This command is used to cancel tracking.";
    }
    public String trackStatus() {
        return "Syntax: " +mainFile.prefix +"trackStatus\n"
                +"This command is used to print the current tracking status.";
    }
    public String avg() {
        return "Syntax: " +mainFile.prefix +"avg\n"
                +"This command is used to get the average number of messages from the last 10 seconds.";
    }
    public String addHours() {
        return "Syntax: " +mainFile.prefix +"addHours [number]\n"
                +"This command is used to add an x amount of hours to the internal clock.\n"
                +"It's used to print the time of messages received, commands executed and more.\n"
                +"Use negative numbers to subtract hours.";
    }
    public String setPrefix() {
        return "Syntax: " +mainFile.prefix +"setPrefix [singular character]\n"
                +"This command is used to set the prefix for commands. Only 1 singular character is accepted.";
    }
    public String totalViewers() {
        return "Syntax: " +mainFile.prefix +"totalViewers\n"
                +"This command is used to iterate through all joined live channels and print the total number of viewers.";
    }
    public String totalMessages() {
        return "Syntax: " +mainFile.prefix +"totalMessages\n"
                +"This command is used to print the total number of messages received since last activation of the bot";
    }
    public String uptime() {
        return "Syntax: " +mainFile.prefix +"uptime\n"
                +"This command is used to print the date plus time of bootup and the uptime.";
    }
    public String sal() {
        return "Syntax: " +mainFile.prefix +"sal (optional)[number]\n"
                +"This command is used to print the current limit of messages per second before a warning is thrown (standard 75).\n"
                +"Giving a number as an argument will change the variable to given number.\n"
                +"WARNING: The value should be between 50-200. Everything above 200 might run into complications!";
    }
    public String gus() {
        return "Syntax: " +mainFile.prefix +"gus\n"
                +"This command is used to print the current size of the HashMap for active users.";
    }
    public String glmc() {
        return "Syntax: " +mainFile.prefix +"glmc\n"
                +"This command is used to print the current size of the HashMap for last messages.\n"
                +"Entries are deleted after 300 seconds by default to prevent the HashMap from growing too large.";
    }
    public String slmt() {
        return "Syntax: " +mainFile.prefix +"slmt (optional)[number]\n"
                +"This command is used to set the timer for resetting the HashMap \"LastMessages\"\n"
                +"The input has to be a whole number between 1-86400 (inclusive), representing seconds\n"
                +"If no number is provided, the current Timer is printed.";
    }
    public String pcme() {
        return "Syntax: " +mainFile.prefix +"pcme [boolean]\n"
                +"This command is used to set the boolean for debugging the HashMap \"LastMessages\"\n"
                +"Sometimes it can happen that removing an entry fails.\n"
                +"This is purely for debugging and doesn't serve any purpose for regular users.";
    }
    public String commands() {
        return "Syntax: " +mainFile.prefix +"commands\n"
                +"This command is used to print the currently active commands.";
    }
    public String help() {
        return "Syntax: " +mainFile.prefix +"help\n"
                +"This command is used to provide a brief help for every command.";
    }
}