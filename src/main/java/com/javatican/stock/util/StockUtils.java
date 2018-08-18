package com.javatican.stock.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.javatuples.Pair;
import org.jsoup.nodes.Document;

import com.javatican.stock.model.PortfolioItem;

public class StockUtils {

	private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd");
	private static final String FOUR_DEC_DOUBLE_FORMAT = "##.0000";
	private static final String ONE_DEC_DOUBLE_FORMAT = "##.0";
	private static final String TWO_DEC_DOUBLE_FORMAT = "##.00";
	private static final String ZERO_DEC_DOUBLE_FORMAT = "##.";

	public static boolean isFriday(Date theDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(theDate);
		// dayOfWeek starts from 1(Sunday)
		return cal.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY;
	}

	/**
	 * Round double.
	 *
	 * @param value
	 *            the value
	 * @param format
	 *            the format
	 * @return the double
	 */
	public static double roundDoubleDp4(double value) {
		DecimalFormat df = new DecimalFormat(FOUR_DEC_DOUBLE_FORMAT);
		return Double.valueOf(df.format(value));
	}

	public static double roundDoubleDp2(double value) {
		DecimalFormat df = new DecimalFormat(TWO_DEC_DOUBLE_FORMAT);
		return Double.valueOf(df.format(value));
	}

	public static double roundDoubleDp1(double value) {
		DecimalFormat df = new DecimalFormat(ONE_DEC_DOUBLE_FORMAT);
		return Double.valueOf(df.format(value));
	}

	public static double roundDoubleDp0(double value) {
		DecimalFormat df = new DecimalFormat(ZERO_DEC_DOUBLE_FORMAT);
		return Double.valueOf(df.format(value));
	}

	public static Date calculateAuditDate(int year, int season) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		int month;
		int day;
		switch (season) {
		case 1:
			month = 3;
			day = 31;
			break;
		case 2:
			month = 6;
			day = 30;
			break;
		case 3:
			month = 9;
			day = 30;
			break;
		case 4:
			month = 12;
			day = 31;
			break;
		default:
			month = 12;
			day = 31;
		}
		cal.set(year, month - 1, day, 0, 0, 0);
		return cal.getTime();
	}

	/*
	 * format the current date as a string of '20180101' format
	 */
	public static String todayDateString() {
		Calendar cal = Calendar.getInstance();
		return format.format(cal.getTime());
	}

	public static Date todayWithoutTime() {
		Calendar cal = Calendar.getInstance();
		try {
			return format.parse(format.format(cal.getTime()));
		} catch (ParseException e) {
			return null;
		}
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
	 * parse a date string of '107/01/01' format
	 */
	public static Optional<Date> rocYearStringToDate(String date) {
		try {
			int rocYear = Integer.parseInt(date.substring(0, date.indexOf('/')));
			int wYear = 1911 + rocYear;
			String wDate = Integer.toString(wYear) + date.substring(date.indexOf('/'));
			return Optional.of(format2.parse(wDate));
		} catch (Exception ex) {
			return Optional.empty();
		}
	}

	/*
	 * parse a date string of '20180101' format
	 */
	public static Optional<Date> stringSimpleToDate(String date) {
		try {
			return Optional.of(format.parse(date));
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
	public static Pair<Date, Date> getFirstAndLastDayOfLastMonth() {
		Calendar cal = Calendar.getInstance();
		int currentYear = cal.get(Calendar.YEAR);
		int currentMonth = cal.get(Calendar.MONTH);
		// first day of this month
		cal.set(currentYear, currentMonth, 1);
		// last day of previous month
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date second = cal.getTime();
		// first day of previous month
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date first = cal.getTime();
		return Pair.with(first, second);
	}

	/*
	 * calculate the date strings for the first day of each month within past 6
	 * months period.
	 */
	public static List<String> calculateDateStringPastSixMonth() {
		List<String> result = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		int currentYear = cal.get(Calendar.YEAR);
		int currentMonth = cal.get(Calendar.MONTH);
		int day = 1;
		for (int i = 5; i >= 0; i--) {
			int month = currentMonth - i;
			int year = currentYear;
			if (month < 0) {
				month += 12;
				year--;
			}
			cal.set(year, month, day);
			result.add(format.format(cal.getTime()));
		}
		return result;
	}

	public static Date getNextNTradingDate(Date currentDate, int n, List<Date> dateList) {
		if (dateList == null || dateList.isEmpty())
			return null;
		int last_index = dateList.size() - 1;
		int current_index = dateList.indexOf(currentDate);
		if (current_index < 0)
			return null;
		int target_index = current_index + n;
		if (target_index > last_index)
			return null;
		return dateList.get(target_index);
	}

	public static String getNextNTradingDate(String currentDateStr, int n, List<Date> dateList) {
		Date currentDate = StockUtils.stringSimpleToDate(currentDateStr).get();
		Date targetDate = getNextNTradingDate(currentDate, n, dateList);
		if (targetDate == null)
			return null;
		else
			return StockUtils.dateToSimpleString(targetDate);
	}

	public static Date getLastTradingDateOfWeek(Date tradingDate, List<Date> tdList) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(tradingDate);
		// dayOfWeek starts from 1(Sunday)
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		// first set to Saturday(sometimes TWSE will open on Sat.)
		cal.add(Calendar.DAY_OF_WEEK, 7 - dayOfWeek);
		// logger.info(cal.getTime().toString());
		//
		for (int i = 0; i <= (7 - dayOfWeek); i++) {
			Date d = cal.getTime();
			// logger.info(d.toString());
			if (tdList.contains(d)) {
				return d;
			}
			cal.add(Calendar.DAY_OF_WEEK, -1);
		}
		return null;
	}

}
