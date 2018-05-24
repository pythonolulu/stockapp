package com.javatican.stock.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.jsoup.nodes.Document;

public class StockUtils {

	private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd");

	public static String todayDateString() {
		Calendar cal = Calendar.getInstance();
		return format.format(cal.getTime());
	}

	public static Optional<Date> stringToDate(String date) {
		try {
			return Optional.of(format2.parse(date));
		} catch (Exception ex) {
			return Optional.empty();
		}
	}
	public static String dateToSimpleString(Date date) {
		return format.format(date);
	}

	public static String dateToStringSeparatedBySlash(Date date) {
		return format2.format(date);
	}
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

	public static String removeCommaInNumber(String number) {
		return number.replace(",", "");
	}

}
