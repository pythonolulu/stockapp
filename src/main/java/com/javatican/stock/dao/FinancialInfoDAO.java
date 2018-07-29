package com.javatican.stock.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
import com.javatican.stock.model.FinancialInfo;
import com.javatican.stock.repository.FinancialInfoRepository;

@Repository("financialInfoDAO")
public class FinancialInfoDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	private static final String IGNORE_LIST_RESOURCE_FILE_PATH = "file:./settingData/ignoreListForFinancialInfoData.json";
	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	FinancialInfoRepository financialInfoRepository;

	static {
		objectMapper.setDateFormat(df);
	}

	public FinancialInfoDAO() {
	}

	public void saveIgnoreList(List<String> igList) throws StockException {
		Resource resource = resourceLoader.getResource(IGNORE_LIST_RESOURCE_FILE_PATH);
		try (OutputStream st = ((WritableResource) resource).getOutputStream()) {
			objectMapper.writeValue(st, igList);
			logger.info("Finish saving ignore list for financial info download.");
		} catch (Exception ex) {
			throw new StockException(ex);
		}
	}

	public List<String> loadIgnoreList() {
		Resource resource = resourceLoader.getResource(String.format(IGNORE_LIST_RESOURCE_FILE_PATH));
		try (InputStream st = resource.getInputStream();) {
			List<String> igList = objectMapper.readValue(st, new TypeReference<List<String>>() {
			});
			return igList;
		} catch (Exception ex) {
			return null;
		}
	}

	public void save(FinancialInfo financialInfo) {
		financialInfoRepository.save(financialInfo);
	}

	public void saveAll(Collection<FinancialInfo> list) {
		financialInfoRepository.saveAll(list);
	}

	public boolean existsByYearAndSeasonAndSymbol(int year, int season, String symbol) {
		return financialInfoRepository.existsByYearAndSeasonAndSymbol(year, season, symbol);
	}

	public FinancialInfo getByAuditDateAndSymbol(Date auditDate, String symbol) {
		return financialInfoRepository.getByAuditDateAndSymbol(auditDate, symbol);
	}

	public FinancialInfo getByYearAndSeasonAndSymbol(int year, int season, String symbol) {
		return financialInfoRepository.getByYearAndSeasonAndSymbol(year, season, symbol);
	}

	public List<FinancialInfo> getByYearAndSymbol(int year, String symbol) {
		return financialInfoRepository.getByYearAndSymbol(year, symbol);
	}

	public List<FinancialInfo> findBySymbol(String symbol) {
		return financialInfoRepository.findBySymbol(symbol);
	}

	public List<FinancialInfo> findAllSeason4BySymbol(String symbol) {
		return financialInfoRepository.findAllSeason4BySymbol(symbol);
	}

	public TreeMap<Integer, FinancialInfo> findAllSeason4BySymbolAsMap(String symbol) {
		List<FinancialInfo> fiList = financialInfoRepository.findAllSeason4BySymbol(symbol);
		TreeMap<Integer, FinancialInfo> fiMap = new TreeMap<>();
		fiList.stream().forEach(fi -> fiMap.put(fi.getYear(), fi));
		return fiMap;
	}

	public FinancialInfo getLastSeason4BySymbol(String symbol) {
		return financialInfoRepository.findTopBySymbolAndSeasonOrderByYearDesc(symbol, 4);
	}
}
