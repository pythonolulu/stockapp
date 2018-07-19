package com.javatican.stock;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockPriceChange;
import com.javatican.stock.util.StockUtils;

public class TestMain {
	public static void main(String[] args) throws IOException {
		Date today = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		System.out.println(dayOfWeek);
	}
	public static void main9(String[] args) throws IOException {
		final String TWSE_REALTIME_QUOTE_GET_URL = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=%s&json=1&delay=0&_=%s";

		StringBuilder sb = new StringBuilder();

		sb.append(String.format("tse_%s.tw|", "2317"));
		sb.append(String.format("tse_%s.tw|", "2330"));

		String strUrl = String.format(TWSE_REALTIME_QUOTE_GET_URL, sb.toString(), Long.toString(1531705877469L));

		// try (InputStream inStream = new URL(strUrl).openStream();) {
		// Document doc = Jsoup.parse(inStream, "UTF-8", strUrl);
		// System.out.println(doc.body());
		// }
		Document doc = Jsoup.connect(strUrl).get();
		System.out.println(doc.text());

	}

	public static void main8(String[] args) {
		// note: the return values of keySet() or entrySet() of TreeMap will keep the
		// order of the TreeMap（ascending key order)
		TreeMap<String, Double> upPercentMap = new TreeMap<>();
		TreeMap<String, Double> resultMap = new TreeMap<>();
		upPercentMap.put("20180609", 19.8);
		upPercentMap.put("20180702", 20.3);
		upPercentMap.put("20180703", 20.5);
		upPercentMap.put("20180710", 20.8);
		upPercentMap.put("20180701", 20.1);
		upPercentMap.put("20180705", 20.2);
		upPercentMap.put("20180406", 20.0);
		upPercentMap.put("20180704", 20.9);
		upPercentMap.put("20180707", 20.4);
		upPercentMap.put("20180708", 21.1);
		upPercentMap.put("20180711", 19.1);
		// below is not compiling , because collect(Collectors.toMap()) method will
		// return a Map object, can not be casted to a TreeMap
		//
		// resultMap = upPercentMap.entrySet().stream().skip(upPercentMap.size() - 7)
		// .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		upPercentMap.entrySet().stream().skip(upPercentMap.size() - 7)
				.forEach(e -> resultMap.put(e.getKey(), e.getValue()));
		resultMap.entrySet().stream().forEach(e -> System.out.println("key:" + e.getKey() + ",value:" + e.getValue()));
	}

