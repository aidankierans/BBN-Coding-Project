/** Interface for counting occurrences of a recurring meeting between two dates. 
 * 
 * @author Aidan Kierans
 *
 */
public interface DayCounter {
	
	/** Count the instances of a day of the week between some date and some other 
	 * date, inclusive.
	 * 
	 * @param start The first date in the range
	 * @param end The last date of the range
	 * @param weekDay The day of the week
	 * @return The number of instances of that day of the week in that range of dates.
	 */
	public default int countInRange(int[] start, int[] end, int weekDay) {
		int range = daysBetween(start, end);

		// Count most of the relevant days between the first and last day 
		int count = range / 7;
		// Count the relevant day that's left over if there is one
		int remainder = range % 7;		
		count += (Math.abs(weekDay - weekDayFromDate(start)) <= remainder) ? 1 : 0;

		return count;
	}

	/** Count the days between two dates, inclusive.
	 * 
	 * @param dStart The earlier date
	 * @param dEnd The later date
	 * @return The inclusive range between the two input dates
	 */
	public static int daysBetween(int[] start, int[] end) {
		int dif = 0;
		
		// Make sure the years are reasonable
		assert start[0] != -1 && end[0] != -1;
		// Make sure the dates are in the right order
		assert start[0] <= end[0];
		if(start[0] == end[0]) {
			assert start[1] <= end[1];
			if(start[1] == end[1]) {
				assert start[2] <= end[2];
				// If this point is reached, the answer is really easy:
				return end[2] - start[2] + 1;
			}
		}

		// Exclude days before start date and after end date
		dif -= daysBeforeDate(start);
		dif -= 365 - daysBeforeDate(end) + (isLeapYear(end[0]) ? 1 : 0);
		// Count days of all years between start date and end date (inclusive)
		for(int y = start[0]; y <= end[0]; y++) {
			dif += 365;
			if(isLeapYear(y)) {
				dif += 1;
			}
		}

		return dif;
	}

	/** Count the number of days preceding some date in a year.
	 * 
	 * @param date An integer array representation of a date.
	 * @return The total number of days that occur before that date in that year.
	 */
	public static int daysBeforeDate(int[] date) {
		int count = 0;
		// Days of each month in a non-leap year
		int[] monthDays = new int[] {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		monthDays[1] += isLeapYear(date[0]) ? 1 : 0;

		for(int m = 1; m < date[1]; m++) { // Add days from each of the previous months
			count += monthDays[m - 1];
		}
		count += date[2] - 1; // Add days in this month before this day
		return count;
	}


	/** Find the weekday of a date.
	 * Algorithm source: https://artofmemory.com/blog/how-to-calculate-the-day-of-the-week-4203.html
	 * @param date An integer array representation of a date.
	 * @return An integer representation of the day of the week.
	 */
	public static int weekDayFromDate(int[] date) {
		int yearCode = date[0] % 100;
		yearCode = (yearCode + (yearCode/4)) % 7;
		int[] mc = new int[] {0, 3, 3, 6, 1, 4, 6, 2, 5, 0, 3, 5};
		int monthCode = mc[date[1] - 1];
		int centuryCode = 6; // Assume all dates are between 2000 and 2100
		int leapYearCode = (isLeapYear(date[0]) && date[1] <= 2) ? 1 : 0;
		// Result is an integer between 0 and 6 inclusive such that 0 means Sunday
		return (yearCode + monthCode + centuryCode + date[2] - leapYearCode) % 7;
	}

	/** Check whether a given year is a leap year.
	 * 
	 * @param year The year to check.
	 * @return True if the year is a leap year, false otherwise.
	 */
	public static boolean isLeapYear(int year) {
		if(year % 4 == 0 && year % 100 != 0) {
			return true;
		}
		else if(year % 400 == 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/** Compare two dates to determine their order.
	 * 
	 * @param firstD The first date, represented as an integer array.
	 * @param secondD The second date, represented as an integer array.
	 * @return True if firstD occurs chronologically before secondD, or if firstD and secondD
	 *  are the same date. In other words, return false if and only if secondD occurs before 
	 *  firstD.
	 */
	public static boolean isDateBefore(int[] firstD, int[] secondD) {
		assert firstD.length == secondD.length;
		for(int i = 0; i < firstD.length; i++) {
			if(firstD[i] < secondD[i]) {
				return true; 
			}
			if(firstD[i] > secondD[i]) {
				return false; 
			}
		}
		assert firstD.equals(secondD); // sanity check
		return true;
	}

}