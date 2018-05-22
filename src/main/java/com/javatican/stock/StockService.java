package com.javatican.stock;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.model.TradingDate;
import com.javatican.stock.util.StockUtils;

@Service("stockService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor=StockException.class)
public class StockService {
	private static final String TWSE_DAILY_TRADING_GET_URL = "http://www.tse.com.tw/en/exchangeReport/FMTQIK?response=html&date=%s";
	@Autowired
	StockDAO stockDAO;
	
	public void storeTradingDate() throws StockException {
		try {
			Document doc = Jsoup.connect(String.format(TWSE_DAILY_TRADING_GET_URL, StockUtils.todayDateString()))
					.get();
			//StockUtils.writeDocumentToFile(doc, "test.html");
			Elements trs = doc.select("table > tbody > tr");
			for(Element tr: trs) {
				Element td = tr.selectFirst("td");
				TradingDate tDate = new TradingDate();
				tDate.setDateAsString(td.text());
				stockDAO.saveTradingDate(tDate);
			}
		} catch (IOException ex) {
			throw new StockException(ex);
		}
	}

}
