package com.javatican.stock;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.javatican.stock.model.StockTradeByTrust;
import com.javatican.stock.service.StockItemService;
import com.javatican.stock.service.StockService;
import com.javatican.stock.service.StockTradeByTrustService;
import com.javatican.stock.util.ResponseMessage;
import com.javatican.stock.util.StockUtils;

@RestController
@RequestMapping("stock/*")
public class StockController {

	@Autowired
	private StockService stockService;
	@Autowired
	private StockItemService stockItemService;
	@Autowired
	private StockTradeByTrustService stockTradeByTrustService;
	
	/*
	 * only call once during initial setup
	 */
	@GetMapping("/prepareTrustData")
	public ResponseMessage prepareTrustData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockTradeByTrustService.prepareData();
			mes.setCategory("Success");
			mes.setText("Stock Trade Data by Trust have been created.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText(" Stock Trade Data by Trust fail to be created.");
		}
		return mes;
	}
	/*
	 * 2. handler for download and save any new stock trading data by Trust
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
	 * 3. handler for download and save stock prices for any new stock items
	 */
	@GetMapping("/createStockPrices")
	public ResponseMessage createStockPrices() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.downloadAndSaveStockPrices();;
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
	 * 1. handler for download and save any new trading date and trading values data
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

	@GetMapping("/prepareData")
	public ResponseMessage prepareData() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockService.prepareData();
			mes.setCategory("Success");
			mes.setText("Trading date information has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Trading date information fails to be updated.");
		}
		return mes;
	}

	@GetMapping("/updatePriceDataForAll")
	public ResponseMessage updatePriceDataForAllSymbols() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.updatePriceDataForAllExistSymbols();
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
	public ResponseMessage updateStockPriceData(@PathVariable String stockSymbol) {
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
	 * 4. handler for calculate average price for last month for missing price field stock items.
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
	
	
	@GetMapping("/updatePriceFieldForAll")
	public ResponseMessage updateStockItemPriceFieldForAll() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.updateStockItemPriceFieldForAllSymbols();
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
	 * 5. handler for calculate the top 30 stocks traded by Trust for the specified trading date.
	 */
	@GetMapping("/{tradingDate}/top30ByTrust")
	public ModelAndView getTop30ByTrust(@PathVariable String tradingDate) {
		Date date = StockUtils.stringSimpleToDate(tradingDate).get();
		List<StockTradeByTrust> stbtList =  stockService.getTop30StockTradeByTrust(date);
		ModelAndView mav = new ModelAndView("stock/top30ByTrust");	
		mav.addObject("tradingDate", date);
		mav.addObject("stbtItems", stbtList);
		return mav;
	}
}
