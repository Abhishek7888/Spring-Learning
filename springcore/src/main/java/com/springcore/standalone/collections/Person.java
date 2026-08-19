package com.springcore.standalone.collections;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Person {

	private List<String> friend;
	private Map<String,Integer> feesStructure;
	private Properties properties;

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Map<String, Integer> getFeesStructure() {
		return feesStructure;
	}

	public void setFeesStructure(Map<String, Integer> feesStructure) {
		this.feesStructure = feesStructure;
	}

	public Person() {
		super();
		// TODO Auto-generated constructor stub
	}

	public List<String> getFriend() {
		return friend;
	}

	public void setFriend(List<String> friend) {
		this.friend = friend;
	}

	@Override
	public String toString() {
		return "Person [friend=" + friend + ", feesStructure=" + feesStructure + "]";
	}

}
