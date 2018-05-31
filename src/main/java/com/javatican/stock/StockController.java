package com.javatican.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javatican.stock.util.ResponseMessage;

@RestController
@RequestMapping("stock/*")
public class StockController {

	@Autowired
	private StockService stockService;
	@Autowired
	private StockItemService stockItemService;
	@Autowired
	private StockTradeByTrustService stockTradeByTrustService;
	
	
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

	@GetMapping("/createStockProfiles")
	public ResponseMessage createStockProfiles() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockItemService.downloadAndSaveStockItems();;
			mes.setCategory("Success");
			mes.setText("download and save all stock profiles.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Fails to download and save all stock profiles.");
		}
		return mes;
	}
	
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

	@GetMapping("/updateData")
	public ResponseMessage updateDate() {
		ResponseMessage mes = new ResponseMessage();
		try {
			stockService.updateTradingDateAndValue();
			mes.setCategory("Success");
			mes.setText("Trading date information has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Trading date information fails to be updated.");
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
}
