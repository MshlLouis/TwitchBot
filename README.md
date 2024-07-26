# A Bot for Twitch that allows the user to connect to Twitch Channels to read Chat, Timeouts/Bans and Subscriptions/Bit-Cheers. Saves all those events to a database.


## 1: Requirements

- IDE + Gradle v6.8+
- Online Database
- Twitch Account


## 2: Initializing

First, get credentials to authorize your access to Twitch. This can be done using "https://twitchtokengenerator.com". For simplicity it is recommended to grant all permissions by selecting all scopes, though it is possible to only select the ones needed (you gotta figure out that one for yourself, I'm lazy).
Next, prepare your credentials.txt file. The first three inputs MUST BE:
1. ACCESS TOKEN
2. CLIENT ID
3. CLIENT SECRET




Next, create a database online. The next three inputs in your file MUST BE:




4. Full Database URL (e.g. [url]:[port]/[Database Name])
5. Username
6. Password

Finally, create the required tables using the file "mySQLFile.java". The files main method provides all 4 method calls (Don't change the name "chatlogs1", I have absolutely zero clue whether it works with a different name and again, I'm lazy).


## 3: Running

Now you should be able to execute the program. The following information should be printed when executing:
1. "Opened database successfully" (Indicating that you successfully connected the program to your database. If you get an error, proceed with caution!)
2. "Currently using +1 hour time addition. Use !addHours to adjust!" (Change the value depending on where you live, e.g. US E is -4h from UTC)
3. Current prefix is "!" (used for commands)
4. "Limit for Average Messages per Second set to 75" (If more than 75 messages per second are being received, it will throw a warning. The value can be changed if needed. The warning is NOT indicating a problem, it's simply for the users info. The critical point is reached at 100+ messages per second)


## 4: Commands

Use !commands or !help to get a description for every available command.
