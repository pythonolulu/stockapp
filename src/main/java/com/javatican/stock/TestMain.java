package com.javatican.stock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.jsoup.select.Elements;

import com.javatican.stock.model.StockPriceChange;
import com.javatican.stock.util.StockUtils;

public class TestMain {


	public static void main(String[] args) throws StockException{
		try {
			String dateString="20180613";
			File f = new File("./test.html");
			Document doc = Jsoup.parse(f,"UTF-8");
			//
			Elements tables = doc.select("body > div > table");
			Elements trs = tables.get(4).select("tbody > tr");
			StockPriceChange spc;
			Date tradingDate = StockUtils.stringSimpleToDate(dateString).get();
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				System.out.println("tds size="+tds.size());
				if(tds.size()!=16) {
					for(int i=0; i<tds.size(); i++) {
						System.out.println("tds["+i+"]="+tds.get(i).text()); 
					}
				}
				spc = new StockPriceChange();
				spc.setSymbol(tds.get(0).text());
				spc.setTradingDate(tradingDate);
				spc.setName(tds.get(1).text());
				double sign = 1.0;
				try {
					String signStr = tds.get(9).selectFirst("p").text();
					if (signStr.indexOf("-") >= 0)
						sign = -1.0;
				} catch (Exception ex) {
				}
				try {
					spc.setTradeVolume(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(2).text())));
					spc.setTransaction(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(3).text())));
					spc.setTradeValue(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(4).text())));
					spc.setOpen(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(5).text())));
					spc.setHigh(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(6).text())));
					spc.setLow(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(7).text())));
					spc.setClose(Double.valueOf(StockUtils.removeCommaInNumber(tds.get(8).text())));
					spc.setChange(sign * Double.valueOf(StockUtils.removeCommaInNumber(tds.get(10).text())));
				} catch (Exception ex) {
				}
				System.out.println(spc.toString());
			}
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}
	public static void main5(String[] args) {
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
