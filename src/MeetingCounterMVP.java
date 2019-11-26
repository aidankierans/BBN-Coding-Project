import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/** A "quick and dirty" implementation of the DayCounter interface. 
 * 
 * @author Aidan Kierans
 */
public class MeetingCounterMVP implements DayCounter {

	public static void main(String[] args) throws FileNotFoundException {
		String[][] ms = null;
		
		if(args.length >= 1) {
			ms = fileTo2DArray(args[0]); 
		}
		else {
			// Assume the file with the meetings is in project folder
			ms = fileTo2DArray("input.csv");
		}				
		
		MeetingCounterThorough mC = new MeetingCounterThorough();
		for(int i = 0; i < ms.length; i++) {
			int[] start = dStringToArr(ms[i][0]);
			int[] end = dStringToArr(ms[i][1]);			
			int weekDay = weekDayFromString(ms[i][2]);
			int meetingCount = mC.countInRange(start, end, weekDay);
			System.out.println("Meeting " + i + ": " + meetingCount);
		}
	}

	/** Convert a date from String form to an integer array for ease of manipulation.
	 * 
	 * @param dStr A date represented as a series of numbers separated by some other character.
	 * @return An integer array representation of a date in the same format as the input date.
	 * In this general inputs are expected in YYYY-MM-DD format.
	 */
	public static int[] dStringToArr(String dStr) {
		String[] strArr = dStr.split("\\D"); // split on non-digits
		int[] date = new int[strArr.length];
		for(int i = 0; i < strArr.length; i++) {
			date[i] = Integer.parseUnsignedInt(strArr[i]);
		}
		return date;
	}

	/** Convert a string representation of a day of the week to an integer representation.
	 * 
	 * @param dWeek The day of the week
	 * @return An integer representation of the day of the week, where Sunday is represented
	 * as 0, Monday as 1, etc., and an input that isn't recognized is -1.
	 */
	public static int weekDayFromString(String dWeek) {
		String day = dWeek.toLowerCase().trim();
		switch (day) {
		case "sunday" : return 0;
		case "monday" : return 1;
		case "tuesday" : return 2;
		case "wednesday" : return 3;
		case "thursday" : return 4;
		case "friday" : return 5;
		case "saturday" : return 6;
		default: return -1;
		}
	}
	
	/** Convert a file to a String array. Currently used to read the input meetings file, which is assumed
	 * to be a CSV of dates and days of the week.
	 * 
	 * @param filepath The String to be used as the filepath for the list of holidays
	 * @return An array of Strings, for which each String represents a row of the input csv
	 * @throws FileNotFoundException Thrown if the file cannot be found.
	 */
	public static String[][] fileTo2DArray(String filepath) throws FileNotFoundException {
		ArrayList<String[]> rows = new ArrayList<String[]>();

		Scanner input = new Scanner(new File(filepath));
		while(input.hasNext()) {
			String line = input.nextLine();
			if(line.startsWith("#")) {
				continue; // Ignore lines that are commented out
			}
			String[] strArr = new String[2];
			strArr = line.split(", ");
			rows.add(strArr);
		}
		input.close();
		
		String[][] meetings = new String[rows.size()][3];
		return rows.toArray(meetings);
	}

}