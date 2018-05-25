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
	private IndividualStockService individualStockService;


	@GetMapping("/{stockSymbol}/createProfile")
	public ResponseMessage createStockProfile(@PathVariable String stockSymbol) {
		ResponseMessage mes = new ResponseMessage();
		try {
			individualStockService.createStockItem(stockSymbol);
			mes.setCategory("Success");
			mes.setText("Profile for stock "+stockSymbol+" has been created.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Profile for stock "+stockSymbol+" fails to be created.");
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
	@GetMapping("/{stockSymbol}/prepareData")
	public ResponseMessage prepareStockData(@PathVariable String stockSymbol) {
		ResponseMessage mes = new ResponseMessage();
		try {
			individualStockService.prepareData(stockSymbol);
			mes.setCategory("Success");
			mes.setText("Trading data for stock "+stockSymbol+" has been set");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Trading data for stock "+stockSymbol+" fail to be set.");
		}
		return mes; 
	}
	
	@GetMapping("/{stockSymbol}/updateData")
	public ResponseMessage updateStockData(@PathVariable String stockSymbol) {
		ResponseMessage mes = new ResponseMessage();
		try {
			individualStockService.updateData(stockSymbol);
			mes.setCategory("Success");
			mes.setText("Trading data for stock "+stockSymbol+" has been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Trading data for stock "+stockSymbol+" fails to be updated.");
		}
		return mes; 
	}
}
  