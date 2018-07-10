package com.javatican.stock.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.javatican.stock.model.StockPriceChange;
import com.javatican.stock.util.StockUtils;

@Repository("stockPriceChangeDAO")
public class StockPriceChangeDAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String TOP_RESOURCE_FILE_PATH = "file:./download/change/%s_top.json";
	private static final String BOTTOM_RESOURCE_FILE_PATH = "file:./download/change/%s_bottom.json";
	@Autowired
	private ResourceLoader resourceLoader;

	static {
		objectMapper.setDateFormat(df);
	}

	public StockPriceChangeDAO() {
	}

	public void saveTop(String dateString, List<StockPriceChange> spcList) throws StockException {
		save(dateString, spcList, true);
	}

	public void saveBottom(String dateString, List<StockPriceChange> spcList) throws StockException {
		save(dateString, spcList, false);
	}

	private void save(String dateString, List<StockPriceChange> spcList, boolean isTop) throws StockException {
		String filePath = isTop ? TOP_RESOURCE_FILE_PATH : BOTTOM_RESOURCE_FILE_PATH;
		Resource resource = resourceLoader.getResource(String.format(filePath, dateString));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, spcList);
			logger.info("Finish saving stock price change for date:" + dateString);
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<StockPriceChange> loadTop(Date tradingDate) throws StockException {
		return load(tradingDate, true);
	}

	public List<StockPriceChange> loadBottom(Date tradingDate) throws StockException {
		return load(tradingDate, false);
	}

	private List<StockPriceChange> load(Date tradingDate, boolean isTop) throws StockException {
		String filePath = isTop ? TOP_RESOURCE_FILE_PATH : BOTTOM_RESOURCE_FILE_PATH;
		Resource resource = resourceLoader
				.getResource(String.format(filePath, StockUtils.dateToSimpleString(tradingDate)));
		try (InputStream st = resource.getInputStream();) {
			List<StockPriceChange> spcList = objectMapper.readValue(st, new TypeReference<List<StockPriceChange>>() {
			});
			return spcList;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}
}