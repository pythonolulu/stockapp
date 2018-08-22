package com.javatican.stock.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

@Repository("indexStrategyDAO")
public class IndexStrategyDAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String SMA_STATS_RESOURCE_FILE_PATH = "file:./strategy/index/sma.json";
	@Autowired
	private ResourceLoader resourceLoader;
	static {
		objectMapper.setDateFormat(df);
	}

	public IndexStrategyDAO() {
	}

	public boolean smaStatsDataExistsForDate() {
		Resource resource = resourceLoader.getResource(SMA_STATS_RESOURCE_FILE_PATH);
		return resource.exists();
	}

	public void saveSmaStatsData(Map<Date, List<Number>> statsMap) throws StockException {
		Resource resource = resourceLoader.getResource(SMA_STATS_RESOURCE_FILE_PATH);
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, statsMap);
			logger.info("Finish saving index sma stats data");
		} catch (Exception ex) {
			logger.warn("Errror while trying to save index sma stats data ");
			throw new StockException(ex);
		}
	}

	public Map<Date, List<Number>> loadSmaStatsData() throws StockException {
		Resource resource = resourceLoader.getResource(SMA_STATS_RESOURCE_FILE_PATH);
		try (InputStream st = resource.getInputStream();) {
			Map<Date, List<Number>> statsMap = objectMapper.readValue(st,
					new TypeReference<Map<Date, List<Number>>>() {
					});
			return statsMap;
		} catch (Exception ex) {
			logger.warn("Errror while trying to load index sma stats data ");
			throw new StockException(ex);
		}
	}

}
