package com.javatican.stock.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatican.stock.StockException;
import com.javatican.stock.model.StockPrice;

/*
 * StockPrice data is store in json file. 
 */
@Repository("stockPriceDAO")
public class StockPriceDAO {
	private static final Logger logger = LoggerFactory.getLogger(StockPriceDAO.class);
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RESOURCE_FILE_PATH = "file:./download/price/%s.json";
	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private ResourcePatternResolver resourcePatternResolver;

	static {
		objectMapper.setDateFormat(df);
	}

	public StockPriceDAO() {
	}

	public List<String> existingSymbols() throws StockException {
		try {
			Resource[] resources = resourcePatternResolver.getResources("file:./download/*.json");
			List<String> symbols = new ArrayList<>();
			for (Resource r : resources) {
				String filename = r.getFilename();
				symbols.add(filename.substring(0, filename.indexOf(".")));
			}
			return symbols;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public boolean existsForSymbol(String stockSymbol) {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		return resource.exists();
	}

	public void save(String stockSymbol, List<StockPrice> spList) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, spList);
			logger.info("Finish saving stock prices for stock:" + stockSymbol);
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<StockPrice> load(String stockSymbol) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		try (InputStream st = resource.getInputStream();) {
			List<StockPrice> spList = objectMapper.readValue(st, new TypeReference<List<StockPrice>>() {
			});
			return spList;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<StockPrice> loadBetweenDate(String stockSymbol, Date start, Date end) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		try (InputStream st = resource.getInputStream();) {
			List<StockPrice> spList = objectMapper.readValue(st, new TypeReference<List<StockPrice>>() {
			});

			return spList.stream()
					.filter(sp -> sp.getTradingDate().compareTo(start) >= 0 && sp.getTradingDate().compareTo(end) <= 0)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	private Map<Date, StockPrice> loadAsMap(String stockSymbol) throws StockException {
		Map<Date, StockPrice> spMap = new TreeMap<>();
		List<StockPrice> spList = load(stockSymbol);
		spList.stream().forEach(sp -> spMap.put(sp.getTradingDate(), sp));
		return spMap;
	}

	/*
	 * add new StockPrice data into the json file. Duplicate items will be excluded.
	 */
	public List<StockPrice> update(String stockSymbol, List<StockPrice> newData) throws StockException {
		List<StockPrice> existingData = load(stockSymbol);
		return update(stockSymbol, existingData, newData);
	}

	/*
	 * add new StockPrice data into the json file. Duplicate items will be excluded.
	 */
	public List<StockPrice> update(String stockSymbol, List<StockPrice> existingData, List<StockPrice> newData)
			throws StockException {
		Date d = getLatestDateForPriceData(existingData);
		for (StockPrice sp : newData) {
			// only new data will be added into the list
			if (sp.getTradingDate().after(d)) {
				existingData.add(sp);
				logger.info("Add stock price for " + stockSymbol + " @" + sp.getTradingDate());
			}
		}
		save(stockSymbol, existingData);
		return existingData;
	}

	/*
	 * get the latest date for the stock price data
	 */
	public Date getLatestDateForPriceData(String stockSymbol) throws StockException {
		List<StockPrice> cList = load(stockSymbol);
		// assuming the price data is sorted by date ascending
		// get the last trading date of price data
		if (cList.size() > 0) {
			return cList.get(cList.size() - 1).getTradingDate();
		} else {
			throw new StockException("No price data in the resource file for symbol:" + stockSymbol);
		}
	}

	/*
	 * get the latest date for the stock price data
	 */
	public Date getLatestDateForPriceData(List<StockPrice> spList) throws StockException {
		// assuming the price data is sorted by date ascending
		// get the last trading date of price data
		if (spList.size() > 0) {
			return spList.get(spList.size() - 1).getTradingDate();
		} else {
			throw new StockException("No price data in the input data");
		}
	}

	/*
	 * return List of StockPrice if the latest date for the stock price data is
	 * before the specified date otherwise return null
	 */
	public List<StockPrice> getExistingPriceDataIfNewDataAvailable(String stockSymbol, Date date)
			throws StockException {
		List<StockPrice> cList = load(stockSymbol);
		// assuming the price data is sorted by date ascending
		// get the last trading date of price data
		if (cList.size() > 0) {
			Date latestDate = cList.get(cList.size() - 1).getTradingDate();
			if (latestDate.before(date)) {
				return cList;
			} else {
				return null;
			}
		} else {
			throw new StockException("No price data in the resource file for symbol:" + stockSymbol);
		}
	}
}
