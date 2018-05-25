package com.javatican.stock.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.javatuples.Pair;
import org.jsoup.nodes.Document;

public class StockUtils {

	private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd");
	/*
	 * format the current date as a string of '20180101' format
	 */
	public static String todayDateString() {
		Calendar cal = Calendar.getInstance();
		return format.format(cal.getTime());
	}
	/*
	 * parse a date string of '2018/01/01' format
	 */
	public static Optional<Date> stringToDate(String date) {
		try {
			return Optional.of(format2.parse(date));
		} catch (Exception ex) {
			return Optional.empty();
		}
	}
	/*
	 * format a date object as '20180101' -like string
	 */
	public static String dateToSimpleString(Date date) {
		return format.format(date);
	}
	/*
	 * format a date object as '2018/01/01' -like string
	 */
	public static String dateToStringSeparatedBySlash(Date date) {
		return format2.format(date);
	}
	/*
	 * write the parsed document into a file
	 */
	public static void writeDocumentToFile(Document doc, String filename) {
		try (BufferedWriter htmlWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));) {
			htmlWriter.write(doc.toString());
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
	 * remove comma separators in the parsed number records
	 */
	public static String removeCommaInNumber(String number) {
		return number.replace(",", "");
	}
	/*
	 * return the first day of the current month as string
	 */
	public static String getFirstDayOfCurrentMonth() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return format.format(cal.getTime());
	}
	/*
	 * calculate the first day and last day of the last month
	 */
	public static Pair<Date, Date> getFirstAndLastDayOfLastMonth(){
		Calendar cal = Calendar.getInstance(); 
		int currentYear = cal.get(Calendar.YEAR);
		int currentMonth = cal.get(Calendar.MONTH);
		//first day of this month
		cal.set(currentYear, currentMonth, 1);
		//last day of previous month
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date second = cal.getTime();
		//first day of previous month
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date first = cal.getTime();
		return Pair.with(first, second);
	}
	/*
	 * calculate the date strings for the first day of each month within past 6 months period.
	 */
	public static List<String> calculateDateStringPastSixMonth(){
		List<String> result = new ArrayList<>();
		Calendar cal = Calendar.getInstance(); 
		int currentYear = cal.get(Calendar.YEAR);
		int currentMonth = cal.get(Calendar.MONTH);
		int day = 1;
		for(int i=5; i>=0; i--) {
			int month=currentMonth-i;
			int year = currentYear;
			if(month<0) {
				month+=12;
				year--;
			}
			cal.set(year, month, day);
			result.add(format.format(cal.getTime()));
		}
		return result;
	}

}
