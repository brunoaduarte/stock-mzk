package mzk.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Product {

	private static final AtomicInteger COUNTER = new AtomicInteger();
	private int id;
	private String name;
	private Long barCode;
	private Integer serialNumber;
	private boolean sold;
	
	public Product() {
		super();
		this.id = COUNTER.getAndIncrement();
	}

	public Product(String name, Long barCode, Integer serialNumber, boolean sold) {
		super();
		this.id = COUNTER.getAndIncrement();
		this.name = name;
		this.barCode = barCode;
		this.serialNumber = serialNumber;
		this.sold = sold;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getBarCode() {
		return barCode;
	}

	public void setBarCode(Long barCode) {
		this.barCode = barCode;
	}

	public Integer getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(Integer serialNumber) {
		this.serialNumber = serialNumber;
	}

	public int getId() {
		return id;
	}

	public boolean isSold() {
		return sold;
	}

	public void setSold(boolean sold) {
		this.sold = sold;
	}

}
