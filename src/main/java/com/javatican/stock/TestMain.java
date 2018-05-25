package com.javatican.stock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.*;
import org.jsoup.Connection.*;
import org.jsoup.nodes.*;

public class TestMain {

	public static void main(String[] args) {
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
