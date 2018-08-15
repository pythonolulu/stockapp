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

@Repository("smaSelectStrategy2DAO")
public class SmaSelectStrategy2DAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RAW_STATS_RESOURCE_FILE_PATH = "file:./strategy/2_raw/%s.json";
	private static final String STATS_RESOURCE_FILE_PATH = "file:./strategy/2/%s_%s.json";
	@Autowired
	private ResourceLoader resourceLoader;

	static {
		objectMapper.setDateFormat(df);
	}

	public SmaSelectStrategy2DAO() {
	}

	public boolean statsDataExistsForDate(String dateString, String type) {
		Resource resource = resourceLoader.getResource(String.format(STATS_RESOURCE_FILE_PATH, dateString, type));
		return resource.exists();
	}

	public void saveStatsData(String dateString, String type, Map<String, List<Number>> statsMap)
			throws StockException {
		Resource resource = resourceLoader.getResource(String.format(STATS_RESOURCE_FILE_PATH, dateString, type));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, statsMap);
			logger.info("Finish saving stats data of date: " + dateString + " and type: " + type);
		} catch (Exception ex) {
			logger.warn("Errror while trying to save stats data of date: " + dateString + " and type: " + type);
			throw new StockException(ex);
		}
	}

	public Map<String, List<Number>> loadStatsData(String dateString, String type) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(STATS_RESOURCE_FILE_PATH, dateString, type));
		try (InputStream st = resource.getInputStream();) {
			Map<String, List<Number>> statsMap = objectMapper.readValue(st,
					new TypeReference<Map<String, List<Number>>>() {
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
