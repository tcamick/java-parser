import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Cole Amick 
 * Date: 01.29.2014
 */

public class ParserLog {
	private File file;
	private boolean logBit;
	private OutputStream fileOutStream;

	/**
	 * Creates a log file
	 * 
	 * @throws FileNotFoundException
	 */
	public ParserLog(boolean logBit) throws FileNotFoundException {
		this.logBit = logBit;
		if (logBit) {
			System.out.println("ParserLog.txt created with logging enabled");
			file = new File("./ParserLog.txt");
			fileOutStream = new FileOutputStream(file);
		} else
			System.out.println("Logging disabled");

	}

	/**
	 * converts msg into bytes and writes the bytes to the log file adds new
	 * line character to msg
	 * 
	 * @param msg
	 */
	public void logMsg(String msg) {
		// add newline character
		msg += '\n';
		// get bytes
		byte bytes[] = msg.getBytes();
		// write bytes
		if (logBit)
			for (int i = 0; i < msg.length(); i++) {
				try {
					fileOutStream.write(bytes[i]);
				} catch (IOException e) {

					e.printStackTrace();
				}
			}

	}

	/**
	 * Closes log file
	 */
	public void closeLog() {
		if (logBit)
			try {

				fileOutStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
