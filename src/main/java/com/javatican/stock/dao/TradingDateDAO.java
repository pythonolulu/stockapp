package com.javatican.stock.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.TradingDate;
import com.javatican.stock.repository.TradingDateRepository;
import com.javatican.stock.util.StockUtils;

@Repository("tradingDateDAO")
public class TradingDateDAO {
	public TradingDateDAO() {
	}

	@Autowired
	TradingDateRepository tradingDateRepository;

	public long count() {
		return tradingDateRepository.count();
	}

	public void save(TradingDate tradingDate) {
		tradingDateRepository.save(tradingDate);
	}

	public boolean existsByDate(Date date) {
		return tradingDateRepository.existsByDate(date);
	}

	public List<TradingDate> findAll() {
		return tradingDateRepository.findAll();
	}

	public List<Date> findAllTradingDate() {
		return tradingDateRepository.findAllTradingDate();
	}

	public List<Date> getAllFutureClosingDates() {
		List<Date> dList = tradingDateRepository.findAllTradingDate();
		return getAllFutureClosingDates(dList);
	}

	public List<Date> getAllFutureClosingDatesAfter(Date d) {
		List<Date> dList = tradingDateRepository.findAllTradingDateAfter(d);
		return getAllFutureClosingDates(dList);
	}

	private List<Date> getAllFutureClosingDates(List<Date> dList) {
		Map<String, Date> targetMap = new TreeMap<>();
		for (Date d : dList) {
			String yearMonth = StockUtils.getYearMonth(d);
			if (targetMap.get(yearMonth) != null) {
				continue;
			}
			if (StockUtils.isThirdWednesdayOfTheMonthOrAfter(d)) {
				targetMap.put(yearMonth, d);
			}
		}
		return new ArrayList<Date>(targetMap.values());
	}
	public List<TradingDate> findBetween(Date begin, Date end) {
		return tradingDateRepository.findByDateBetween(begin, end);
	}

	public List<Date> findDateByDateBetween(Date begin, Date end) {
		return tradingDateRepository.findDateByDateBetween(begin, end);
	}

	public List<TradingDate> findAfter(Date date) {
		return tradingDateRepository.findByDateAfter(date);
	}

	public Date getLatestTradingDate() {
		return tradingDateRepository.findTopByOrderByDateDesc().getDate();
	}

	public List<Date> findLatestNTradingDateDesc(int length) {
		// PageRequest constructor has been deprecated, and use of() method.
		Pageable pageable = PageRequest.of(0, length);
		return tradingDateRepository.findLatestNTradingDateDesc(pageable);
	}

	public List<Date> findLatestNTradingDateAsc(int length) {
		// PageRequest constructor has been deprecated, and use of() method.
		Pageable pageable = PageRequest.of(0, length);
		List<Date> dateList = tradingDateRepository.findLatestNTradingDateDesc(pageable);
		return dateList.stream().sorted().collect(Collectors.toList());
	}

}
