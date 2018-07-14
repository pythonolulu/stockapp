package com.javatican.stock.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.javatican.stock.util.StockUtils; 

@Repository("callWarrantSelectStrategy1DAO")
public class CallWarrantSelectStrategy1DAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RESOURCE_FILE_PATH = "file:./strategy/1/call/%s_%s.json";
	private static final String COMBINED_RESOURCE_FILE_PATH = "file:./strategy/1c/call/%s.json";
	@Autowired
	private ResourceLoader resourceLoader;
	// @Autowired
	// private ResourcePatternResolver resourcePatternResolver;

	static {
		objectMapper.setDateFormat(df);
	}

	public CallWarrantSelectStrategy1DAO() {
	}

	public boolean existsForCombinedResult(int holdPeriod) {
		Resource resource = resourceLoader
				.getResource(String.format(COMBINED_RESOURCE_FILE_PATH, holdPeriod));
		return resource.exists();
	}

	public void saveCombinedResult( int holdPeriod, Map<String, TreeMap<String, Double>> statsMap)
			throws StockException {
		Resource resource = resourceLoader
				.getResource(String.format(COMBINED_RESOURCE_FILE_PATH, holdPeriod));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, statsMap);
			logger.info("Finish saving combined strategy '1' data of holdPeriod: " + holdPeriod + " days");
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public Map<String, TreeMap<String, Double>> loadCombinedResult(int holdPeriod)
			throws StockException {
		Resource resource = resourceLoader
				.getResource(String.format(COMBINED_RESOURCE_FILE_PATH, holdPeriod));
		try (InputStream st = resource.getInputStream();) {
			Map<String, TreeMap<String, Double>> statsMap = objectMapper.readValue(st,
					new TypeReference<Map<String, TreeMap<String, Double>>>() {
					});
			return statsMap;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public void save(String stockSymbol, int holdPeriod, TreeMap<String, Double> upPercentMap) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol, holdPeriod));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, upPercentMap);
			logger.info(
					"Finish saving strategy '1' data of holdPeriod: " + holdPeriod + " days for stock:" + stockSymbol);
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public TreeMap<String, Double> load(String stockSymbol, int holdPeriod) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol, holdPeriod));
		try (InputStream st = resource.getInputStream();) {
			TreeMap<String, Double> upPercentMap = objectMapper.readValue(st, new TypeReference<TreeMap<String, Double>>() {
			});
			return upPercentMap;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public TreeMap<String, Double> loadBetweenDate(String stockSymbol, int holdPeriod, Date start, Date end) throws StockException {

		TreeMap<String, Double> upPercentMap = this.load(stockSymbol, holdPeriod);
//		return upPercentMap.entrySet().stream()
//				.filter(e -> StockUtils.stringSimpleToDate(e.getKey()).get().compareTo(start) >= 0
//						&& StockUtils.stringSimpleToDate(e.getKey()).get().compareTo(end) <= 0)
//				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		TreeMap<String, Double> resultMap = new TreeMap<>();
		upPercentMap.entrySet().stream()
		.filter(e -> StockUtils.stringSimpleToDate(e.getKey()).get().compareTo(start) >= 0
				&& StockUtils.stringSimpleToDate(e.getKey()).get().compareTo(end) <= 0)
		.forEach(e->resultMap.put(e.getKey(), e.getValue()));
		return resultMap;

	}
}
