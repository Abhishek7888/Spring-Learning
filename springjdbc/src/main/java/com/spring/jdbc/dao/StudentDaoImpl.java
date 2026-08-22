package com.spring.jdbc.dao;

import org.springframework.jdbc.core.JdbcTemplate;

import com.spring.jdbc.entities.Student;

public class StudentDaoImpl implements StudentDao {

	private JdbcTemplate template;

	public JdbcTemplate getTemplate() {
		return template;
	}

	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	// inserting Data
	@Override
	public int insert(Student student) {
		// TODO Auto-generated method stub
		String query = "insert into student(id,name,city) values(?,?,?)";
		int update = this.template.update(query, student.getId(), student.getName(), student.getCity());

		return update;
	}

//	Updating Data
	@Override
	public int change(Student student) {
		// TODO Auto-generated method stub
		String query = "update student set name = ?,city=? where id = ?";
		int update = this.template.update(query, student.getName(), student.getCity(), student.getId());
		return update;
	}

	@Override
	public int delete(int studentid) {
		String query = "delete from student where id = ?";
		int update = this.template.update(query, studentid);

		return update;
	}

}
