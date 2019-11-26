import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/** A thorough implementation of the DayCounter interface. The main improvement of this
 * version is that it takes holidays into account, and excludes holidays from consideration.
 * 
 * @author Aidan Kierans
 */
public class MeetingCounterThorough implements DayCounter {

	/** Count the instances of a day of the week between some date and some other 
	 * date, inclusive. Do not count the instances that coincide with holidays, if any.
	 * @see DayCounter#countInRange(java.lang.String, java.lang.String, java.lang.String)
	 *
	 * @param start The first date in the range
	 * @param end The last date of the range
	 * @param weekDay The day of the week
	 * @param holidays The holidays to observe
	 * @return The number of instances of that day of the week in that range of dates.
	 */	
	public int countInRange(int[] start, int[] end, int weekDay, int[][] holidays) {
		int count = DayCounter.super.countInRange(start, end, weekDay);
		if(null != holidays) {
			count -= intersectingHolidays(start, end, weekDay, holidays);
		}
		return count;
	}

	/** Count the holidays that happen between two dates (inclusive) and occur on the given weekday.
	 * 
	 * @param start The first date in the range
	 * @param end The last date of the range
	 * @param week The day of the week
	 * @param holidays The array of dates to be considered holidays, in which yearly holidays 
	 * have a year of -1.
	 * @return The number of instances of that day of the week in that range of dates.
	 */
	public static int intersectingHolidays(int[] start, int[] end, int weekDay, int[][] holidays) {
		int count = 0;
		for(int[] holiday : holidays) {
			int[] h = holiday.clone();
			if(h[0] == -1) {
				if(start[0] != end[0]) {
					//  Check whether the holiday coincides with a meeting in the first year
					h[0] = start[0];
					int wkd = DayCounter.weekDayFromDate(h);
					count += (wkd == weekDay && DayCounter.isDateBefore(start, h)) ? 1 : 0;

					/* Efficiently check whether the holiday coincides with a meeting on any
					 * intermediate years. Rather than recalculate the weekday every year from
					 * scratch, update the weekday every year based on whether or not a leap day
					 * has been passed.
					 */
					for(int i = start[0] + 1; i < end[0]; i++) {
						if(h[1] <= 2) {
							wkd += DayCounter.isLeapYear(i - 1) ? 2 : 1;
						}
						else {
							wkd += DayCounter.isLeapYear(i) ? 2 : 1;							
						}
						wkd = wkd % 7;
						count += wkd == weekDay ? 1 : 0;
					}

					//  Check whether the holiday coincides with a meeting in the last year
					h[0] = end[0];
					wkd = DayCounter.weekDayFromDate(h);
					count += DayCounter.isDateBefore(h, end) ? 1 : 0;
				}
				else {
					h[0] = start[0];
					assert h[0] == end[0]; // sanity check
				}
			}
			assert h[0] != -1; // sanity check
			if(weekDay != DayCounter.weekDayFromDate(h)) {
				continue;
			}			
			count += (DayCounter.isDateBefore(start, h) && DayCounter.isDateBefore(h, end)) ? 1 : 0;					
		}
		return count;
	}

	public static void main(String[] args) throws FileNotFoundException {
		String[][] ms = null;
		String[] h = null;

		if(args.length >= 1) {
			ms = fileTo2DArray(args[0]); 
			if(args.length >= 2) {
				h = fileToArray(args[1]);
			}
		}
		else {
			// Assume the file with the meetings is in project folder
			ms = fileTo2DArray("input.csv");
			// Assume the same for the file with the holidays
			h = fileToArray("holidays.csv");
		}				

		int[][] hs = new int[h.length][3];
		for(int i = 0; i < hs.length; i++) {
			hs[i] = dStringToArr(h[i]);
		}

		MeetingCounterThorough mC = new MeetingCounterThorough();
		for(int i = 0; i < ms.length; i++) {
			int[] start = dStringToArr(ms[i][0]);
			int[] end = dStringToArr(ms[i][1]);			
			int weekDay = weekDayFromString(ms[i][2]);
			int meetingCount = mC.countInRange(start, end, weekDay, hs);
			System.out.println("Meeting " + i + ": " + meetingCount);
		}
	}

	/** Convert a date from String form to an integer array for ease of manipulation.
	 * 
	 * @param dStr A date represented as a series of numbers separated by some other character.
	 * @return An integer array representation of a date in the same format as the input date.
	 * In this general inputs are expected in YYYY-MM-DD or MM-DD format.
	 */
	public static int[] dStringToArr(String dStr) {
		String[] strArr = dStr.split("\\D"); // split on non-digits
		int[] date = new int[3];
		if(strArr.length == 2) {
			date[0] = -1;
			for(int i = 1; i < date.length; i++) {
				date[i] = Integer.parseUnsignedInt(strArr[i - 1]);
			}
		}
		else {
			assert strArr.length == 3;
			for(int i = 0; i < date.length; i++) {
				date[i] = Integer.parseUnsignedInt(strArr[i]);
			}
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

	/** Convert a file to a String array. Currently used to read the holiday file, which is assumed
	 * to be a CSV of dates separated by commas.
	 * 
	 * @param filepath The String to be used as the filepath for the list of holidays
	 * @return An array of Strings, for which each String represents a row of the input csv
	 * @throws FileNotFoundException Thrown if the file cannot be found.
	 */
	public static String[] fileToArray(String filepath) throws FileNotFoundException {
		String[][] arr2d = fileTo2DArray(filepath);

		String[] arr = new String[arr2d[0].length];
		for(int i = 0; i < arr2d[0].length; i++) {
			arr[i] = arr2d[0][i];
		}

		return arr;
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
			strArr = line.split(",\\s?");
			rows.add(strArr);
		}
		input.close();

		String[][] meetings = new String[rows.size()][3];
		return rows.toArray(meetings);
	}

}