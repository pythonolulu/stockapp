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
import com.javatican.stock.util.StockUtils;

@Repository("callWarrantSelectStrategy1DAO")
public class CallWarrantSelectStrategy1DAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RAW_STATS_RESOURCE_FILE_PATH = "file:./strategy/1_raw/call/%s_%s.json";
	private static final String STATS_RESOURCE_FILE_PATH = "file:./strategy/1/call/%s_%s_%s.json";
	@Autowired
	private ResourceLoader resourceLoader;

	static {
		objectMapper.setDateFormat(df);
	}

	public CallWarrantSelectStrategy1DAO() {
	}

	public boolean statsDataExistsFor(String dateString, int holdPeriod, int dataDatePeriod) {
		Resource resource = resourceLoader
				.getResource(String.format(STATS_RESOURCE_FILE_PATH, dateString, holdPeriod, dataDatePeriod));
		return resource.exists();
	}

	public void saveStatsData(String dateString, int holdPeriod, int dataDatePeriod, Map<String, List<Number>> statsMap)
			throws StockException {
		Resource resource = resourceLoader
				.getResource(String.format(STATS_RESOURCE_FILE_PATH, dateString, holdPeriod, dataDatePeriod));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, statsMap);
			logger.info("Finish saving stats data of date:" + dateString + ", holdPeriod:" + holdPeriod
					+ ", dataDatePeriod:" + dataDatePeriod);
		} catch (Exception ex) {
			logger.warn("Errror while trying to save stats data of date" + dateString + ", holdPeriod:" + holdPeriod
					+ ", dataDatePeriod:" + dataDatePeriod);
			throw new StockException(ex);
		}
	}

	public Map<String, List<Number>> loadStatsData(String dateString, int holdPeriod, int dataDatePeriod)
			throws StockException {
		Resource resource = resourceLoader.getResource(String.format(STATS_RESOURCE_FILE_PATH, dateString, holdPeriod, dataDatePeriod));
		try (InputStream st = resource.getInputStream();) {
			Map<String, List<Number>>  statsMap = objectMapper.readValue(st,
					new TypeReference<Map<String, List<Number>>>() {
					});
			return statsMap;
		} catch (Exception ex) {
			logger.warn("Errror while trying to load stats data of date" + dateString + ", holdPeriod:" + holdPeriod
					+ ", dataDatePeriod:" + dataDatePeriod);
			throw new StockException(ex);
		}
	}

	public void saveRawStatsData(String stockSymbol, int holdPeriod, TreeMap<String, Double> rawStatsMap)
			throws StockException {
		Resource resource = resourceLoader
				.getResource(String.format(RAW_STATS_RESOURCE_FILE_PATH, stockSymbol, holdPeriod));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, rawStatsMap);
			logger.info("Finish saving raw stats data of holdPeriod: " + holdPeriod + " days for stock:" + stockSymbol);
		} catch (Exception ex) {
			logger.warn("Errror while trying to save raw stats data of holdPeriod: " + holdPeriod + " days for stock:"
					+ stockSymbol);
			throw new StockException(ex);
		}
	}

	public TreeMap<String, Double> loadRawStatsData(String stockSymbol, int holdPeriod) throws StockException {
		Resource resource = resourceLoader
				.getResource(String.format(RAW_STATS_RESOURCE_FILE_PATH, stockSymbol, holdPeriod));
		try (InputStream st = resource.getInputStream();) {
			TreeMap<String, Double> rawStatsMap = objectMapper.readValue(st,
					new TypeReference<TreeMap<String, Double>>() {
					});
			return rawStatsMap;
		} catch (Exception ex) {
			logger.warn("Errror while trying to load raw stats data of holdPeriod: " + holdPeriod + " days for stock:"
					+ stockSymbol);
			throw new StockException(ex);
		}
	}

	public TreeMap<String, Double> loadRawStatsDataBetweenDate(String stockSymbol, int holdPeriod, Date start, Date end)
			throws StockException {

		TreeMap<String, Double> rawStatsMap = this.loadRawStatsData(stockSymbol, holdPeriod);
		TreeMap<String, Double> filteredMap = new TreeMap<>();
		rawStatsMap.entrySet().stream()
				.filter(e -> StockUtils.stringSimpleToDate(e.getKey()).get().compareTo(start) >= 0
						&& StockUtils.stringSimpleToDate(e.getKey()).get().compareTo(end) <= 0)
				.forEach(e -> filteredMap.put(e.getKey(), e.getValue()));
		return filteredMap;

	}
}
