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

@Repository("priceBreakUpSelectStrategy4DAO")
public class PriceBreakUpSelectStrategy4DAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RAW_STATS_RESOURCE_FILE_PATH = "file:./strategy/4_raw/breakup/%s.json";
	private static final String STATS_RESOURCE_FILE_PATH = "file:./strategy/4/breakup/%s.json";
	@Autowired
	private ResourceLoader resourceLoader;

	static {
		objectMapper.setDateFormat(df);
	}

	public PriceBreakUpSelectStrategy4DAO() {
	}

	public boolean statsDataExistsForDate(String dateString) {
		Resource resource = resourceLoader.getResource(String.format(STATS_RESOURCE_FILE_PATH, dateString));
		return resource.exists();
	}

	public void saveStatsData(String dateString, Map<String, Integer> statsMap) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(STATS_RESOURCE_FILE_PATH, dateString));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, statsMap);
			logger.info("Finish saving stats data of date: " + dateString);
		} catch (Exception ex) {
			logger.warn("Errror while trying to save stats data of date: " + dateString);
			throw new StockException(ex);
		}
	}

	public Map<String, Integer> loadStatsData(String dateString) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(STATS_RESOURCE_FILE_PATH, dateString));
		try (InputStream st = resource.getInputStream();) {
			Map<String, Integer> statsMap = objectMapper.readValue(st, new TypeReference<Map<String, Integer>>() {
			});
			return statsMap;
		} catch (Exception ex) {
			logger.warn("Errror while trying to load stats data of date: " + dateString);
			throw new StockException(ex);
		}
	}

	public void saveRawStatsData(String stockSymbol, TreeMap<Date, List<Number>> rawStatsMap) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RAW_STATS_RESOURCE_FILE_PATH, stockSymbol));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, rawStatsMap);
			logger.info("Finish saving raw stats data for stock:" + stockSymbol);
		} catch (Exception ex) {
			logger.warn("Errror while trying to save raw stats data for stock:" + stockSymbol);
			throw new StockException(ex);
		}
	}

	public TreeMap<Date, List<Number>> loadRawStatsData(String stockSymbol) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RAW_STATS_RESOURCE_FILE_PATH, stockSymbol));
		try (InputStream st = resource.getInputStream();) {
			TreeMap<Date, List<Number>> rawStatsMap = objectMapper.readValue(st,
					new TypeReference<TreeMap<Date, List<Number>>>() {
					});
			return rawStatsMap;
		} catch (Exception ex) {
			logger.warn("Errror while trying to save raw stats data for stock:" + stockSymbol);
			throw new StockException(ex);
		}
	}

	public TreeMap<Date, List<Number>> loadRawStatsDataBetweenDate(String stockSymbol, Date start, Date end)
			throws StockException {
		TreeMap<Date, List<Number>> rawStatsMap = this.loadRawStatsData(stockSymbol);
		TreeMap<Date, List<Number>> filteredMap = new TreeMap<>();
		rawStatsMap.entrySet().stream().filter(e -> e.getKey().compareTo(start) >= 0 && e.getKey().compareTo(end) <= 0)
				.forEach(e -> filteredMap.put(e.getKey(), e.getValue()));
		return filteredMap;

	}
}
