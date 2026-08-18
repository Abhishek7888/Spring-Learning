package com.springcore.ci;

import java.util.List;

public class Person {

	private int personId;
	private String name;
	private Obj obj;

	public Person(int personId, String name, Obj obj) {
		super();
		this.personId = personId;
		this.name = name;
		this.obj = obj;
	}

	@Override
	public String toString() {
		return "Person [personId=" + personId + ", Name=" + name + ", obj=" + obj + "]";
	}

	public Person() {
		super();
		// TODO Auto-generated constructor stub
	}

}
