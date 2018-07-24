package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.javatican.stock.model.CallWarrantTradeSummary;
import com.javatican.stock.model.FinancialInfo;
import com.javatican.stock.model.StockItem;

public interface FinancialInfoRepository extends JpaRepository<FinancialInfo, Long> {
	
	boolean existsByYearAndSeasonAndSymbol(int year, int season, String symbol);
	
	FinancialInfo getByAuditDateAndSymbol(Date auditDate, String symbol);

	FinancialInfo getByYearAndSeasonAndSymbol(int year, int season, String symbol);

	List<FinancialInfo> getByYearAndSymbol(int year, String symbol);

	List<FinancialInfo> findBySymbol(String symbol);

	@Query("select s from FinancialInfo s where s.symbol=?1 and s.season=4 order by s.year asc")
	List<FinancialInfo> findAllSeason4BySymbol(String symbol);
	
	FinancialInfo findTopBySymbolAndSeasonOrderByYearDesc(String symbol, int season);
}
