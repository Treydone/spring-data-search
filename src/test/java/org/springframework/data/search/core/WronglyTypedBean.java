package org.springframework.data.search.core;

import java.util.Date;

import org.springframework.data.search.annotation.Indexed;

public class WronglyTypedBean {

	@Indexed
	private int id;

	@Indexed
	private Date name;

	public WronglyTypedBean() {
	}

	public WronglyTypedBean(int id, Date name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getName() {
		return name;
	}

	public void setName(Date name) {
		this.name = name;
	}

}
