package com.javatican.stock.dao;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.FutureData;
import com.javatican.stock.repository.FutureDataRepository;

@Repository("futureDataDAO")
public class FutureDataDAO {
	@Autowired
	FutureDataRepository futureDataRepository;

	public FutureDataDAO() {
	}

	public boolean existsByTradingDate(Date tradingDate) {
		return futureDataRepository.existsByTradingDate(tradingDate);
	}

	public FutureData getByTradingDate(Date tradingDate) {
		return futureDataRepository.getByTradingDate(tradingDate);
	}

	public List<FutureData> findAll() {
		return futureDataRepository.findAllByOrderByTradingDate();
	}

	public void save(FutureData futureData) {
		futureDataRepository.save(futureData);
	}

	public void saveAll(List<FutureData> fdList) {
		futureDataRepository.saveAll(fdList);
	}

}
