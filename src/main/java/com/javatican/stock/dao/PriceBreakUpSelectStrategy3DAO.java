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

@Repository("priceBreakUpSelectStrategy3DAO")
public class PriceBreakUpSelectStrategy3DAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String ORIGINAL_RESOURCE_FILE_PATH = "file:./strategy/3/breakup/%s.json";
	private static final String RESULT_RESOURCE_FILE_PATH = "file:./strategy/3r/breakup/%s.json";
	@Autowired
	private ResourceLoader resourceLoader;
	// @Autowired
	// private ResourcePatternResolver resourcePatternResolver;

	static {
		objectMapper.setDateFormat(df);
	}

	public PriceBreakUpSelectStrategy3DAO() {
	}

	/*
	 * 'result' here means: the data stores the 'difference' of breakup 'times'
	 * between the target trading date and its previous date . The map is a Map
	 * with key of a string 'stock symbol' and value of an integer represents the
	 * difference b/w the number of times the highest price of the target date
	 * breaks above the highest price for each past week and that of its previous
	 * trading date.
	 */
	public boolean resultExistsForDate(String dateString) {
		Resource resource = resourceLoader.getResource(String.format(RESULT_RESOURCE_FILE_PATH, dateString));
		return resource.exists();
	}

	public void saveResult(String dateString, Map<String, Integer> resultMap) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESULT_RESOURCE_FILE_PATH, dateString));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, resultMap);
			logger.info("Finish saving price break up strategy '3' data of date: " + dateString);
		} catch (Exception ex) {
			logger.warn("Errror while trying to save price break up strategy '3' data of date: " + dateString);
			throw new StockException(ex);
		}
	}

	public Map<String, Integer> loadResult(String dateString) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESULT_RESOURCE_FILE_PATH, dateString));
		try (InputStream st = resource.getInputStream();) {
			Map<String, Integer> resultMap = objectMapper.readValue(st,
					new TypeReference<Map<String, Integer>>() {
					});
			return resultMap;
		} catch (Exception ex) {
			logger.warn("Errror while trying to load price break up strategy '3' data of date: " + dateString);
			throw new StockException(ex);
		}
	}

	/*
	 * The 'original' means it stores the raw 'count' data for each trading date.
	 * The statsMap is a TreeMap with key of trading date and value of an integer
	 * represents the number of times the highest price of the target trading date
	 * breaks above the highest price of each past week.
	 */
	public void save(String stockSymbol, TreeMap<Date, Integer> statsMap) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(ORIGINAL_RESOURCE_FILE_PATH, stockSymbol));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, statsMap);
			logger.info("Finish saving price break up strategy '3' data for stock:" + stockSymbol);
		} catch (Exception ex) {
			logger.warn("Errror while trying to save price break up strategy '3' data for stock:" + stockSymbol);
			throw new StockException(ex);
		}
	}

	public TreeMap<Date, Integer> load(String stockSymbol) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(ORIGINAL_RESOURCE_FILE_PATH, stockSymbol));
		try (InputStream st = resource.getInputStream();) {
			TreeMap<Date, Integer> statsMap = objectMapper.readValue(st, new TypeReference<TreeMap<Date, Integer>>() {
			});
			return statsMap;
		} catch (Exception ex) {
			logger.warn("Errror while trying to save price break up strategy '3' data for stock:" + stockSymbol);
			throw new StockException(ex);
		}
	}

	public TreeMap<Date, Integer> loadBetweenDate(String stockSymbol, Date start, Date end) throws StockException {
		TreeMap<Date, Integer> statsMap = this.load(stockSymbol);
		TreeMap<Date, Integer> resultMap = new TreeMap<>();
		statsMap.entrySet().stream().filter(e -> e.getKey().compareTo(start) >= 0 && e.getKey().compareTo(end) <= 0)
				.forEach(e -> resultMap.put(e.getKey(), e.getValue()));
		return resultMap;

	}
}
