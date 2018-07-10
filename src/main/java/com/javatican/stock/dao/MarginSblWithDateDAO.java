package com.javatican.stock.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatican.stock.StockException;
import com.javatican.stock.model.MarginSblWithDate;

/*
 * MarginSblWithDate data is store in json file. 
 */
@Repository("marginSblWithDateDAO")
public class MarginSblWithDateDAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RESOURCE_FILE_PATH = "file:./download/item_margin_sbl/%s.json";
	@Autowired
	private ResourceLoader resourceLoader;

	static {
		objectMapper.setDateFormat(df);
	}

	public MarginSblWithDateDAO() {
	}

	public boolean existsForSymbol(String stockSymbol) {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		return resource.exists();
	}

	public void save(String stockSymbol, List<MarginSblWithDate> mswdList) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, mswdList);
			logger.info("Finish saving margin and sbl data for stock:" + stockSymbol);
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<MarginSblWithDate> load(String stockSymbol) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		try (InputStream st = resource.getInputStream();) {
			List<MarginSblWithDate> mswdList = objectMapper.readValue(st, new TypeReference<List<MarginSblWithDate>>() {
			});
			return mswdList;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<MarginSblWithDate> loadBetweenDate(String stockSymbol, Date start, Date end) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		try (InputStream st = resource.getInputStream();) {
			List<MarginSblWithDate> mswdList = objectMapper.readValue(st, new TypeReference<List<MarginSblWithDate>>() {
			});

			return mswdList.stream()
					.filter(mswd -> mswd.getTradingDate().compareTo(start) >= 0 && mswd.getTradingDate().compareTo(end) <= 0)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	private Map<Date, MarginSblWithDate> loadAsMap(String stockSymbol) throws StockException {
		Map<Date, MarginSblWithDate> mswdMap = new TreeMap<>();
		List<MarginSblWithDate> mswdList = load(stockSymbol);
		mswdList.stream().forEach(mswd -> mswdMap.put(mswd.getTradingDate(), mswd));
		return mswdMap;
	}

	/*
	 * add new MarginSblWithDate data into the json file. Duplicate items will be excluded.
	 */
	public List<MarginSblWithDate> update(String stockSymbol, List<MarginSblWithDate> newData) throws StockException {
		List<MarginSblWithDate> existingData = load(stockSymbol);
		return update(stockSymbol, existingData, newData);
	}

	/*
	 * add new MarginSblWithDate data into the json file. Duplicate items will be excluded.
	 */
	public List<MarginSblWithDate> update(String stockSymbol, List<MarginSblWithDate> existingData, List<MarginSblWithDate> newData)
			throws StockException {
		Date d = getLatestDateForMarginData(existingData);
		for (MarginSblWithDate mswd : newData) {
			// only new data will be added into the list
			if (mswd.getTradingDate().after(d)) {
				existingData.add(mswd);
				logger.info("Add margin and sbl data for " + stockSymbol + " @" + mswd.getTradingDate());
			}
		}
		save(stockSymbol, existingData);
		return existingData;
	}

	/*
	 * get the latest date  
	 */
	public Date getLatestDateForMarginData(String stockSymbol) throws StockException {
		List<MarginSblWithDate> cList = load(stockSymbol);
		// assuming the price data is sorted by date ascending
		// get the last trading date of price data
		if (cList.size() > 0) {
			return cList.get(cList.size() - 1).getTradingDate();
		} else {
			throw new StockException("No margin and sbl data in the resource file for symbol:" + stockSymbol);
		}
	}

	/*
	 * get the latest date for the stock price data
	 */
	public Date getLatestDateForMarginData(List<MarginSblWithDate> mswdList) throws StockException {
		// assuming the data is sorted by date ascending
		// get the last trading date 
		if (mswdList.size() > 0) {
			return mswdList.get(mswdList.size() - 1).getTradingDate();
		} else {
			throw new StockException("No margin and sbl data in the input data");
		}
	}

}