	public static void main7(String[] args) {
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new MyChart();
			}
		});

	}

	public static class MyChart extends JFrame {
		private List<StockItemData> sidList;

		public MyChart() {
			this(false);
		}

		public MyChart(boolean offline) {
			String symbol = "6153";
			loadData(symbol);
			if (offline) {
				outputToPNG(symbol);
			} else {
				initUI(symbol);
				this.setLocation(0, 0);
				this.setVisible(true);
			}
		}

		private void outputToPNG(String symbol) {
			JFreeChart chart = createChart(symbol);
			try {
				ChartUtils.saveChartAsPNG(new File(String.format("%s_chart.png", symbol)), chart, 1200, 900);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void initUI(String symbol) {
			JFreeChart chart = createChart(symbol);
			ChartPanel chartPanel = new ChartPanel(chart);

			chartPanel.setPreferredSize(new java.awt.Dimension(1200, 900));
			chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
			chartPanel.setBackground(Color.white);
			chartPanel.setMouseZoomable(true);
			chartPanel.setMouseWheelEnabled(true);
			add(chartPanel, BorderLayout.CENTER);
			JFrame.setDefaultLookAndFeelDecorated(true);
			setTitle("Stock Analysis Chart : " + symbol);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setResizable(false);
			pack();
		}

		private void loadData(String symbol) {
			String fileStr = String.format("./download/stats/%s_stats.json", symbol);
			ObjectMapper objectMapper = new ObjectMapper();
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
			objectMapper.setDateFormat(df);
			try {
				this.sidList = objectMapper.readValue(new File(fileStr), new TypeReference<List<StockItemData>>() {
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private OHLCSeriesCollection createPriceDataset() {
			OHLCSeriesCollection priceDataset = new OHLCSeriesCollection();
			OHLCSeries ohlcSeries = new OHLCSeries("Price");
			// add data
			sidList.stream().forEach(sid -> ohlcSeries.add(new Day(sid.getTradingDate()), sid.getStockPrice().getOpen(),
					sid.getStockPrice().getHigh(), sid.getStockPrice().getLow(), sid.getStockPrice().getClose()));
			priceDataset.addSeries(ohlcSeries);
			return priceDataset;
		}

		private TimeSeriesCollection createVolumeDataset() {
			TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
			TimeSeries volumeSeries = new TimeSeries("Volume");
			// add data
			sidList.stream().forEach(sid -> volumeSeries.add(new Day(sid.getTradingDate()),
					(new Random().nextBoolean() ? 1 : -1) * sid.getStockPrice().getTradeVolume()));
			volumeDataset.addSeries(volumeSeries);
			return volumeDataset;
		}

		private TimeSeriesCollection createSMADataset() {
			TimeSeriesCollection smaDataset = new TimeSeriesCollection();
			TimeSeries sma60Series = new TimeSeries("SMA60");
			TimeSeries sma20Series = new TimeSeries("SMA20");
			TimeSeries sma10Series = new TimeSeries("SMA10");
			TimeSeries sma5Series = new TimeSeries("SMA5");
			// add data
			sidList.stream().forEach(sid -> {
				Date date = sid.getTradingDate();
				sma60Series.add(new Day(date), sid.getSma60());
				sma20Series.add(new Day(date), sid.getSma20());
				sma10Series.add(new Day(date), sid.getSma10());
				sma5Series.add(new Day(date), sid.getSma5());
			});
			// add data
			smaDataset.addSeries(sma60Series);
			smaDataset.addSeries(sma20Series);
			smaDataset.addSeries(sma10Series);
			smaDataset.addSeries(sma5Series);
			return smaDataset;
		}

		private TimeSeriesCollection createKDDataset() {
			TimeSeriesCollection kdDataset = new TimeSeriesCollection();
			TimeSeries kSeries = new TimeSeries("K9");
			TimeSeries dSeries = new TimeSeries("D9");
			// add data
			sidList.stream().forEach(sid -> {
				Date date = sid.getTradingDate();
				kSeries.add(new Day(date), sid.getK());
				dSeries.add(new Day(date), sid.getD());
			});
			// add data
			kdDataset.addSeries(kSeries);
			kdDataset.addSeries(dSeries);
			return kdDataset;
		}

		private JFreeChart createChart(String symbol) {
			OHLCSeriesCollection priceDataset = createPriceDataset();
			TimeSeriesCollection volumeDataset = createVolumeDataset();
			TimeSeriesCollection smaDataset = createSMADataset();
			TimeSeriesCollection kdDataset = createKDDataset();

			// Create candlestick chart priceAxis
			NumberAxis priceAxis = new NumberAxis("Price");
			priceAxis.setAutoRangeIncludesZero(false);
			// Create candlestick chart renderer
			CandlestickRenderer candlestickRenderer = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE,
					false, new HighLowItemLabelGenerator(new SimpleDateFormat("yy/MM/dd"), new DecimalFormat("0.00")));
			candlestickRenderer.setUpPaint(Color.red);
			candlestickRenderer.setDownPaint(Color.green);
			candlestickRenderer.setUseOutlinePaint(true);
			// Create candlestickSubplot
			XYPlot candlestickSubplot = new XYPlot(priceDataset, null, priceAxis, candlestickRenderer);
			candlestickSubplot.setBackgroundPaint(Color.white);
			// create SMA renderer
			XYLineAndShapeRenderer smaRenderer = new XYLineAndShapeRenderer();
			// add 2nd dataset (SMA)
			candlestickSubplot.setDataset(1, smaDataset);
			smaRenderer.setDefaultShapesVisible(false);
			smaRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
			smaRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
			smaRenderer.setSeriesStroke(2, new BasicStroke(1.0f));
			smaRenderer.setSeriesStroke(3, new BasicStroke(1.0f));
			candlestickSubplot.setRenderer(1, smaRenderer);
			// Create volume chart volumeAxis
			NumberAxis volumeAxis = new NumberAxis("Volume");
			volumeAxis.setAutoRangeIncludesZero(true);
			// Set to no decimal
			volumeAxis.setNumberFormatOverride(new DecimalFormat("###,###"));
			// Create volume chart renderer
			XYBarRenderer timeRenderer = new XYBarRenderer();
			timeRenderer.setShadowVisible(false);
			timeRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator("Date={1}, Vol={2}",
					new SimpleDateFormat("yy/MM/dd"), new DecimalFormat("###,###")));
			// Create volumeSubplot
			XYPlot volumeSubplot = new XYPlot(volumeDataset, null, volumeAxis, timeRenderer);
			volumeSubplot.setBackgroundPaint(Color.white);
			//
			// Create volume chart volumeAxis
			NumberAxis kdAxis = new NumberAxis("K/D");
			kdAxis.setAutoRangeIncludesZero(true);
			kdAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			// Create volume chart renderer
			XYLineAndShapeRenderer kdRenderer = new XYLineAndShapeRenderer();
			kdRenderer.setDefaultShapesVisible(false);
			kdRenderer.setSeriesStroke(0, new BasicStroke(1.0f));
			kdRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
			// Create kdSubplot
			XYPlot kdSubplot = new XYPlot(kdDataset, null, kdAxis, kdRenderer);
			kdSubplot.setBackgroundPaint(Color.white);
			//
			DateAxis dateAxis = new DateAxis("Date");
			dateAxis.setDateFormatOverride(new SimpleDateFormat("yy/MM/dd"));
			// Create mainPlot
			CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
			mainPlot.setGap(10.0);
			mainPlot.add(candlestickSubplot, 3);
			mainPlot.add(volumeSubplot, 1);
			mainPlot.add(kdSubplot, 1);
			mainPlot.setOrientation(PlotOrientation.VERTICAL);
			JFreeChart chart = new JFreeChart(symbol, JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
			// chart.removeLegend();

			return chart;
		}

	}

	public static void main6(String[] args) throws StockException {
		try {
			String dateString = "20180613";
			File f = new File("./test.html");
			Document doc = Jsoup.parse(f, "UTF-8");
			//
			Elements tables = doc.select("body > div > table");
			Elements trs = tables.get(4).select("tbody > tr");
			StockPriceChange spc;
			Date tradingDate = StockUtils.stringSimpleToDate(dateString).get();
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				System.out.println("tds size=" + tds.size());
				if (tds.size() != 16) {
					for (int i = 0; i < tds.size(); i++) {
						System.out.println("tds[" + i + "]=" + tds.get(i).text());
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
		CircularFifoQueue<Double> queue = new CircularFifoQueue<>(10);
		List<Double> dList = Arrays
				.asList(new Double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0 });
		dList.stream().forEach(di -> {
			queue.add(di);
			double[] values = ArrayUtils.toPrimitive(queue.toArray(new Double[0]));
			for (double d : values) {
				System.out.println(d);
			}
			System.out.println("===");
			if (values.length >= 5) {
				System.out.println("mean5=" + StatUtils.mean(values, values.length - 5, 5));
			}
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
