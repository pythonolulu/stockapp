package com.javatican.stock;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatican.stock.model.StockPrice;
/*
 * StockPrice data is store in json file. 
 */
@Repository("stockPriceDAO")
public class StockPriceDAO {
	private static final Logger logger = LoggerFactory.getLogger(StockPriceDAO.class);
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

	@Autowired
	private ResourceLoader resourceLoader;

	static {
		objectMapper.setDateFormat(df);
	}

	public void save(String stockSymbol, List<StockPrice> spList) throws StockException {
		Resource resource = resourceLoader.getResource(String.format("file:./download/%s.json", stockSymbol));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, spList);
			logger.info("Finish save stock prices for stock:" + stockSymbol);
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<StockPrice> load(String stockSymbol) throws StockException {
		Resource resource = resourceLoader.getResource(String.format("file:./download/%s.json", stockSymbol));
		try (InputStream st = resource.getInputStream();) {
			List<StockPrice> spList = objectMapper.readValue(st, new TypeReference<List<StockPrice>>() {
			});
			return spList;
		} catch (Exception ex) {
			throw new StockException(ex);
		} 
	}

	private Map<Date, StockPrice> loadAsMap(String stockSymbol) throws StockException {
		Map<Date, StockPrice> spMap = new TreeMap<>();
		List<StockPrice>  spList = load(stockSymbol);
		for (StockPrice sp: spList) {
			spMap.put(sp.getTradingDate(), sp);
		}
		return spMap;
	}
	/*
	 * add new StockPrice data into the json file. Duplicate items will be excluded.
	 */
	public void addStockPriceList(String stockSymbol, List<StockPrice> spList) throws StockException{
		List<StockPrice>  cList = load(stockSymbol);
		Date d = cList.get(cList.size()-1).getTradingDate();
		for(StockPrice sp: spList) {
			//only new data will be added into the list
			if(sp.getTradingDate().after(d)) {
				cList.add(sp);
				logger.info("Add stock price for "+stockSymbol+" @"+sp.getTradingDate());
			}
		}
		save(stockSymbol, cList);
	}
}
