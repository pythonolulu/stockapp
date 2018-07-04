package com.javatican.stock.dao;

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
import com.javatican.stock.StockException;
import com.javatican.stock.model.StockPrice;
import com.javatican.stock.model.WarrantTrade;

@Repository("warrantTradeDAO")
public class WarrantTradeDAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String CALL_WARRANT_TRADE_PATH = "file:./download/warrant/%s_call.json";
	private static final String PUT_WARRANT_TRADE_PATH = "file:./download/warrant/%s_put.json";
	@Autowired
	private ResourceLoader resourceLoader;
	static {
		objectMapper.setDateFormat(df);
	}

	public void saveCallWarrants(String dateString, List<WarrantTrade> wtList) throws StockException {
		this.save(dateString, wtList, true);
	}

	public void savePutWarrants(String dateString, List<WarrantTrade> wtList) throws StockException {
		this.save(dateString, wtList, false);
	}

	private void save(String dateString, List<WarrantTrade> wtList, boolean isCall) throws StockException {
		String resource_path = isCall ? CALL_WARRANT_TRADE_PATH : PUT_WARRANT_TRADE_PATH;
		Resource resource = resourceLoader.getResource(String.format(resource_path, dateString));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, wtList);
			logger.info("Finish saving " + (isCall ? "call" : "put") + " warrant trade data for date:" + dateString);
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<WarrantTrade> loadCallWarrants(String dateString) throws StockException {
		return this.load(dateString, true);
	}

	public List<WarrantTrade> loadPutWarrants(String dateString) throws StockException {
		return this.load(dateString, false);
	}

	private List<WarrantTrade> load(String dateString, boolean isCall) throws StockException {
		String resource_path = isCall ? CALL_WARRANT_TRADE_PATH : PUT_WARRANT_TRADE_PATH;
		Resource resource = resourceLoader.getResource(String.format(resource_path, dateString));
		try (InputStream st = resource.getInputStream();) {
			List<WarrantTrade> wtList = objectMapper.readValue(st, new TypeReference<List<WarrantTrade>>() {
			});
			return wtList;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}
	
	//return a map with key of warrant symbol
	public Map<String, WarrantTrade> loadCallWarrantsAsMap(String dateString) throws StockException {
		Map<String, WarrantTrade> wtMap = new TreeMap<>();
		List<WarrantTrade> wtList = loadCallWarrants(dateString);
		wtList.stream().forEach(wt -> wtMap.put(wt.getWarrantSymbol(), wt));
		return wtMap;
	}

	public Map<String, WarrantTrade> loadPutWarrantsAsMap(String dateString) throws StockException {
		Map<String, WarrantTrade> wtMap = new TreeMap<>();
		List<WarrantTrade> wtList = loadPutWarrants(dateString);
		wtList.stream().forEach(wt -> wtMap.put(wt.getWarrantSymbol(), wt));
		return wtMap;
	}
}