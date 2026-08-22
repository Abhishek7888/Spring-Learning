package com.spring.jdbc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.spring.jdbc.dao.StudentDao;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		System.out.println("Hello World!");
		// spring JDBC => JdbcTemplate
		ApplicationContext context = new ClassPathXmlApplicationContext("com/spring/jdbc/config.xml");
		JdbcTemplate bean = context.getBean("jdbcTemplate", JdbcTemplate.class);

		StudentDao bean2 = context.getBean("studentdao", StudentDao.class);
//	Insert
//		Student student = new Student();
//		student.setId(4);
//		student.setName("Sudhanshu");
//		student.setCity("Pune");
//
//		int insert = bean2.insert(student);
//
//		System.out.println("student added" + insert);
//	Update
//		Student student = new Student();
//		student.setId(2);
//		student.setName("Dip");
//		student.setCity("Mumbai");
//
//		int change = bean2.change(student);
//
//		System.out.println("data changed " + change);
// Delete
		int d = bean2.delete(2);
		System.out.println("student deleted" + d);
	}
}
