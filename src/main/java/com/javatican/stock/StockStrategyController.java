package com.javatican.stock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.javatican.stock.member.FormCommand;
import com.javatican.stock.model.RealtimeMarketInfo;
import com.javatican.stock.model.RealtimeMarketInfo.StockItemMarketInfo;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockPriceChange;
import com.javatican.stock.model.StockTradeByForeign;
import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.service.CallWarrantTradeSummaryService;
import com.javatican.stock.service.ChartService;
import com.javatican.stock.service.IndexChartService;
import com.javatican.stock.service.IndexStrategyService;
import com.javatican.stock.service.PutWarrantTradeSummaryService;
import com.javatican.stock.service.RealtimeQuoteService;
import com.javatican.stock.service.StockItemService;
import com.javatican.stock.service.StockService;
import com.javatican.stock.service.StrategyService;
import com.javatican.stock.service.StrategyService2;
import com.javatican.stock.service.StrategyService3;
import com.javatican.stock.service.StrategyService4;
import com.javatican.stock.service.StrategyService5;
import com.javatican.stock.util.ResponseMessage;
import com.javatican.stock.util.StockUtils;

@RestController
@RequestMapping("stock/*")
public class StockStrategyController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private StockService stockService;
	@Autowired
	private StockItemService stockItemService;
	@Autowired
	private ChartService chartService;
	@Autowired
	private IndexChartService indexChartService;
	@Autowired
	private CallWarrantTradeSummaryService callWarrantTradeSummaryService;
	@Autowired
	private PutWarrantTradeSummaryService putWarrantTradeSummaryService;
	@Autowired
	private StrategyService strategyService;
	@Autowired
	private StrategyService2 strategyService2;
	@Autowired
	private StrategyService3 strategyService3;
	@Autowired
	private StrategyService4 strategyService4;
	@Autowired
	private StrategyService5 strategyService5;
	@Autowired
	private IndexStrategyService indexStrategyService;
	@Autowired
	RealtimeQuoteService realtimeQuoteService;

	/*
	 * a. handler for calculating the top stocks traded by Trust for the specified
	 * trading date.
	 */
	@GetMapping("/topTradeByTrust")
	public ModelAndView getTopTradeByTrust(@RequestParam(value = "tradingDate", required = false) String dateString,
			@RequestParam(value = "selectCount", defaultValue = "30") int selectCount,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		Date date;
		if (dateString == null) {
			date = stockService.getLatestTradingDate();
		} else {
			date = StockUtils.stringSimpleToDate(dateString).get();
		}
		// show 10 data points for each stock item
		int dateLength = 10;
		// key: stockItem
		// value: a map with key of date string and value StockTradeByTrust object
		LinkedHashMap<StockItem, TreeMap<String, StockTradeByTrust>> dataMap = stockService
				.getTopStockItemTradeByTrust(date, dateLength, selectCount);
		// create stock charts
		chartService.createGraphs(dataMap.keySet(), force);
		// also return trading date list
		List<Date> dateList = stockService.getLatestNTradingDateDesc(dateLength);
		List<String> dList = dateList.stream().map(td -> StockUtils.dateToStringSeparatedBySlash(td))
				.collect(Collectors.toList());
		ModelAndView mav = new ModelAndView("stock/topTradeByTrust");
		// prepare K/D values
		LinkedHashMap<StockItem, TreeMap<String, StockItemData>> statsMap = stockService
				.getStockItemStatsData(new ArrayList<StockItem>(dataMap.keySet()), dateList);
		//
		mav.addObject("command", new FormCommand());
		mav.addObject("title", "投信买卖热门股 - " + StockUtils.dateToStringSeparatedBySlash(date));
		mav.addObject("tradingDate", date);
		mav.addObject("dateList", dList);
		mav.addObject("dataMap", dataMap);
		mav.addObject("statsMap", statsMap);
		// stockItems with call and put warrants
		mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}

	/*
	 * a. handler for calculating the top stocks traded by Trust for the specified
	 * trading date.
	 */
	@GetMapping("/topTradeByForeign")
	public ModelAndView getTopTradeByForeign(@RequestParam(value = "tradingDate", required = false) String dateString,
			@RequestParam(value = "selectCount", defaultValue = "30") int selectCount,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		Date date;
		if (dateString == null) {
			date = stockService.getLatestTradingDate();
		} else {
			date = StockUtils.stringSimpleToDate(dateString).get();
		}
		// show 10 data points for each stock item
		int dateLength = 10;
		// key: stockItem
		// value: a map with key of date string and value StockTradeByTrust object
		LinkedHashMap<StockItem, TreeMap<String, StockTradeByForeign>> dataMap = stockService
				.getTopStockItemTradeByForeign(date, dateLength, selectCount);
		// create stock charts
		chartService.createGraphs(dataMap.keySet(), force);
		// also return trading date list
		List<Date> dateList = stockService.getLatestNTradingDateDesc(dateLength);
		List<String> dList = dateList.stream().map(td -> StockUtils.dateToStringSeparatedBySlash(td))
				.collect(Collectors.toList());
		ModelAndView mav = new ModelAndView("stock/topTradeByForeign");
		// prepare K/D values
		LinkedHashMap<StockItem, TreeMap<String, StockItemData>> statsMap = stockService
				.getStockItemStatsData(new ArrayList<StockItem>(dataMap.keySet()), dateList);
		//
		mav.addObject("command", new FormCommand());
		mav.addObject("title", "外资买卖热门股 - " + StockUtils.dateToStringSeparatedBySlash(date));
		mav.addObject("tradingDate", date);
		mav.addObject("dateList", dList);
		mav.addObject("dataMap", dataMap);
		mav.addObject("statsMap", statsMap);
		// stockItems with call and put warrants
		mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}

	/*
	 * b. handler for calculating the top 50 stock performers for the specified
	 * trading date.
	 */
	@GetMapping("/top50")
	public ModelAndView top50(@RequestParam(value = "tradingDate", required = false) String dateString,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		int dateLength = 10;
		Date date;
		if (dateString == null) {
			date = stockService.getLatestTradingDate();
		} else {
			date = StockUtils.stringSimpleToDate(dateString).get();
		}
		ModelAndView mav;
		try {
			List<StockPriceChange> spcList = stockService.loadTop(date);
			List<Date> dateList = stockService.getLatestNTradingDateDesc(dateLength);
			List<String> dList = dateList.stream().map(td -> StockUtils.dateToStringSeparatedBySlash(td))
					.collect(Collectors.toList());
			mav = new ModelAndView("stock/top50");
			// prepare K/D values
			LinkedHashMap<StockItem, TreeMap<String, StockItemData>> statsMap = stockService.getStockItemStatsData(
					spcList.stream().map(spc -> spc.getStockItem()).collect(Collectors.toList()), dateList);
			// create stock charts
			chartService.createGraphs(statsMap.keySet(), force);
			//
			mav.addObject("tradingDate", date);
			mav.addObject("title", "涨幅前50名 - " + StockUtils.dateToStringSeparatedBySlash(date));
			mav.addObject("dateList", dList);
			mav.addObject("spcList", spcList);
			mav.addObject("statsMap", statsMap);
			// stockItems with call and put warrants
			mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
			mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		} catch (StockException e) {
			mav = new ModelAndView("stock/error");
		}
		return mav;
	}

	/*
	 * c. handler for calculating the bottom 50 stock performers for the specified
	 * trading date.
	 */
	@GetMapping("/bottom50")
	public ModelAndView bottom50(@RequestParam(value = "tradingDate", required = false) String dateString,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		int dateLength = 10;
		Date date;
		if (dateString == null) {
			date = stockService.getLatestTradingDate();
		} else {
			date = StockUtils.stringSimpleToDate(dateString).get();
		}
		ModelAndView mav;
		try {
			List<StockPriceChange> spcList = stockService.loadBottom(date);

			List<Date> dateList = stockService.getLatestNTradingDateDesc(dateLength);
			List<String> dList = dateList.stream().map(td -> StockUtils.dateToStringSeparatedBySlash(td))
					.collect(Collectors.toList());
			//
			mav = new ModelAndView("stock/bottom50");
			// prepare K/D values
			LinkedHashMap<StockItem, TreeMap<String, StockItemData>> statsMap = stockService.getStockItemStatsData(
					spcList.stream().map(spc -> spc.getStockItem()).collect(Collectors.toList()), dateList);
			// create stock charts
			chartService.createGraphs(statsMap.keySet(), force);
			//
			mav.addObject("tradingDate", date);
			mav.addObject("title", "跌幅前50名 - " + StockUtils.dateToStringSeparatedBySlash(date));
			mav.addObject("dateList", dList);
			mav.addObject("spcList", spcList);
			mav.addObject("statsMap", statsMap);
			// stockItems with call and put warrants
			mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
			mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		} catch (StockException e) {
			mav = new ModelAndView("stock/error");
		}
		return mav;
	}

	// @GetMapping("/{stockSymbol}/getChart")
	// public Resource getChart(@PathVariable String stockSymbol) {
	// chartService.createGraph(stockSymbol);
	// return new ServletContextResource(servletContext,
	// "/stock/imgs/"+stockSymbol+".png");
	// }
	@GetMapping("/{stockSymbol}/getChart")
	public ModelAndView getChart(@PathVariable String stockSymbol,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		chartService.createGraph(stockSymbol, force);
		return new ModelAndView("redirect:" + "/stock/imgs/" + stockSymbol + ".png");
	}

	@GetMapping("/updateAllCharts")
	public ResponseMessage updateAllCharts(@RequestParam(value = "force", defaultValue = "false") boolean force,
			HttpServletRequest request) {

		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			List<StockItem> siList = stockItemService.findAllStockItems();
			chartService.createGraphs(siList, force);
			mes.setCategory("Success");
			mes.setText("All stock graphs have been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Stock graphs fail to be updated.");
		}
		return mes;
	}

	@GetMapping("/{stockSymbol}/getStrategyChart")
	public ModelAndView getStrategyChart(@PathVariable String stockSymbol,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		chartService.createGraph(stockSymbol, force);
		return new ModelAndView("redirect:" + "/stock/imgs/strategy/" + stockSymbol + ".png");
	}

	@GetMapping("/getIndexChart")
	public ModelAndView getIndexChart(@RequestParam(value = "force", defaultValue = "false") boolean force) {
		String latestTradingDateString = StockUtils.dateToSimpleString(stockService.getLatestTradingDate());
		indexChartService.createGraph(force, latestTradingDateString);
		return new ModelAndView("redirect:" + "/stock/imgs/index/index_" + latestTradingDateString + ".png");
	}

	/*
	 * Note: To use the Call Warrant Select strategy 1, need to call this 'prepare'
	 * method first to update the stats data. And then call
	 * callWarrantSelectStrategy1() method to get the top list. The same for put
	 * warrant select strategy 1.
	 */
	@GetMapping("/prepareCallWarrantSelectStrategy1")
	public ResponseMessage prepareCallWarrantSelectStrategy1(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			strategyService.prepareRawStatsDataForCallW(1);
			strategyService.prepareRawStatsDataForCallW(3);
			strategyService.prepareRawStatsDataForCallW(5);
			mes.setCategory("Success");
			mes.setText("Call warrant select strategy 1 data has been prepared.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Call warrant select strategy 1 data failed to be prepared.");
		}
		return mes;
	}

	@GetMapping("/preparePutWarrantSelectStrategy1")
	public ResponseMessage preparePutWarrantSelectStrategy1(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			strategyService.prepareRawStatsDataForPutW(1);
			strategyService.prepareRawStatsDataForPutW(3);
			strategyService.prepareRawStatsDataForPutW(5);
			mes.setCategory("Success");
			mes.setText("Put warrant select strategy 1 data has been prepared.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Put warrant select strategy 1 data failed to be prepared.");
		}
		return mes;
	}

	/*
	 * Buy a call warrant(with the largest trade value for the specific target
	 * stock) on each trading date and hold for a 'holdPeriod' day period, calculate
	 * the probability of going up and sort the stock targets based on this
	 * probability.
	 * 
	 * Parameters: holdPeriod: the period of each warrant item to hold;
	 * dataDatePeriod: how many days of data to calculate the accumulated
	 * performance selectCount: how many in the top list to output force: whether to
	 * force regenerating the graph charts
	 */
	@GetMapping("/callWarrantSelectStrategy1")
	public ModelAndView callWarrantSelectStrategy1(
			@RequestParam(value = "holdPeriod", defaultValue = "3") int holdPeriod,
			@RequestParam(value = "dataDatePeriod", defaultValue = "120") int dataDatePeriod,
			@RequestParam(value = "selectCount", defaultValue = "50") int selectCount,
			@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote) {
		Date date = stockService.getLatestTradingDate();
		String dateString = StockUtils.dateToSimpleString(date);
		ModelAndView mav;
		// statsMap: key is the symbol, value is a tree map with key of date string and
		// value of double(warrant price up percentage)
		Map<String, List<Number>> statsMap = strategyService.getStatsDataCallW(dateString, holdPeriod, dataDatePeriod);
		if (statsMap != null) {
			Map<String, StockItem> siMap = callWarrantTradeSummaryService.getStockItemsWithCallWarrant();
			mav = new ModelAndView("stock/callWarrantSelectStrategy1");
			//
			LinkedHashMap<String, List<Number>> resultMap = new LinkedHashMap<>();
			statsMap.entrySet().stream().sorted((e1, e2) -> {
				double acc_percent_1 = e1.getValue().get(2).doubleValue();
				double acc_percent_2 = e2.getValue().get(2).doubleValue();
				return (acc_percent_1 > acc_percent_2) ? -1 : 1;
			}).limit(selectCount).forEach(e -> resultMap.put(e.getKey(), e.getValue()));

			resultMap.entrySet().stream().forEach(entry -> {
				logger.info("Symbol:" + entry.getKey() + "(" + siMap.get(entry.getKey()).getName() + "), data count:"
						+ entry.getValue().get(0) + ", up ratio:" + entry.getValue().get(1)
						+ ", accumulated percentage:" + entry.getValue().get(2));
			});

			// create stock charts
			chartService.createGraphs2(resultMap.keySet(), force);
			//
			Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
			if (realtimeQuote) {
				RealtimeMarketInfo mri;
				try {
					mri = realtimeQuoteService.getInfo(resultMap.keySet());
					for (StockItemMarketInfo simi : mri.getMsgArray()) {
						realtimeMap.put(simi.getC(), simi);
					}
				} catch (StockException e) {
					logger.warn("Error getting realtime quote!");
					e.printStackTrace();
				}
			}
			//
			mav.addObject("command", new FormCommand());
			mav.addObject("realtimeMap", realtimeMap);
			mav.addObject("resultMap", resultMap);
			mav.addObject("siMap", siMap);
			mav.addObject("title", "認購權證累计涨幅 - " + StockUtils.dateToStringSeparatedBySlash(date));
			mav.addObject("tradingDate", date);
			mav.addObject("dataDatePeriod", dataDatePeriod);
			mav.addObject("selectCount", selectCount);
			mav.addObject("holdPeriod", holdPeriod);
			// stockItems with call and put warrants
			// mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
			mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		} else {
			mav = new ModelAndView("stock/error");
		}
		return mav;
	}

	/*
	 * Buy a put warrant(with the largest trade value for the specific target stock)
	 * on each trading date and hold for a 'holdPeriod' day period, calculate the
	 * probability of going up and sort the stock targets based on this probability.
	 * 
	 * Parameters: holdPeriod: the period of each warrant item to hold;
	 * dataDatePeriod: how many days of data to calculate the accumulated
	 * performance selectCount: how many in the top list to output force: whether to
	 * force regenerating the graph charts
	 */
	@GetMapping("/putWarrantSelectStrategy1")
	public ModelAndView putWarrantSelectStrategy1(
			@RequestParam(value = "holdPeriod", defaultValue = "3") int holdPeriod,
			@RequestParam(value = "dataDatePeriod", defaultValue = "120") int dataDatePeriod,
			@RequestParam(value = "selectCount", defaultValue = "50") int selectCount,
			@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote) {
		Date date = stockService.getLatestTradingDate();
		String dateString = StockUtils.dateToSimpleString(date);
		ModelAndView mav;
		Map<String, List<Number>> statsMap = strategyService.getStatsDataPutW(dateString, holdPeriod, dataDatePeriod);
		if (statsMap != null) {
			Map<String, StockItem> siMap = putWarrantTradeSummaryService.getStockItemsWithPutWarrant();
			mav = new ModelAndView("stock/putWarrantSelectStrategy1");
			//
			LinkedHashMap<String, List<Number>> resultMap = new LinkedHashMap<>();
			statsMap.entrySet().stream().sorted((e1, e2) -> {
				double acc_percent_1 = e1.getValue().get(2).doubleValue();
				double acc_percent_2 = e2.getValue().get(2).doubleValue();
				return (acc_percent_1 > acc_percent_2) ? -1 : 1;
			}).limit(selectCount).forEach(e -> resultMap.put(e.getKey(), e.getValue()));

			resultMap.entrySet().stream().forEach(entry -> {
				logger.info("Symbol:" + entry.getKey() + "(" + siMap.get(entry.getKey()).getName() + "), data count:"
						+ entry.getValue().get(0) + ", up ratio:" + entry.getValue().get(1)
						+ ", accumulated percentage:" + entry.getValue().get(2));
			});

			// create stock charts
			chartService.createGraphs2(resultMap.keySet(), force);
			//
			Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
			if (realtimeQuote) {
				RealtimeMarketInfo mri;
				try {
					mri = realtimeQuoteService.getInfo(resultMap.keySet());
					for (StockItemMarketInfo simi : mri.getMsgArray()) {
						realtimeMap.put(simi.getC(), simi);
					}
				} catch (StockException e) {
					logger.warn("Error getting realtime quote!");
					e.printStackTrace();
				}
			}
			//
			mav.addObject("command", new FormCommand());
			mav.addObject("realtimeMap", realtimeMap);
			mav.addObject("resultMap", resultMap);
			mav.addObject("siMap", siMap);
			mav.addObject("title", "認售權證累计涨幅 - " + StockUtils.dateToStringSeparatedBySlash(date));
			mav.addObject("tradingDate", date);
			mav.addObject("dataDatePeriod", dataDatePeriod);
			mav.addObject("selectCount", selectCount);
			mav.addObject("holdPeriod", holdPeriod);
			// stockItems with put and put warrants
			mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
			// mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		} else {
			mav = new ModelAndView("stock/error");
		}
		return mav;
	}

	@GetMapping("/getRealtimeQuote")
	private ResponseMessage getRealtimeQuote(@RequestParam(value = "symbols") String symbols,
			HttpServletRequest request) {
		List<String> symbolList = new ArrayList<>();
		for (String symbol : symbols.split(",")) {
			symbolList.add(symbol);
		}
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			RealtimeMarketInfo mri = realtimeQuoteService.getInfo(symbolList);
			for (StockItemMarketInfo simi : mri.getMsgArray()) {
				logger.info(simi.toString());
			}
			mes.setCategory("Success");
			mes.setText("Realtime quote is successfully received.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Realtime quote fails to receive.");
		}
		return mes;

	}

	@GetMapping("/aboveSma20SelectStrategy2")
	public ModelAndView aboveSma20SelectStrategy2(@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote,
			@RequestParam(value = "allData", defaultValue = "false") boolean allData) {
		// ryan add 2019/5/26
		String type = allData ? "20A_all" : "20A";
		Date date = stockService.getLatestTradingDate();
		String dateString = StockUtils.dateToSimpleString(date);
		// ryan add 2019/5/26
		// if allData==true, select all symbols, otherwise, get all the stockItem with
		// call warrants
		Map<String, StockItem> siMap = allData ? stockItemService.findAllStockItemsAsMap()
				: callWarrantTradeSummaryService.getStockItemsWithCallWarrant();
		//
		ModelAndView mav = new ModelAndView("stock/aboveSma20SelectStrategy2");

		Map<String, List<Number>> statsMap = strategyService2.getStatsData(dateString, type);
		LinkedHashMap<String, List<Number>> resultMap = new LinkedHashMap<>();

		statsMap.entrySet().stream()
				.sorted((e1, e2) -> e1.getValue().get(0).intValue() > e2.getValue().get(0).intValue() ? -1
						: (e1.getValue().get(0).intValue() < e2.getValue().get(0).intValue() ? 1 : 0))
				.forEach(e -> resultMap.put(e.getKey(), e.getValue()));

		// create stock charts
		chartService.createGraphs2(resultMap.keySet(), force);
		Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
		if (realtimeQuote) {
			RealtimeMarketInfo mri;
			try {
				mri = realtimeQuoteService.getInfo(resultMap.keySet());
				for (StockItemMarketInfo simi : mri.getMsgArray()) {
					realtimeMap.put(simi.getC(), simi);
				}
			} catch (StockException e) {
				logger.warn("Error getting realtime quote!");
				e.printStackTrace();
			}
		}
		//
		mav.addObject("command", new FormCommand());
		mav.addObject("realtimeMap", realtimeMap);
		mav.addObject("title", "股价在20日均线之上 - " + StockUtils.dateToStringSeparatedBySlash(date));
		mav.addObject("tradingDate", date);
		mav.addObject("resultMap", resultMap);
		mav.addObject("siMap", siMap);
		// stockItems with call and put warrants
		mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}

	@GetMapping("/belowSma20SelectStrategy2")
	public ModelAndView belowSma20SelectStrategy2(@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote,
			@RequestParam(value = "allData", defaultValue = "false") boolean allData) {
		// ryan add 2019/5/26
		String type = allData ? "20B_all" : "20B";

		Date date = stockService.getLatestTradingDate();
		String dateString = StockUtils.dateToSimpleString(date);
		// ryan add 2019/5/26
		// if allData==true, select all symbols, otherwise, get all the stockItem with
		// call warrants
		Map<String, StockItem> siMap = allData ? stockItemService.findAllStockItemsAsMap()
				: callWarrantTradeSummaryService.getStockItemsWithCallWarrant();
		//
		ModelAndView mav = new ModelAndView("stock/belowSma20SelectStrategy2");

		Map<String, List<Number>> statsMap = strategyService2.getStatsData(dateString, type);
		LinkedHashMap<String, List<Number>> resultMap = new LinkedHashMap<>();

		statsMap.entrySet().stream()
				.sorted((e1, e2) -> e1.getValue().get(0).intValue() < e2.getValue().get(0).intValue() ? -1
						: (e1.getValue().get(0).intValue() > e2.getValue().get(0).intValue() ? 1 : 0))
				.forEach(e -> resultMap.put(e.getKey(), e.getValue()));

		// create stock charts
		chartService.createGraphs2(resultMap.keySet(), force);
		Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
		if (realtimeQuote) {
			RealtimeMarketInfo mri;
			try {
				mri = realtimeQuoteService.getInfo(resultMap.keySet());
				for (StockItemMarketInfo simi : mri.getMsgArray()) {
					realtimeMap.put(simi.getC(), simi);
				}
			} catch (StockException e) {
				logger.warn("Error getting realtime quote!");
				e.printStackTrace();
			}
		}
		//
		mav.addObject("command", new FormCommand());
		mav.addObject("realtimeMap", realtimeMap);
		mav.addObject("title", "股价在20日均线之下 - " + StockUtils.dateToStringSeparatedBySlash(date));
		mav.addObject("tradingDate", date);
		mav.addObject("resultMap", resultMap);
		mav.addObject("siMap", siMap);
		// stockItems with call and put warrants
		mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}

	@GetMapping("/aboveSma60SelectStrategy2")
	public ModelAndView aboveSma60SelectStrategy2(@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote,
			@RequestParam(value = "allData", defaultValue = "false") boolean allData) {
		// ryan add 2019/5/26
		String type = allData ? "60A_all" : "60A";
		Date date = stockService.getLatestTradingDate();
		String dateString = StockUtils.dateToSimpleString(date);
		// ryan add 2019/5/26
		// if allData==true, select all symbols, otherwise, get all the stockItem with
		// call warrants
		Map<String, StockItem> siMap = allData ? stockItemService.findAllStockItemsAsMap()
				: callWarrantTradeSummaryService.getStockItemsWithCallWarrant();
		//
		ModelAndView mav = new ModelAndView("stock/aboveSma60SelectStrategy2");

		Map<String, List<Number>> statsMap = strategyService2.getStatsData(dateString, type);
		LinkedHashMap<String, List<Number>> resultMap = new LinkedHashMap<>();

		statsMap.entrySet().stream()
				.sorted((e1, e2) -> e1.getValue().get(0).intValue() > e2.getValue().get(0).intValue() ? -1
						: (e1.getValue().get(0).intValue() < e2.getValue().get(0).intValue() ? 1 : 0))
				.forEach(e -> resultMap.put(e.getKey(), e.getValue()));

		// create stock charts
		chartService.createGraphs2(resultMap.keySet(), force);
		Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
		if (realtimeQuote) {
			RealtimeMarketInfo mri;
			try {
				mri = realtimeQuoteService.getInfo(resultMap.keySet());
				for (StockItemMarketInfo simi : mri.getMsgArray()) {
					realtimeMap.put(simi.getC(), simi);
				}
			} catch (StockException e) {
				logger.warn("Error getting realtime quote!");
				e.printStackTrace();
			}
		}
		//
		mav.addObject("command", new FormCommand());
		mav.addObject("realtimeMap", realtimeMap);
		mav.addObject("title", "股价在60日均线之上 - " + StockUtils.dateToStringSeparatedBySlash(date));
		mav.addObject("tradingDate", date);
		mav.addObject("resultMap", resultMap);
		mav.addObject("siMap", siMap);
		// stockItems with call and put warrants
		mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}

	@GetMapping("/belowSma60SelectStrategy2")
	public ModelAndView belowSma60SelectStrategy2(@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote,
			@RequestParam(value = "allData", defaultValue = "false") boolean allData) {
		// ryan add 2019/5/26
		String type = allData ? "60B_all" : "60B";
		Date date = stockService.getLatestTradingDate();
		String dateString = StockUtils.dateToSimpleString(date);
		// ryan add 2019/5/26
		// if allData==true, select all symbols, otherwise, get all the stockItem with
		// call warrants
		Map<String, StockItem> siMap = allData ? stockItemService.findAllStockItemsAsMap()
				: callWarrantTradeSummaryService.getStockItemsWithCallWarrant();
		//
		ModelAndView mav = new ModelAndView("stock/belowSma60SelectStrategy2");

		Map<String, List<Number>> statsMap = strategyService2.getStatsData(dateString, type);
		LinkedHashMap<String, List<Number>> resultMap = new LinkedHashMap<>();

		statsMap.entrySet().stream()
				.sorted((e1, e2) -> e1.getValue().get(0).intValue() < e2.getValue().get(0).intValue() ? -1
						: (e1.getValue().get(0).intValue() > e2.getValue().get(0).intValue() ? 1 : 0))
				.forEach(e -> resultMap.put(e.getKey(), e.getValue()));

		// create stock charts
		chartService.createGraphs2(resultMap.keySet(), force);
		Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
		if (realtimeQuote) {
			RealtimeMarketInfo mri;
			try {
				mri = realtimeQuoteService.getInfo(resultMap.keySet());
				for (StockItemMarketInfo simi : mri.getMsgArray()) {
					realtimeMap.put(simi.getC(), simi);
				}
			} catch (StockException e) {
				logger.warn("Error getting realtime quote!");
				e.printStackTrace();
			}
		}
		//
		mav.addObject("command", new FormCommand());
		mav.addObject("realtimeMap", realtimeMap);
		mav.addObject("title", "股价在60日均线之下 - " + StockUtils.dateToStringSeparatedBySlash(date));
		mav.addObject("tradingDate", date);
		mav.addObject("resultMap", resultMap);
		mav.addObject("siMap", siMap);
		// stockItems with call and put warrants
		mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}

	@GetMapping("/prepareSmaSelectStrategy2")
	public ResponseMessage prepareSmaSelectStrategy2(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			strategyService2.prepareRawStatsData();
			mes.setCategory("Success");
			mes.setText("Sma select strategy 2 stats data has been prepared.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Sma select strategy 2 stats data fails to be prepared.");
		}
		return mes;
	}

	@GetMapping("/preparePriceBreakUpSelectStrategy3")
	public ResponseMessage preparePriceBreakUpSelectStrategy3(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			strategyService3.prepareRawStatsData();
			mes.setCategory("Success");
			mes.setText("Price break up select strategy 3 stats data has been prepared.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Price break up select strategy 3 stats data fails to be prepared.");
		}
		return mes;
	}

	@GetMapping("/preparePriceBreakUpSelectStrategy4")
	public ResponseMessage preparePriceBreakUpSelectStrategy4(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			strategyService4.prepareRawStatsData();
			mes.setCategory("Success");
			mes.setText("Price break up select strategy 4 stats data has been prepared.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Price break up select strategy 4 stats data fails to be prepared.");
		}
		return mes;
	}

	@GetMapping("/priceBreakUpSelectStrategy3")
	public ModelAndView priceBreakUpSelectStrategy3a(
			@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote,
			@RequestParam(value = "selectCount", defaultValue = "50") int selectCount,
			@RequestParam(value = "tradingDate", required = false) String dateString) {
		Date date = null;
		if (dateString == null) {
			date = stockService.getLatestTradingDate();
			dateString = StockUtils.dateToSimpleString(date);
		} else {
			date = StockUtils.stringSimpleToDate(dateString).get();
		}
		// get all the stockItem with call warrants
		Map<String, StockItem> siMap = callWarrantTradeSummaryService.getStockItemsWithCallWarrant();
		ModelAndView mav = new ModelAndView("stock/priceBreakUpSelectStrategy3");
		Map<String, Double> statsMap = strategyService3.getStatsData(dateString);
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<>();
		statsMap.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(selectCount)
				.forEach(e -> resultMap.put(e.getKey(), e.getValue()));
		// create stock charts
		chartService.createGraphs2(resultMap.keySet(), force);
		Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
		if (realtimeQuote) {
			RealtimeMarketInfo mri;
			try {
				mri = realtimeQuoteService.getInfo(resultMap.keySet());
				for (StockItemMarketInfo simi : mri.getMsgArray()) {
					realtimeMap.put(simi.getC(), simi);
				}
			} catch (StockException e) {
				logger.warn("Error getting realtime quote!");
				e.printStackTrace();
			}
		}
		//
		mav.addObject("command", new FormCommand());
		mav.addObject("realtimeMap", realtimeMap);
		mav.addObject("title", "股价向上突破 - " + StockUtils.dateToStringSeparatedBySlash(date));
		mav.addObject("tradingDate", date);
		mav.addObject("resultMap", resultMap);
		mav.addObject("siMap", siMap);
		// stockItems with call and put warrants
		// mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}

	@GetMapping("/priceBreakUpSelectStrategy4")
	public ModelAndView priceBreakUpSelectStrategy4(
			@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote,
			@RequestParam(value = "selectCount", defaultValue = "50") int selectCount,
			@RequestParam(value = "tradingDate", required = false) String dateString) {
		Date date = null;
		if (dateString == null) {
			date = stockService.getLatestTradingDate();
			dateString = StockUtils.dateToSimpleString(date);
		} else {
			date = StockUtils.stringSimpleToDate(dateString).get();
		}
		// get all the stockItem with call warrants
		Map<String, StockItem> siMap = callWarrantTradeSummaryService.getStockItemsWithCallWarrant();
		ModelAndView mav = new ModelAndView("stock/priceBreakUpSelectStrategy4");
		Map<String, Integer> statsMap = strategyService4.getStatsData(dateString);
		LinkedHashMap<String, Integer> resultMap = new LinkedHashMap<>();
		statsMap.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(selectCount)
				.forEach(e -> resultMap.put(e.getKey(), e.getValue()));
		// create stock charts
		chartService.createGraphs2(resultMap.keySet(), force);
		Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
		if (realtimeQuote) {
			RealtimeMarketInfo mri;
			try {
				mri = realtimeQuoteService.getInfo(resultMap.keySet());
				for (StockItemMarketInfo simi : mri.getMsgArray()) {
					realtimeMap.put(simi.getC(), simi);
				}
			} catch (StockException e) {
				logger.warn("Error getting realtime quote!");
				e.printStackTrace();
			}
		}
		//
		mav.addObject("command", new FormCommand());
		mav.addObject("realtimeMap", realtimeMap);
		mav.addObject("title", "股价向上突破 - " + StockUtils.dateToStringSeparatedBySlash(date));
		mav.addObject("tradingDate", date);
		mav.addObject("resultMap", resultMap);
		mav.addObject("siMap", siMap);
		// stockItems with call and put warrants
		// mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}

	@GetMapping("/prepareFinancialSelectStrategy5")
	public ResponseMessage prepareFinancialSelectStrategy5(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			strategyService5.prepareRawStatsData();
			mes.setCategory("Success");
			mes.setText("Financial select strategy 5 stats data has been prepared.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Financial select strategy 5 stats data fails to be prepared.");
		}
		return mes;
	}

	private Comparator<? super Entry<String, Map<String, List<Number>>>> getComparator(String sortKey) {
		if (sortKey.equals("PER")) {
			return (e1, e2) -> {
				double per_1 = e1.getValue().get("per").get(0).doubleValue();
				double per_2 = e2.getValue().get("per").get(0).doubleValue();
				return (per_1 > per_2) ? 1 : ((per_1 < per_2) ? -1 : 0);
			};
		} else if (sortKey.equals("PBR")) {
			return (e1, e2) -> {
				double pbr_1 = e1.getValue().get("pbr").get(0).doubleValue();
				double pbr_2 = e2.getValue().get("pbr").get(0).doubleValue();
				return (pbr_1 > pbr_2) ? 1 : ((pbr_1 < pbr_2) ? -1 : 0);
			};
		} else if (sortKey.equals("PM")) {
			return (e1, e2) -> {
				double pm_1 = e1.getValue().get("pm_avg").get(0).doubleValue();
				double pm_2 = e2.getValue().get("pm_avg").get(0).doubleValue();
				return (pm_1 > pm_2) ? -1 : ((pm_1 < pm_2) ? 1 : 0);
			};
		} else {
			return (e1, e2) -> {
				double fcfy_1 = e1.getValue().get("fcfy_avg").get(0).doubleValue();
				double fcfy_2 = e2.getValue().get("fcfy_avg").get(0).doubleValue();
				return (fcfy_1 > fcfy_2) ? -1 : ((fcfy_1 < fcfy_2) ? 1 : 0);
			};
		}
	}

	private Predicate<? super Entry<String, Map<String, List<Number>>>> getFilterPredicate(String filterKey) {
		if (filterKey.equals("PM")) {
			return e -> {
				List<Number> pmList = e.getValue().get("pm_i");
				double pm_prev = pmList.get(pmList.size() - 1).doubleValue();
				for (int i = pmList.size() - 2; i >= 0; i--) {
					double pm_current = pmList.get(i).doubleValue();
					if (pm_current <= pm_prev) {
						return false;
					}
					pm_prev = pm_current;
				}
				return true;
			};
		} else {
			return e -> {
				List<Number> fcfyList = e.getValue().get("fcfy_i");
				for (Number fcfy : fcfyList) {
					if (fcfy.doubleValue() < 0) {
						return false;
					}
				}
				return true;
			};
		}
	}

	@GetMapping("/financialSelectStrategy5")
	public ModelAndView financialSelectStrategy5(@RequestParam(value = "year", defaultValue = "2017") int year,
			@RequestParam(value = "period", defaultValue = "3") int period,
			@RequestParam(value = "selectCount", defaultValue = "50") int selectCount,
			@RequestParam(value = "sortKey", defaultValue = "PER") String sortKey,
			@RequestParam(value = "filterKey", defaultValue = "FCFY") String filterKey,
			@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote) {
		// sortKey can only be
		// 1. 'PER'(Price-Earning-Ratio),
		// 2. 'PBR'(Price-Book-Ratio),
		// 3. 'PM'(Profit-Margin),
		// 4. 'FCFY'(Free-Cash-Flow-Yield)

		// filterKey can only be
		// 1. 'FCFY'(Free-Cash-Flow-Yield) all positive
		// 2. 'PM' (Profit-Margin) increasing through years.
		ModelAndView mav;
		Map<String, Map<String, List<Number>>> statsMap = strategyService5.getStatsData(year, period);
		if (statsMap != null) {
			Map<String, StockItem> siMap = stockItemService.findAllStockItemsAsMap();
			mav = new ModelAndView("stock/financialSelectStrategy5");
			//
			LinkedHashMap<String, Map<String, List<Number>>> resultMap = new LinkedHashMap<>();
			// filter and sort
			// filter: first filter those per is positive and then apply the filterKey
			// setting
			statsMap.entrySet().stream().filter(e -> {
				if (e.getValue().get("per").get(0).doubleValue() <= 0) {
					return false;
				}
				return true;
			}).filter(getFilterPredicate(filterKey)).sorted(getComparator(sortKey)).limit(selectCount)
					.forEach(e -> resultMap.put(e.getKey(), e.getValue()));

			resultMap.entrySet().stream().forEach(entry -> {
				logger.info("Symbol:" + entry.getKey() + "(" + siMap.get(entry.getKey()).getName() + "), avg yield:"
						+ entry.getValue().get("fcfy_avg").get(0).doubleValue());
			});

			// create stock charts
			chartService.createGraphs2(resultMap.keySet(), force);
			//
			Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
			if (realtimeQuote) {
				RealtimeMarketInfo mri;
				try {
					mri = realtimeQuoteService.getInfo(resultMap.keySet());
					for (StockItemMarketInfo simi : mri.getMsgArray()) {
						realtimeMap.put(simi.getC(), simi);
					}
				} catch (StockException e) {
					logger.warn("Error getting realtime quote!");
					e.printStackTrace();
				}
			}
			//
			mav.addObject("command", new FormCommand());
			mav.addObject("title", "自由现金流报酬率");
			mav.addObject("realtimeMap", realtimeMap);
			mav.addObject("resultMap", resultMap);
			mav.addObject("siMap", siMap);
			mav.addObject("year", year);
			mav.addObject("period", period);
			mav.addObject("selectCount", selectCount);
			// stockItems with put and put warrants
			mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
			mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		} else {
			mav = new ModelAndView("stock/error");
		}
		return mav;
	}

	@GetMapping("/prepareSmaStatsData")
	public ResponseMessage prepareSmaStatsData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			indexStrategyService.prepareSmaStatsData();
			mes.setCategory("Success");
			mes.setText("Sma stats data has been prepared.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Sma stats data fails to be prepared.");
		}
		return mes;
	}

}
