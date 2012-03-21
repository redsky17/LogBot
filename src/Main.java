import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class Main extends PircBot {



	String nick;
	String login;
	String server;
	String ircchannel;
	String nickserv;
	String link;
	String command;
	public static String prefix;
	public static String folder;

	public static void main(String[] args) {
		try {
			new Main();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadVars() {
		nick = loadProp("Nickname", "LogiiBot");
		login = loadProp("Login", "log");
		server = loadProp("IRCServer", "irc.freenode.net");
		ircchannel = loadProp("Channel", "##reddit-android-developers");
		nickserv = loadProp("NickservPass");
		link = loadProp("Link",
				"https://github.com/RedditAndroidDev/IRC_Logs/tree/master/logs");
		command = loadProp("Command");
		prefix = loadProp("LogPrefix", "log_");
		folder = loadProp("LogDir", "logs");
	}

	public Main() throws NickAlreadyInUseException, IOException, IrcException {
		
		loadVars(); //loads the variables from the config

		this.setName(nick); //sets the nickname
		this.setLogin(login); //sets the login (The part before the hostname: login@111.222.121.110)

		this.setVerbose(true); //sets there to be output on IRC events, should be disabled on production if you really want.

		
		this.setMessageDelay(500);
		join();
	}

	public void join() { //this is used to be recycable to be used at first connect and if the bot gets disconnected
		try {
			this.connect(server); //tries to connect to the irc server
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		this.joinChannel(ircchannel); //joins the irc channel
		if (!nickserv.equalsIgnoreCase("pleasereplace")
				&& !nickserv.equalsIgnoreCase("")) { //checks if there is a nickserv password set, if there is it will attempt to auth.
			this.identify(nickserv);
		}
	}

	public void onJoin(String channel, String sender, String login,
			String hostname) {
		TextListing.addEntry("### " + sender + " has joined " + channel); //adds entry to logs when user joins
	}

	public boolean isOp(String sender, String channel) { //checks if the specified user on the specified channel is an op.

		User users[] = getUsers(channel);
		User u = null;
		for (User user : users) {
			if (sender.equals(user.getNick())) {
				u = user;
				break;
			}
		}
		if (u != null) {
			if (u.isOp()) {
				return true;
			}
		}

		return false;
	}

	public void onMessage(String channel, String sender, String login,
			String hostname, String message) { //event when a conventional message is sent
		TextListing.addEntry("<" + sender + "> " + message); //logs it
		if (message.toLowerCase().startsWith("!logs")) { //checks if it is the !logs commands
			sendMessage(channel, "The link to the log files is: " + link); //if it is it simply sends a message to the server
		}
		if (message.toLowerCase().startsWith("!paste")) { //checks if it is the
			String[] split = message.split(" ");
			if (split.length <= 1) {
				sendMessage(channel, "You need a number!");
			} else {

				String full = "";
				List<String> textListGet = loadLogToList(getCurrentFolder()
						+ getCurrentLogName());
				int count = 0;
			
				if (textListGet.size() < Integer.parseInt(split[1])
						|| split[1].equalsIgnoreCase("-1")) {
					count = textListGet.size()-1;
				} else {
					try{
						count = Integer.parseInt(split[1]);
					}catch(Exception e){
						sendMessage(channel, "Looks like that isn't an integer! Try again.");
						return;
					}
				
				}

		
				for (int i = textListGet.size() - count - 1; i < textListGet // thanks
						.size() - 1; i++) {
					// i =
					try {
						full = full
								+ URLEncoder
										.encode(textListGet.get(i), "UTF-8")
								+ System.getProperty("line.separator");
					} catch (UnsupportedEncodingException e) {
					
						e.printStackTrace();
					}
				}

				sendMessage(
						channel,
						"The log has been uploaded and is at:"
								+ sendPostRequest("http://sprunge.us/",
										"sprunge=" + full));
			}

		}
		if (message.toLowerCase().startsWith("!upload")) {
			if (!command.equalsIgnoreCase("pleasereplace")
					&& !command.equalsIgnoreCase("")) {
				if (isOp(sender, channel)) {
					try {
						Runtime.getRuntime().exec(command);
						sendMessage(channel, "Attempted the upload.");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

	}

	public String getCurrentLogName() {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MMM.yyyy");
		String dateNow = formatter.format(currentDate.getTime());
		return Main.prefix + "" + dateNow + ".txt";
	}

	public String getCurrentFolder() {
		return Main.folder + "" + System.getProperty("file.separator");
	}

	public List<String> loadLogToList(String dest) {
		List<String> textlist = new ArrayList<String>();
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(dest);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				textlist.add(strLine);
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return textlist;
	}

	public void onPrivateMessage(String sender, String login, String hostname,
			String message) {
		if (message.toLowerCase().startsWith("!upload")
				&& sender.equalsIgnoreCase("red_sky")) {
			loadVars();
		}
	}

	public void onDisconnect() {

		join();
	}

	public String sendPostRequest(String url2, String data) {

		// Build parameter string

		try {

			// Send the request
			URL url = new URL(url2);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(
					conn.getOutputStream());

			// write parameters
			writer.write(data);
			writer.flush();

			// Get the response
			StringBuffer answer = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				answer.append(line);
			}
			writer.close();
			reader.close();

			// Output the response
			return answer.toString();

		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void onKick(String channel, String kickerNick, String kickerLogin,
			String kickerHostname, String recipientNick, String reason) {
		TextListing.addEntry("### " + recipientNick + " was kicked from "
				+ channel + " by " + kickerNick + " (" + reason + ")");
		if (recipientNick.equalsIgnoreCase(nick)) {
			this.joinChannel(ircchannel);
			sendMessage(channel, kickerNick + ": Why, just... why?");
		}
	}

	public static String loadProp(String prop) {
		return loadProp(prop, "pleasereplace");

	}

	public void onNickChange(String oldNick, String login, String hostname,
			String newNick) {
		TextListing.addEntry("### " + oldNick + " is now known as " + newNick);
	}

	public void onNotice(String sourceNick, String sourceLogin,
			String sourceHostname, String target, String notice) {
		if (!target.equalsIgnoreCase("notice")
				&& !target.equalsIgnoreCase(nick)) {
			TextListing.addEntry("### NOTICE: <" + sourceNick + "> " + notice);
		}
	}

	public void onPart(String channel, String sender, String login,
			String hostname) {
		TextListing.addEntry("### " + sender
				+ " has parted (left) the channel.");
	}

	public void onQuit(String sourceNick, String sourceLogin,
			String sourceHostname, String reason) {
		TextListing.addEntry("### " + sourceNick + " has quit the server. ("
				+ reason + ")");
	}

	public static String loadProp(String prop, String shouldbe) {
		File file = new File("config.txt");

		// Does the file already exist
		if (!file.exists()) {
			try {
				// Try creating the file
				file.createNewFile();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(file));
			String toreturn = properties.getProperty(prop, shouldbe);
			if (toreturn.equalsIgnoreCase(shouldbe)) {
				properties.setProperty(prop, shouldbe);
			}
			properties.store(new FileOutputStream(file),
					"Set the settings accordingly.");

			return toreturn;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
}
