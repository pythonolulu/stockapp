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
import com.javatican.stock.util.StockUtils;

/*
 * StockPrice data is store in json file. 
 */
@Repository("callWarrantSelectStrategy1DAO")
public class CallWarrantSelectStrategy1DAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RESOURCE_FILE_PATH = "file:./strategy/1/%s.json";
	@Autowired
	private ResourceLoader resourceLoader;

	static {
		objectMapper.setDateFormat(df);
	}

	public CallWarrantSelectStrategy1DAO() {
	}

	public void save(String stockSymbol, Map<String, Double> upPercentMap) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, upPercentMap);
			logger.info("Finish saving strategy data for stock:" + stockSymbol);
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public Map<String, Double> load(String stockSymbol) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		try (InputStream st = resource.getInputStream();) {
			Map<String, Double> upPercentMap = objectMapper.readValue(st, new TypeReference<Map<String, Double>>() {
			});
			return upPercentMap;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public Map<String, Double> loadBetweenDate(String stockSymbol, Date start, Date end) throws StockException {

		Map<String, Double> upPercentMap = this.load(stockSymbol);
		return upPercentMap.entrySet().stream()
				.filter(e -> StockUtils.stringSimpleToDate(e.getKey()).get().compareTo(start) >= 0
						&& StockUtils.stringSimpleToDate(e.getKey()).get().compareTo(end) <= 0)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	}
}
