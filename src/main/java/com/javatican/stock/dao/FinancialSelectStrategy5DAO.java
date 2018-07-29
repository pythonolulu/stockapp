package com.javatican.stock.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

@Repository("financialSelectStrategy5DAO")
public class FinancialSelectStrategy5DAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RAW_STATS_RESOURCE_FILE_PATH = "file:./strategy/5_raw/%s.json";
	private static final String STATS_RESOURCE_FILE_PATH = "file:./strategy/5/%s_%s.json";
	@Autowired
	private ResourceLoader resourceLoader;

	static {
		objectMapper.setDateFormat(df);
	}

	public FinancialSelectStrategy5DAO() {
	}

	public boolean statsDataExistsFor(int year, int period) {
		Resource resource = resourceLoader.getResource(String.format(STATS_RESOURCE_FILE_PATH, year, period));
		return resource.exists();
	}

	public void saveStatsData(int year, int period, Map<String, Map<String, List<Number>>> statsMap) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(STATS_RESOURCE_FILE_PATH, year, period));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, statsMap);
			logger.info("Finish saving stats data of year: " + year + ", period=" + period);
		} catch (Exception ex) {
			logger.warn("Errror while trying to save stats data of year: " + year + ", period=" + period);
			throw new StockException(ex);
		}
	}

	public Map<String, Map<String, List<Number>>> loadStatsData(int year, int period) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(STATS_RESOURCE_FILE_PATH, year, period));
		try (InputStream st = resource.getInputStream();) {
			Map<String, Map<String, List<Number>>> statsMap = objectMapper.readValue(st, new TypeReference<Map<String, Map<String, List<Number>>>>() {
			});
			return statsMap;
		} catch (Exception ex) {
			logger.warn("Errror while trying to load stats data of year: " + year + ", period=" + period);
			throw new StockException(ex);
		}
	}

	public void saveRawStatsData(String stockSymbol, TreeMap<Integer, List<Number>> rawStatsMap) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RAW_STATS_RESOURCE_FILE_PATH, stockSymbol));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, rawStatsMap);
			logger.info("Finish saving raw stats data for stock:" + stockSymbol);
		} catch (Exception ex) {
			logger.warn("Errror while trying to save raw stats data for stock:" + stockSymbol);
			throw new StockException(ex);
		}
	}

	public TreeMap<Integer, List<Number>> loadRawStatsData(String stockSymbol) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RAW_STATS_RESOURCE_FILE_PATH, stockSymbol));
		try (InputStream st = resource.getInputStream();) {
			TreeMap<Integer, List<Number>> rawStatsMap = objectMapper.readValue(st, new TypeReference<TreeMap<Integer, List<Number>>>() {
			});
			return rawStatsMap;
		} catch (Exception ex) {
			logger.warn("Errror while trying to save raw stats data for stock:" + stockSymbol);
			throw new StockException(ex);
		}
	}

}
