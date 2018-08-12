package com.javatican.stock.util;

public class ResponseMessage {
	private String text;
	private String category;
	public ResponseMessage() {
		super();
	}
	public ResponseMessage(String text, String category) {
		super();
		this.text = text;
		this.category = category;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	@Override
	public String toString() {
		return String.format("Category:%s, message:%s", this.category, this.text);
	}
}
