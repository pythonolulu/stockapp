package com.javatican.stock.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.javatican.stock.model.StockItemWeeklyData;

@Repository("stockItemWeeklyDataDAO")
public class StockItemWeeklyDataDAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RESOURCE_FILE_PATH = "file:./download/stats/weekly/%s_stats.json";
	@Autowired
	private ResourceLoader resourceLoader;

	static {
		objectMapper.setDateFormat(df);
	}

	public StockItemWeeklyDataDAO() {
	}

	public void save(String stockSymbol, List<StockItemWeeklyData> sidList) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, sidList);
			logger.info("Finish saving stock stats weekly data for stock:" + stockSymbol);
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<StockItemWeeklyData> load(String stockSymbol) throws StockException {
		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, stockSymbol));
		try (InputStream st = resource.getInputStream();) {
			List<StockItemWeeklyData> sidList = objectMapper.readValue(st, new TypeReference<List<StockItemWeeklyData>>() {
			});
			return sidList;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}
}