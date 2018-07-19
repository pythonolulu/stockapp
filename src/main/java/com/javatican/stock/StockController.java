package com.javatican.stock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.assertj.core.internal.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.javatican.stock.model.RealtimeMarketInfo;
import com.javatican.stock.model.RealtimeMarketInfo.StockItemMarketInfo;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockPriceChange;
import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.service.CallWarrantTradeSummaryService;
import com.javatican.stock.service.ChartService;
import com.javatican.stock.service.DealerTradeSummaryService;
import com.javatican.stock.service.MarginService;
import com.javatican.stock.service.PutWarrantTradeSummaryService;
import com.javatican.stock.service.RealtimeQuoteService;
import com.javatican.stock.service.StockItemService;
import com.javatican.stock.service.StockService;
import com.javatican.stock.service.StockTradeByForeignService;
import com.javatican.stock.service.StockTradeByTrustService;
import com.javatican.stock.service.StrategyService;
import com.javatican.stock.util.ResponseMessage;
import com.javatican.stock.util.StockUtils;

@RestController
@RequestMapping("stock/*")
public class StockController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private StockService stockService;
	@Autowired
	private ChartService chartService;
	@Autowired
	private StockItemService stockItemService;
	@Autowired
	private StockTradeByTrustService stockTradeByTrustService;
	@Autowired
	private StockTradeByForeignService stockTradeByForeignService;
	@Autowired
	private CallWarrantTradeSummaryService callWarrantTradeSummaryService;
	@Autowired
	private PutWarrantTradeSummaryService putWarrantTradeSummaryService;
	@Autowired
	private DealerTradeSummaryService dealerTradeSummaryService;
	@Autowired
	private MarginService marginService;
	@Autowired
	private StrategyService strategyService;
	@Autowired
	RealtimeQuoteService realtimeQuoteService;

	// @Autowired
	// private ServletContext servletContext;

	/*
	 * 1. handler for downloading and saving any new trading date and trading values
	 * data
	 */
	@GetMapping("/updateData")
	public ResponseMessage updateData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockService.updateTradingDateAndValue();
			mes.setCategory("Success");
			mes.setText("Trading date and value information has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Trading date and value information fails to be updated.");
		}
		return mes;
	}

	/*
	 * 2.1. handler for downloading and saving any new stock trading data by Trust
	 */
	@GetMapping("/updateTrustData")
	public ResponseMessage updateTrustData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockTradeByTrustService.updateData();
			mes.setCategory("Success");
			mes.setText("Stock Trade Data by Trust have been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText(" Stock Trade Data by Trust fail to be updated.");
		}
		return mes;
	}

	/*
	 * 2.2. handler for downloading and saving any new stock trading data by Foreign
	 * Traders
	 */
	@GetMapping("/updateForeignData")
	public ResponseMessage updateForeignData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockTradeByForeignService.updateData();
			mes.setCategory("Success");
			mes.setText("Stock Trade Data by foreign traders have been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText(" Stock Trade Data by foreign traders fail to be updated.");
		}
		return mes;
	}

	/*
	 * 2.3. handler for downloading and saving top performers
	 */
	@GetMapping("/{tradingDate}/preparePerformers")
	public ResponseMessage prepareTopAndBottomPerformers(@PathVariable String tradingDate) {

		ResponseMessage mes = new ResponseMessage();
		try {
			stockService.preparePerformers(tradingDate, 50);
			mes.setCategory("Success");
			mes.setText("Stock price change for top/bottom 50 stocks have been saved.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Stock price change for top/bottom 50 stocks fail to be saved.");
		}
		return mes;
	}

	/*
	 * 3. handler for downloading and saving stock prices for any new stock items
	 */
	@GetMapping("/createStockPrices")
	public ResponseMessage createStockPrices() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.downloadAndSaveStockPrices();
			mes.setCategory("Success");
			mes.setText("download and save all stock prices.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Fails to download and save all stock prices.");
		}
		return mes;
	}

	/*
	 * 4. handler for calculating average price for last month for missing price
	 * field stock items.
	 */
	@GetMapping("/updateMissingPriceField")
	public ResponseMessage updateMissingStockItemPriceField() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.updateMissingStockItemPriceField();
			mes.setCategory("Success");
			mes.setText("Stock price fields have been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Stock price fields fail to be udpated.");
		}
		return mes;
	}

	/*
	 * 5. handler for updating price for all existing stock items
	 */
	@GetMapping("/updatePriceDataForAll")
	public ResponseMessage updatePriceDataForAll() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.updatePriceDataForAllImproved();
			mes.setCategory("Success");
			mes.setText("Price data for all stocks have been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Price data for all stocks fail to be updated.");
		}
		return mes;
	}

	@GetMapping("/{stockSymbol}/updatePriceData")
	private ResponseMessage updateStockPriceData(@PathVariable String stockSymbol) {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.updatePriceDataForSymbol(stockSymbol);
			mes.setCategory("Success");
			mes.setText("Trading data for stock " + stockSymbol + " has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Trading data for stock " + stockSymbol + " fails to be updated.");
		}
		return mes;
	}

	/*
	 * 6. handler for calculating K/D and SMA values for all existing stock items
	 */
	@GetMapping("/calculateAndSaveKDForAll")
	public ResponseMessage calculateAndSaveKDForAll() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.calculateAndSaveKDForAll();
			mes.setCategory("Success");
			mes.setText("Stats data has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Stats data fails to be updated.");
		}
		return mes;
	}

	@GetMapping("/{stockSymbol}/calculateAndSaveKD")
	private ResponseMessage calculateAndSaveKD(@PathVariable String stockSymbol) {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.calculateAndSaveKDForSymbol(stockSymbol);
			mes.setCategory("Success");
			mes.setText("Stats data for stock " + stockSymbol + " has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Stats data for stock " + stockSymbol + " fails to be updated.");
		}
		return mes;
	}

	/*
	 * 7. handler for download call warrants trade data.
	 */
	@GetMapping("/updateCallWarrantData")
	public ResponseMessage updateCallWarrantData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			callWarrantTradeSummaryService.updateData();
			mes.setCategory("Success");
			mes.setText("Call warrant trading data has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Call warrant trading data fails to be updated.");
		}
		return mes;

	}

	/*
	 * 8. handler for download put warrants trade data.
	 */
	@GetMapping("/updatePutWarrantData")
	public ResponseMessage updatePutWarrantData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			putWarrantTradeSummaryService.updateData();
			mes.setCategory("Success");
			mes.setText("Put warrant trading data has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Put warrant trading data fails to be updated.");
		}
		return mes;

	}

	/*
	 * 9. handler for download Dealer warrants hedge trade data.
	 */
	@GetMapping("/updateDealerData")
	public ResponseMessage updateDealerData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			dealerTradeSummaryService.updateData();
			mes.setCategory("Success");
			mes.setText("Dealer trading data has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Dealer trading data fails to be updated.");
		}
		return mes;

	}

	@GetMapping("/updateMarginData")
	public ResponseMessage updateMarginData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			marginService.updateData();
			mes.setCategory("Success");
			mes.setText("Margin and sbl data has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Margin and sbl data fails to be updated.");
		}
		return mes;

	}

	@GetMapping("/extractMarginData")
	private ResponseMessage extractMarginData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			marginService.extractMarginData();
			mes.setCategory("Success");
			mes.setText("Margin and sbl data has been extracted.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Margin and sbl data fails to be extracted.");
		}
		return mes;

	}

	/*
	 * only run at the beginning of new month.
	 */
	@GetMapping("/updatePriceFieldForAll")
	public ResponseMessage updateStockItemPriceFieldForAll() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.updateStockItemPriceFieldForAll();
			mes.setCategory("Success");
			mes.setText("Stock price fields for all stocks have been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Stock price fields for all stocks fail  to be udpated.");
		}
		return mes;
	}

	/*
	 * a. handler for calculating the top 30 stocks traded by Trust for the
	 * specified trading date.
	 */
	@GetMapping("/{tradingDate}/top30ByTrust")
	public ModelAndView getTop30ByTrust(@PathVariable String tradingDate,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		// show 10 data points for each stock item
		int dateLength = 10;
		Date date = StockUtils.stringSimpleToDate(tradingDate).get();
		// key: stockItem
		// value: a map with key of date string and value StockTradeByTrust object
		LinkedHashMap<StockItem, TreeMap<String, StockTradeByTrust>> dataMap = stockService
				.getTop30StockItemTradeByTrust(date, dateLength);
		// create stock charts
		chartService.createGraphs(dataMap.keySet(), force);
		// also return trading date list
		List<Date> dateList = stockService.getLatestNTradingDateDesc(dateLength);
		List<String> dList = dateList.stream().map(td -> StockUtils.dateToStringSeparatedBySlash(td))
				.collect(Collectors.toList());
		ModelAndView mav = new ModelAndView("stock/top30ByTrust");
		// prepare K/D values
		LinkedHashMap<StockItem, TreeMap<String, StockItemData>> statsMap = stockService
				.getStockItemStatsData(new ArrayList<StockItem>(dataMap.keySet()), dateList);
		//
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
	@GetMapping("/{tradingDate}/top50")
	public ModelAndView top50(@PathVariable String tradingDate,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		int dateLength = 10;
		Date date = StockUtils.stringSimpleToDate(tradingDate).get();
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
	@GetMapping("/{tradingDate}/bottom50")
	public ModelAndView bottom50(@PathVariable String tradingDate,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		int dateLength = 10;
		Date date = StockUtils.stringSimpleToDate(tradingDate).get();
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

	@GetMapping("/prepareDealerData")
	private ResponseMessage prepareDealerData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			dealerTradeSummaryService.prepareData();
			mes.setCategory("Success");
			mes.setText("Dealer trading data has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Dealer trading data fails to be updated.");
		}
		return mes;

	}

	//
	// @GetMapping("/prepareCallWarrantData")
	// private ResponseMessage prepareCallWarrantData() {
	// ResponseMessage mes = new ResponseMessage();
	// try {
	// callWarrantTradeSummaryService.prepareData();
	// mes.setCategory("Success");
	// mes.setText("Call warrant trading data has been updated.");
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// mes.setCategory("Fail");
	// mes.setText("Call warrant trading data fails to be updated.");
	// }
	// return mes;
	//
	// }

	// @GetMapping("/preparePutWarrantData")
	// private ResponseMessage preparePutWarrantData() {
	// ResponseMessage mes = new ResponseMessage();
	// try {
	// putWarrantTradeSummaryService.prepareData();
	// mes.setCategory("Success");
	// mes.setText("Put warrant trading data has been updated.");
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// mes.setCategory("Fail");
	// mes.setText("Put warrant trading data fails to be updated.");
	// }
	// return mes;
	//
	// }

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

	@GetMapping("/{stockSymbol}/getStrategyChart")
	public ModelAndView getStrategyChart(@PathVariable String stockSymbol,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		chartService.createGraph(stockSymbol, force);
		return new ModelAndView("redirect:" + "/stock/imgs/strategy/" + stockSymbol + ".png");
	}
	//
	// @GetMapping("/migrateData")
	// public ResponseMessage migrateData() {
	// ResponseMessage mes = new ResponseMessage();
	// try {
	// stockItemService.migrateData();
	// mes.setCategory("Success");
	// mes.setText("success");
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// mes.setCategory("Fail");
	// mes.setText("fail");
	// }
	// return mes;
	// }

	@GetMapping("/prepareDataForCallWarrantSelectStrategy1")
	public ResponseMessage prepareDataForCallWarrantSelectStrategy1() {
		ResponseMessage mes = new ResponseMessage();
		try {
			strategyService.prepareDataForCallWarrantSelectStrategy1(1);
			strategyService.prepareDataForCallWarrantSelectStrategy1(3);
			strategyService.prepareDataForCallWarrantSelectStrategy1(5);
			mes.setCategory("Success");
			mes.setText("Call warrant select strategy 1 data has been prepared.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Call warrant select strategy 1 data failed to be prepared.");
		}
		return mes;
	}

	@GetMapping("/prepareDataForPutWarrantSelectStrategy1")
	public ResponseMessage prepareDataForPutWarrantSelectStrategy1() {
		ResponseMessage mes = new ResponseMessage();
		try {
			strategyService.prepareDataForPutWarrantSelectStrategy1(1);
			strategyService.prepareDataForPutWarrantSelectStrategy1(3);
			strategyService.prepareDataForPutWarrantSelectStrategy1(5);
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
	 * probability. Parameters: holdPeriod: the period of each warrant item to hold;
	 * dataDatePeriod: how many days of data to calculate
	 * 
	 */
	@GetMapping("/callWarrantSelectStrategy1")
	public ModelAndView callWarrantSelectStrategy1(
			@RequestParam(value = "holdPeriod", defaultValue = "3") int holdPeriod,
			@RequestParam(value = "dataDatePeriod", defaultValue = "120") int dataDatePeriod,
			@RequestParam(value = "selectCount", defaultValue = "50") int selectCount,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		// int totalDatePeriod = (int) stockService.tradingDateCount();
		Date date = stockService.getLatestTradingDate();
		ModelAndView mav;
		// statsMap: key is the symbol, value is a tree map with key of date string and
		// value of double(warrant price up percentage)
		Map<String, TreeMap<String, Double>> statsMap = strategyService
				.getStatsDataForCallWarrantSelectStrategy1(holdPeriod);
		if (statsMap != null) {
			// all the stock items with call warrants(used for getting its item name)
			Map<String, StockItem> siMap = callWarrantTradeSummaryService.getStockItemsWithCallWarrant();
			// upProbabilityMap: store the probability of the warrant price going up
			// key is the symbol string and value is a double of probability
			Map<String, Double> upProbabilityMap = new HashMap<>();
			// upPercentAccMap: store the accumulated price up percentages
			// key: symbol string, value is a double of accumulated percentage
			Map<String, Double> upPercentAccMap = new HashMap<>();
			// dataCountMap: store the count of actual data points used for the calculation
			// for up probability and percent accumulation
			// key : symbol string, value is a long for the data count
			Map<String, Long> dataCountMap = new HashMap<>();
			for (String symbol : statsMap.keySet()) {
				// upPercentMap contains price up percentages of warrant on each trading date
				// for the stock symbol
				// key : date string, value: a double of up percentage
				TreeMap<String, Double> upPercentMap = statsMap.get(symbol);
				// filteredUpPercentMap: store filtered result of upPercentMap (only select the
				// latest 'dataDatePeriod' trading dates)
				final TreeMap<String, Double> filteredUpPercentMap;
				if (upPercentMap.size() > dataDatePeriod) {
					filteredUpPercentMap = new TreeMap<>();
					// below is not compiling because collect(Collectors.toMap()) method will
					// return a Map object, can not be casted to a TreeMap
					//
					// filteredUpPercentMap =
					// upPercentMap.entrySet().stream().skip(upPercentMap.size() - dataDatePeriod)
					// .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
					upPercentMap.entrySet().stream().skip(upPercentMap.size() - dataDatePeriod)
							.forEach(e -> filteredUpPercentMap.put(e.getKey(), e.getValue()));
				} else {
					filteredUpPercentMap = upPercentMap;
				}
				long size = filteredUpPercentMap.size();
				long up_size = filteredUpPercentMap.values().stream().filter(value -> value > 0.0).count();
				double acc_percent = filteredUpPercentMap.values().stream().reduce(0.0, (a, b) -> a + b);
				if (size == 0)
					continue;
				upProbabilityMap.put(symbol, StockUtils.roundDoubleDp4((double) up_size / size));
				upPercentAccMap.put(symbol, StockUtils.roundDoubleDp4(acc_percent));
				dataCountMap.put(symbol, size);

			}
			mav = new ModelAndView("stock/callWarrantSelectStrategy1");
			//
			LinkedHashMap<String, Double> resultMap = new LinkedHashMap<>();
			upPercentAccMap.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed())
					.limit(selectCount).forEach(e -> resultMap.put(e.getKey(), e.getValue()));

			resultMap.entrySet().stream().forEach(entry -> {
				logger.info("Symbol:" + entry.getKey() + "(" + siMap.get(entry.getKey()).getName()
						+ "), avg percentage:" + entry.getValue() + ", win percentage:"
						+ upProbabilityMap.get(entry.getKey()) + ", data count:" + dataCountMap.get(entry.getKey()));
			});

			// create stock charts
			chartService.createGraphs2(resultMap.keySet(), force);
			//
			mav.addObject("upPercentAccMap", resultMap);
			mav.addObject("upProbabilityMap", upProbabilityMap);
			mav.addObject("dataCountMap", dataCountMap);
			mav.addObject("siMap", siMap);
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
	 * Buy a call warrant(with the largest trade value for the specific target
	 * stock) on each trading date and hold for a 'holdPeriod' day period, calculate
	 * the probability of going up and sort the stock targets based on this
	 * probability. Parameters: holdPeriod: the period of each warrant item to hold;
	 * dataDatePeriod: how many days of data to calculate
	 * 
	 */
	@GetMapping("/putWarrantSelectStrategy1")
	public ModelAndView putWarrantSelectStrategy1(
			@RequestParam(value = "holdPeriod", defaultValue = "3") int holdPeriod,
			@RequestParam(value = "dataDatePeriod", defaultValue = "120") int dataDatePeriod,
			@RequestParam(value = "selectCount", defaultValue = "50") int selectCount,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		// int totalDatePeriod = (int) stockService.tradingDateCount();
		Date date = stockService.getLatestTradingDate();
		ModelAndView mav;
		// statsMap: key is the symbol, value is a tree map with key of date string and
		// value of double(warrant price up percentage)
		Map<String, TreeMap<String, Double>> statsMap = strategyService
				.getStatsDataForPutWarrantSelectStrategy1(holdPeriod);
		if (statsMap != null) {
			// all the stock items with put warrants(used for getting its item name)
			Map<String, StockItem> siMap = putWarrantTradeSummaryService.getStockItemsWithPutWarrant();
			// upProbabilityMap: store the probability of the warrant price going up
			// key is the symbol string and value is a double of probability
			Map<String, Double> upProbabilityMap = new HashMap<>();
			// upPercentAccMap: store the accumulated price up percentages
			// key: symbol string, value is a double of accumulated percentage
			Map<String, Double> upPercentAccMap = new HashMap<>();
			// dataCountMap: store the count of actual data points used for the calculation
			// for up probability and percent accumulation
			// key : symbol string, value is a long for the data count
			Map<String, Long> dataCountMap = new HashMap<>();
			for (String symbol : statsMap.keySet()) {
				// upPercentMap contains price up percentages of warrant on each trading date
				// for the stock symbol
				// key : date string, value: a double of up percentage
				TreeMap<String, Double> upPercentMap = statsMap.get(symbol);
				// filteredUpPercentMap: store filtered result of upPercentMap (only select the
				// latest 'dataDatePeriod' trading dates)
				final TreeMap<String, Double> filteredUpPercentMap;
				if (upPercentMap.size() > dataDatePeriod) {
					filteredUpPercentMap = new TreeMap<>();
					// below is not compiling because collect(Collectors.toMap()) method will
					// return a Map object, can not be casted to a TreeMap
					//
					// filteredUpPercentMap =
					// upPercentMap.entrySet().stream().skip(upPercentMap.size() - dataDatePeriod)
					// .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
					upPercentMap.entrySet().stream().skip(upPercentMap.size() - dataDatePeriod)
							.forEach(e -> filteredUpPercentMap.put(e.getKey(), e.getValue()));
				} else {
					filteredUpPercentMap = upPercentMap;
				}
				long size = filteredUpPercentMap.size();
				long up_size = filteredUpPercentMap.values().stream().filter(value -> value > 0.0).count();
				double acc_percent = filteredUpPercentMap.values().stream().reduce(0.0, (a, b) -> a + b);
				if (size == 0)
					continue;
				upProbabilityMap.put(symbol, StockUtils.roundDoubleDp4((double) up_size / size));
				upPercentAccMap.put(symbol, StockUtils.roundDoubleDp4(acc_percent));
				dataCountMap.put(symbol, size);

			}
			mav = new ModelAndView("stock/putWarrantSelectStrategy1");
			//
			LinkedHashMap<String, Double> resultMap = new LinkedHashMap<>();
			upPercentAccMap.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed())
					.limit(selectCount).forEach(e -> resultMap.put(e.getKey(), e.getValue()));

			resultMap.entrySet().stream().forEach(entry -> {
				logger.info("Symbol:" + entry.getKey() + "(" + siMap.get(entry.getKey()).getName()
						+ "), avg percentage:" + entry.getValue() + ", win percentage:"
						+ upProbabilityMap.get(entry.getKey()) + ", data count:" + dataCountMap.get(entry.getKey()));
			});

			// create stock charts
			chartService.createGraphs2(resultMap.keySet(), force);
			//
			mav.addObject("upPercentAccMap", resultMap);
			mav.addObject("upProbabilityMap", upProbabilityMap);
			mav.addObject("dataCountMap", dataCountMap);
			mav.addObject("siMap", siMap);
			mav.addObject("tradingDate", date);
			mav.addObject("dataDatePeriod", dataDatePeriod);
			mav.addObject("selectCount", selectCount);
			mav.addObject("holdPeriod", holdPeriod);
			// stockItems with call and put warrants
			mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
			// mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		} else {
			mav = new ModelAndView("stock/error");
		}
		return mav;
	}

	@GetMapping("/sma20SelectStrategy2")
	public ModelAndView sma20SelectStrategy2(@RequestParam(value = "force", defaultValue = "false") boolean force,
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote) {
		Date date = stockService.getLatestTradingDate();
		String dateString = StockUtils.dateToSimpleString(date);
		// get all the stockItem with call warrants
		Map<String, StockItem> siMap = callWarrantTradeSummaryService.getStockItemsWithCallWarrant();
		ModelAndView mav = new ModelAndView("stock/sma20SelectStrategy2");
		LinkedHashMap<String, List<Number>> statsMap;
		if (!strategyService.existsForSma20SelectStrategy2StatsData(dateString)) {
			Map<String, List<Number>> resultMap = new HashMap<>();
			for (StockItem si : siMap.values()) {
				List<StockItemData> sidList = stockService.getStockItemStatsData(si);
				if (sidList == null || !strategyService.isLatestPriceAboveSma20(sidList)) {
					continue;
				} else {
					int daysAboveSma20 = strategyService.getDaysAboveSma20(sidList);
					int daysSma20GoingUp = strategyService.getDaysSma20GoingUp(sidList);
					double latestPriceAboveSma20 = strategyService.getLatestPriceAboveSma20(sidList);
					double latestK9 = strategyService.getLatestK9(sidList);
					double latestD9 = strategyService.getLatestD9(sidList);
					resultMap.put(si.getSymbol(), Arrays.<Number>asList(daysAboveSma20, daysSma20GoingUp,
							latestPriceAboveSma20, latestK9, latestD9));
				}
			}
			// sort according to daysAboveSma20
			statsMap = new LinkedHashMap<>();
			resultMap.entrySet().stream()
					.sorted((e1, e2) -> e1.getValue().get(0).intValue() > e2.getValue().get(0).intValue() ? -1
							: (e1.getValue().get(0).intValue() < e2.getValue().get(0).intValue() ? 1 : 0))
					.forEach(e -> statsMap.put(e.getKey(), e.getValue()));
			// write statsMap
			try {
				strategyService.saveSma20SelectStrategy2StatsData(dateString, statsMap);
			} catch (StockException ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				statsMap = strategyService.loadSma20SelectStrategy2StatsData(dateString);
			} catch (StockException e) {
				e.printStackTrace();
				mav = new ModelAndView("stock/error");
				return mav;
			}
		}
		// create stock charts
		chartService.createGraphs2(statsMap.keySet(), force);
		Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
		if (realtimeQuote) {
			RealtimeMarketInfo mri;
			try {
				mri = realtimeQuoteService.getInfo(statsMap.keySet());
				for (StockItemMarketInfo simi : mri.getMsgArray()) {
					realtimeMap.put(simi.getC(), simi);
				}
			} catch (StockException e) {
				logger.warn("Error getting realtime quote!");
				e.printStackTrace();
			}
		}
		//
		mav.addObject("realtimeMap", realtimeMap);
		mav.addObject("tradingDate", date);
		mav.addObject("statsMap", statsMap);
		mav.addObject("siMap", siMap);
		// stockItems with call and put warrants
		// mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}

	@GetMapping("/getRealtimeQuote")
	private ResponseMessage getRealtimeQuote(@RequestParam(value = "symbols") String symbols) {
		List<String> symbolList = new ArrayList<>();
		for (String symbol : symbols.split(",")) {
			symbolList.add(symbol);
		}
		ResponseMessage mes = new ResponseMessage();
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

	@GetMapping("/prepareWeeklyStockPriceForAll")
	public ResponseMessage prepareWeeklyStockPriceForAll() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.prepareWeeklyStockPriceForAll();
			mes.setCategory("Success");
			mes.setText("Weekly stock price has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Weekly stock price fails to be updated.");
		}
		return mes;
	}

	@GetMapping("/calculateAndSaveWeeklyKDForAll")
	public ResponseMessage calculateAndSaveWeeklyKDForAll() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.calculateAndSaveWeeklyKDForAll();
			mes.setCategory("Success");
			mes.setText("Weekly stats data has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Weekly stats data fails to be updated.");
		}
		return mes;
	}

	@GetMapping("/preparePriceBreakUpSelectStrategy3")
	public ResponseMessage preparePriceBreakUpSelectStrategy3() {
		ResponseMessage mes = new ResponseMessage();
		try {
			strategyService.preparePriceBreakUpSelectStrategy3();
			mes.setCategory("Success");
			mes.setText("Price break up select strategy stats data has been prepared.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Price break up select strategy stats data fails to be prepared.");
		}
		return mes;
	}

	@GetMapping("/priceBreakUpSelectStrategy3")
	public ModelAndView priceBreakUpSelectStrategy3(
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
		Map<String, Integer> dataMap = strategyService.getStatsDataForPriceBreakUpSelectStrategy3(dateString);
		LinkedHashMap<String, Integer> statsMap = new LinkedHashMap<>();
		dataMap.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(selectCount)
				.forEach(e -> statsMap.put(e.getKey(), e.getValue()));
		// create stock charts
		chartService.createGraphs2(statsMap.keySet(), force);
		Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
		if (realtimeQuote) {
			RealtimeMarketInfo mri;
			try {
				mri = realtimeQuoteService.getInfo(statsMap.keySet());
				for (StockItemMarketInfo simi : mri.getMsgArray()) {
					realtimeMap.put(simi.getC(), simi);
				}
			} catch (StockException e) {
				logger.warn("Error getting realtime quote!");
				e.printStackTrace();
			}
		}
		//
		mav.addObject("realtimeMap", realtimeMap);
		mav.addObject("tradingDate", date);
		mav.addObject("statsMap", statsMap);
		mav.addObject("siMap", siMap);
		// stockItems with call and put warrants
		// mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}
}
