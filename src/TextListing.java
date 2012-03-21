import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TextListing {
	public static void addEntry(String entry) {
		try{
			File f = new File(Main.folder);
			f.mkdir();
			Calendar currentDate = Calendar.getInstance();
			  SimpleDateFormat formatter= 
			  new SimpleDateFormat("dd.MMM.yyyy");
			  String dateNow = formatter.format(currentDate.getTime());
			  
			 formatter= 
					  new SimpleDateFormat("HH:mm:ss");
					  String timeNow = formatter.format(currentDate.getTime());
			// Create file 
			  FileWriter fstream = new FileWriter(Main.folder+""+System.getProperty("file.separator")+""+Main.prefix+""+dateNow+".txt",true);
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write("["+timeNow+"] "+entry+System.getProperty("line.separator"));
			  //Close the output stream
			  out.close();
			  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
	
}
}
