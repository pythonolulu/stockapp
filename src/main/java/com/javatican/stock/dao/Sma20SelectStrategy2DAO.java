package com.javatican.stock.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;

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
@Repository("sma20SelectStrategy2DAO")
public class Sma20SelectStrategy2DAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RESOURCE_FILE_PATH = "file:./strategy/2/%s.json";
	@Autowired
	private ResourceLoader resourceLoader;
	// @Autowired
	// private ResourcePatternResolver resourcePatternResolver;

	static {
		objectMapper.setDateFormat(df);
	}

	public Sma20SelectStrategy2DAO() {
	}

	public boolean existsForStatsData(String dateString) {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, dateString));
		return resource.exists();
	}

	public void saveStatsData(String dateString, LinkedHashMap<String, List<Number>> statsMap)
			throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, dateString));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, statsMap);
			logger.info("Finish saving sma20 select strategy '2' data of date: " + dateString);
		} catch (Exception ex) {
			logger.warn("Errror while trying to save sma20 select strategy '2' data of date: " + dateString);
			throw new StockException(ex);
		}
	}

	public LinkedHashMap<String, List<Number>> loadStatsData(String dateString) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, dateString));
		try (InputStream st = resource.getInputStream();) {
			LinkedHashMap<String, List<Number>> statsMap = objectMapper.readValue(st,
					new TypeReference<LinkedHashMap<String, List<Number>>>() {
					});
			return statsMap;
		} catch (Exception ex) {
			logger.warn("Errror while trying to load sma20 select strategy '2' data of date: " + dateString);
			throw new StockException(ex);
		}
	}

}
