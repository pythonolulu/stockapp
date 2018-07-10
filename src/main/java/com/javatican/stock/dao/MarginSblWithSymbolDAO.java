package com.javatican.stock.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatican.stock.StockException;
import com.javatican.stock.model.MarginSblWithSymbol;
import com.javatican.stock.util.StockUtils;

@Repository("marginSblWithSymbolDAO")
public class MarginSblWithSymbolDAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String RESOURCE_FILE_PATH = "file:./download/margin_sbl/%s.json";
	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private ResourcePatternResolver resourcePatternResolver;

	static {
		objectMapper.setDateFormat(df);
	}

	public MarginSblWithSymbolDAO() {
	}

	public Date getLatestTradingDate() throws StockException {
		try {
			Resource[] resources = resourcePatternResolver.getResources("file:./download/margin_sbl/*.json");
			List<String> dateStrList = new ArrayList<>();
			for (Resource r : resources) {
				String filename = r.getFilename();
				dateStrList.add(filename.substring(0, filename.indexOf(".")));
			}
			return StockUtils
					.stringSimpleToDate(dateStrList.stream().sorted(Comparator.reverseOrder()).findFirst().get()).get();
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public Pair<Date, Date> getDataDateRange() throws StockException {
		try {
			Resource[] resources = resourcePatternResolver.getResources("file:./download/margin_sbl/*.json");
			List<String> dateStrList = new ArrayList<>();
			for (Resource r : resources) {
				String filename = r.getFilename();
				dateStrList.add(filename.substring(0, filename.indexOf(".")));
			}
			List<String> dateList = dateStrList.stream().sorted().collect(Collectors.toList());
			return Pair.with(StockUtils.stringSimpleToDate(dateList.get(0)).get(),
					StockUtils.stringSimpleToDate(dateList.get(dateList.size() - 1)).get());

		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<Date> getDataDateList() throws StockException {
		try {
			Resource[] resources = resourcePatternResolver.getResources("file:./download/margin_sbl/*.json");
			List<String> dateStrList = new ArrayList<>();
			for (Resource r : resources) {
				String filename = r.getFilename();
				dateStrList.add(filename.substring(0, filename.indexOf(".")));
			}
			List<Date> dateList = dateStrList.stream().sorted()
					.map(dateString -> StockUtils.stringSimpleToDate(dateString).get()).collect(Collectors.toList());
			return dateList;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public void save(String dateString, Collection<MarginSblWithSymbol> mswsList) throws StockException {

		Resource resource = resourceLoader.getResource(String.format(RESOURCE_FILE_PATH, dateString));
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, mswsList);
			logger.info("Finish saving margin and sbl data for date:" + dateString);
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<MarginSblWithSymbol> load(Date tradingDate) throws StockException {

		Resource resource = resourceLoader
				.getResource(String.format(RESOURCE_FILE_PATH, StockUtils.dateToSimpleString(tradingDate)));
		try (InputStream st = resource.getInputStream();) {
			List<MarginSblWithSymbol> mswsList = objectMapper.readValue(st,
					new TypeReference<List<MarginSblWithSymbol>>() {
					});
			return mswsList;
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}
}