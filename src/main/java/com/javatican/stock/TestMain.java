package com.javatican.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import org.jsoup.*;
import org.jsoup.Connection.*;
import org.jsoup.nodes.*;

public class TestMain {

	public static void main(String[] args) {
		CircularFifoQueue<Double> queue = 
				new CircularFifoQueue<>( Arrays.asList(ArrayUtils.toObject(new double[10])));
		List<Double> dList = Arrays.asList(new Double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0 });
		dList.stream().forEach(di -> {
			queue.add(di);
			double[] values = ArrayUtils.toPrimitive(queue.toArray(new Double[0]));
			for(double d: values) {
				System.out.println(d);
			}
			System.out.println("===");
			System.out.println("mean5=" + StatUtils.mean(values, 5, 5));
		});  
	}

	public static void main4(String[] args) {
		String text = "    173,287,382,620元";
		Pattern p1 = Pattern.compile("\\s*([\\d,-]+)元");
		Matcher m1 = p1.matcher(text);
		if (m1.find()) {
			System.out.println("First: " + m1.group(1));
			System.out.println("Everything Matched: " + m1.group(0));
		}
	}

	public static void main3(String[] args) {
		String text = "(上市公司) 鴻海";
		Pattern p1 = Pattern.compile("\\((\\S+)\\)\\s(\\S+)");
		Matcher m1 = p1.matcher(text);
		if (m1.find()) {
			System.out.println("First: " + m1.group(1));
			System.out.println("Last: " + m1.group(2));
			System.out.println("Everything Matched: " + m1.group(0));
		}
	}

	public static void main2(String[] args) {
		Connection.Response res;
		try {

			res = Jsoup.connect("http://mops.twse.com.tw/mops/web/t05st03").method(Method.GET).execute();
			Document doc = res.parse();
			String sessionId = res.cookie("jcsession");
			System.out.println(sessionId);
			Map<String, String> reqParams = new HashMap<>();
			reqParams.put("encodeURIComponent", "1");
			reqParams.put("step", "1");
			reqParams.put("firstin", "1"); // important param
			reqParams.put("off", "1");
			reqParams.put("keyword4", "");
			reqParams.put("code1", "");
			reqParams.put("TYPEK2", "");
			reqParams.put("checkbtn", "");
			reqParams.put("queryName", "co_id");
			reqParams.put("inpuType", "co_id");
			reqParams.put("TYPEK", "all");
			reqParams.put("co_id", "2317");
			Document doc2 = Jsoup.connect("http://mops.twse.com.tw/mops/web/ajax_t05st03")
					.cookie("jcsession", sessionId).data(reqParams).post();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
