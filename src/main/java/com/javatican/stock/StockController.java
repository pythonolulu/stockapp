package com.javatican.stock;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javatican.stock.service.CallWarrantTradeSummaryService;
import com.javatican.stock.service.DealerTradeSummaryService;
import com.javatican.stock.service.FinancialService;
import com.javatican.stock.service.MarginService;
import com.javatican.stock.service.PutWarrantTradeSummaryService;
import com.javatican.stock.service.StockItemService;
import com.javatican.stock.service.StockService;
import com.javatican.stock.service.StockTradeByForeignService;
import com.javatican.stock.service.StockTradeByTrustService;
import com.javatican.stock.util.ResponseMessage;
import com.javatican.stock.util.StockUtils;

@RestController
@RequestMapping("stock/*")
public class StockController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private StockService stockService;
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
	private FinancialService financialService;

	// @Autowired
	// private ServletContext servletContext;

	/*
	 * 1. handler for downloading and saving any new trading date and trading values
	 * data
	 */
	@GetMapping("/updateData")
	public ResponseMessage updateData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			Date latestTradingDate = stockService.updateTradingDateAndValue();
			if (latestTradingDate == null) {
				mes.setCategory("Fail");
				mes.setText(String.format("No new data available."));
			} else {
				mes.setCategory("Success");
				mes.setText(String.format(
						"Trading date and value information has been updated. The latest trading date is ***%s",
						StockUtils.dateToSimpleString(latestTradingDate)));
			}
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
	public ResponseMessage updateTrustData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	public ResponseMessage updateForeignData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	@GetMapping("/updatePerformers")
	public ResponseMessage updateTopAndBottomPerformers(
			@RequestParam(value = "tradingDate", required = false) String dateString, HttpServletRequest request) {
		if (dateString == null) {
			dateString = StockUtils.dateToSimpleString(stockService.getLatestTradingDate());
		}
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			stockService.updatePerformers(dateString, 50);
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
	public ResponseMessage createStockPrices(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	public ResponseMessage updateMissingStockItemPriceField(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	public ResponseMessage updatePriceDataForAll(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	private ResponseMessage updateStockPriceData(@PathVariable String stockSymbol, HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	public ResponseMessage calculateAndSaveKDForAll(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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

	@GetMapping("/calculateIndexStatsData")
	public ResponseMessage calculateIndexStatsData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			stockService.calculateIndexStatsData();
			mes.setCategory("Success");
			mes.setText("Index Stats data has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Index Stats data fails to be updated.");
		}
		return mes;
	}

	@GetMapping("/calculateWeeklyIndexStatsData")
	public ResponseMessage calculateWeeklyIndexStatsData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			stockService.calculateWeeklyIndexStatsData();
			mes.setCategory("Success");
			mes.setText("Weekly Index Stats data has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Weekly Index Stats data fails to be updated.");
		}
		return mes;
	}

	@GetMapping("/{stockSymbol}/calculateAndSaveKD")
	private ResponseMessage calculateAndSaveKD(@PathVariable String stockSymbol, HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	public ResponseMessage updateCallWarrantData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	public ResponseMessage updatePutWarrantData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	public ResponseMessage updateDealerData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	public ResponseMessage updateMarginData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	private ResponseMessage extractMarginData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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

	@GetMapping("/prepareWeeklyStockPriceForAll")
	public ResponseMessage prepareWeeklyStockPriceForAll(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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
	public ResponseMessage calculateAndSaveWeeklyKDForAll(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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

	/*
	 * only run at the beginning of new month.
	 */
	@GetMapping("/updatePriceFieldForAll")
	public ResponseMessage updateStockItemPriceFieldForAll(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
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

	@GetMapping("/updateFinancialInfo")
	public ResponseMessage updateFinancialInfo(@RequestParam(value = "symbol", required = false) String symbol,
			@RequestParam(value = "checkExists", required = false, defaultValue = "true") boolean checkExists,
			HttpServletRequest request) {

		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			if (symbol == null) {
				financialService.updateData(checkExists);
			} else {
				financialService.updateData(symbol);
			}
			mes.setCategory("Success");
			mes.setText("Financial info data has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Financial info data fails to be updated.");
		}
		return mes;
	}

}
