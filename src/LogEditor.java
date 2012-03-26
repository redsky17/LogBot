import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogEditor {
	public static void addEntry(String entry) { //adds a line to the logs
		try{
			File f = new File(Main.folder); //loads the log folder
			f.mkdir();//creates it if it doesn't exist
			Calendar currentDate = Calendar.getInstance(); //get tehe calender
			  SimpleDateFormat formatter= 
			  new SimpleDateFormat("dd.MMM.yyyy"); //for the log file naming
			  String dateNow = formatter.format(currentDate.getTime());
			  
			 formatter= 
					  new SimpleDateFormat("HH:mm:ss"); //for log line timing
					  String timeNow = formatter.format(currentDate.getTime());
			// Create file 
			  FileWriter fstream = new FileWriter(Main.folder+""+System.getProperty("file.separator")+""+Main.prefix+""+dateNow+".txt",true); //loads the log to append
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write("["+timeNow+"] "+entry+System.getProperty("line.separator")); //adds the time, the log line and a new line
			  //Close the output stream
			  out.close();
			  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
	
	}
}
