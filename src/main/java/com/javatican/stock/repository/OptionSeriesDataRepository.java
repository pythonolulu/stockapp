package com.javatican.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.javatican.stock.model.OptionSeriesData;

public interface OptionSeriesDataRepository extends JpaRepository<OptionSeriesData, Long> {
	boolean existsByTradingDate(Date tradingDate);

	OptionSeriesData getByTradingDate(Date tradingDate);

	List<OptionSeriesData> findAllByOrderByTradingDate();

	@Query("select max(s.tradingDate) from OptionSeriesData s")
	Date getLatestTradingDate();

	@Query("select distinct s.strikePrice from OptionSeriesData s where s.callOption=1 and s.strikePrice>=?1 and s.strikePrice<=?2 order by s.strikePrice asc")
	List<Integer> findCallOptionStrikePriceBetween(Integer low, Integer high);

	@Query("select distinct s.strikePrice from OptionSeriesData s where s.callOption=0 and s.strikePrice>=?1 and s.strikePrice<=?2 order by s.strikePrice asc")
	List<Integer> findPutOptionStrikePriceBetween(Integer low, Integer high);

	@Query("select s from OptionSeriesData s where s.callOption=?1 and s.strikePrice=?2 and s.weekOption=?3 and s.currentMonthOption=?4 and s.nextMonthOption=?5 order by s.tradingDate asc")
	List<OptionSeriesData> customQuery(boolean callOption, Integer strikePrice, boolean weekOption,
			boolean currentMonthOption, boolean nextMonthOption);
	
	@Query("select s from OptionSeriesData s where s.callOption=?1 and s.strikePrice=?2 and s.weekOption=?3 and s.currentMonthOption=?4 and s.nextMonthOption=?5 and s.tradingDate>=?6 order by s.tradingDate asc")
	List<OptionSeriesData> customQueryDateSince(boolean callOption, Integer strikePrice, boolean weekOption,
			boolean currentMonthOption, boolean nextMonthOption, Date dateSince);
}
