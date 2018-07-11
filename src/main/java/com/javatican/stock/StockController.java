package com.javatican.stock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.ModelAndView;

import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.StockItemData;
import com.javatican.stock.model.StockPriceChange;
import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.service.CallWarrantTradeSummaryService;
import com.javatican.stock.service.ChartService;
import com.javatican.stock.service.DealerTradeSummaryService;
import com.javatican.stock.service.MarginService;
import com.javatican.stock.service.PutWarrantTradeSummaryService;
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
	public ModelAndView getTop30ByTrust(@PathVariable String tradingDate) {
		// show 10 data points for each stock item
		int dateLength = 10;
		Date date = StockUtils.stringSimpleToDate(tradingDate).get();
		// key: stockItem
		// value: a map with key of date string and value StockTradeByTrust object
		Map<StockItem, Map<String, StockTradeByTrust>> dataMap = stockService.getTop30StockItemTradeByTrust(date,
				dateLength);
		// create stock charts
		chartService.createGraphs(dataMap.keySet());
		// also return trading date list
		List<Date> dateList = stockService.getLatestNTradingDateDesc(dateLength);
		List<String> dList = dateList.stream().map(td -> StockUtils.dateToStringSeparatedBySlash(td))
				.collect(Collectors.toList());
		ModelAndView mav = new ModelAndView("stock/top30ByTrust");
		// prepare K/D values
		Map<StockItem, Map<String, StockItemData>> statsMap = stockService
				.getStockItemStatsData(new ArrayList<StockItem>(dataMap.keySet()), dateList);

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
	public ModelAndView top50(@PathVariable String tradingDate) {
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
			Map<StockItem, Map<String, StockItemData>> statsMap = stockService.getStockItemStatsData(
					spcList.stream().map(spc -> spc.getStockItem()).collect(Collectors.toList()), dateList);
			// create stock charts
			chartService.createGraphs(statsMap.keySet());
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
	public ModelAndView bottom50(@PathVariable String tradingDate) {
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
			Map<StockItem, Map<String, StockItemData>> statsMap = stockService.getStockItemStatsData(
					spcList.stream().map(spc -> spc.getStockItem()).collect(Collectors.toList()), dateList);
			// create stock charts
			chartService.createGraphs(statsMap.keySet());
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
//	@GetMapping("/prepareCallWarrantData")
//	private ResponseMessage prepareCallWarrantData() {
//		ResponseMessage mes = new ResponseMessage();
//		try {
//			callWarrantTradeSummaryService.prepareData();
//			mes.setCategory("Success");
//			mes.setText("Call warrant trading data has been updated.");
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			mes.setCategory("Fail");
//			mes.setText("Call warrant trading data fails to be updated.");
//		}
//		return mes;
//
//	}

//	@GetMapping("/preparePutWarrantData")
//	private ResponseMessage preparePutWarrantData() {
//		ResponseMessage mes = new ResponseMessage();
//		try {
//			putWarrantTradeSummaryService.prepareData();
//			mes.setCategory("Success");
//			mes.setText("Put warrant trading data has been updated.");
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			mes.setCategory("Fail");
//			mes.setText("Put warrant trading data fails to be updated.");
//		}
//		return mes;
//
//	}

	// @GetMapping("/{stockSymbol}/getChart")
	// public Resource getChart(@PathVariable String stockSymbol) {
	// chartService.createGraph(stockSymbol);
	// return new ServletContextResource(servletContext,
	// "/stock/imgs/"+stockSymbol+".png");
	// }
	@GetMapping("/{stockSymbol}/getChart")
	public ModelAndView getChart(@PathVariable String stockSymbol) {
		chartService.createGraph(stockSymbol);
		return new ModelAndView("redirect:" + "/stock/imgs/" + stockSymbol + ".png");
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
	

	@GetMapping("/callWarrantSelectStrategy1")
	private ResponseMessage callWarrantSelectStrategy1() {
		ResponseMessage mes = new ResponseMessage();
		try {
			strategyService.callWarrantSelectStrategy1(3, (int) stockService.tradingDateCount());
			mes.setCategory("Success");
			mes.setText("call Warrant Select Strategy data has been calculated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("call Warrant Select Strategy data has failed to be calculated.");
		}
		return mes;

	}
}
