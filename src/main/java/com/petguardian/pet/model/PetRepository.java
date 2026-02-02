package com.petguardian.pet.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PetRepository {

	private final JdbcTemplate jdbcTemplate;

	public PetRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// ===== SQL =====
	private static final String INSERT = "INSERT INTO PET (MEM_ID, TYPE_ID, SIZE_ID, PET_NAME, PET_GENDER, PET_AGE, PET_DESCRIPTION, PET_IMAGE) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE = "UPDATE PET SET MEM_ID=?, TYPE_ID=?, SIZE_ID=?, PET_NAME=?, PET_GENDER=?, PET_AGE=?, PET_DESCRIPTION=?, PET_IMAGE=? WHERE PET_ID=?";

	private static final String DELETE = "DELETE FROM PET WHERE PET_ID=?";

	private static final String GET_ALL = "SELECT p.*, pt.TYPE_NAME, ps.SIZE_NAME " + "FROM PET p "
			+ "LEFT JOIN PET_TYPE pt ON p.TYPE_ID = pt.TYPE_ID " + "LEFT JOIN PET_SIZE ps ON p.SIZE_ID = ps.SIZE_ID "
			+ "ORDER BY p.CREATED_TIME DESC";

	private static final String GET_BY_NAME = "SELECT p.*, pt.TYPE_NAME, ps.SIZE_NAME " + "FROM PET p "
			+ "LEFT JOIN PET_TYPE pt ON p.TYPE_ID = pt.TYPE_ID " + "LEFT JOIN PET_SIZE ps ON p.SIZE_ID = ps.SIZE_ID "
			+ "WHERE p.PET_NAME LIKE ?";

	private static final String GET_BY_MEM_ID = "SELECT p.* FROM PET p WHERE p.MEM_ID = ? ORDER BY p.CREATED_TIME DESC";

	// ===== CRUD =====

	public void insert(PetVO pet) {
		jdbcTemplate.update(INSERT, pet.getMemId(), pet.getTypeId(), pet.getSizeId(), pet.getPetName(),
				pet.getPetGender(), pet.getPetAge(), pet.getPetDescription(), pet.getPetImage());
	}

	public void update(PetVO pet) {
		jdbcTemplate.update(UPDATE, pet.getMemId(), pet.getTypeId(), pet.getSizeId(), pet.getPetName(),
				pet.getPetGender(), pet.getPetAge(), pet.getPetDescription(), pet.getPetImage(), pet.getPetId());
	}

	public void delete(Integer petId) {
		jdbcTemplate.update(DELETE, petId);
	}

	public List<PetVO> getAll() {
		return jdbcTemplate.query(GET_ALL, petRowMapper);
	}

	public List<PetVO> getByName(String petName) {
		return jdbcTemplate.query(GET_BY_NAME, petRowMapper, "%" + petName + "%");
	}

	// ===== RowMapper =====
	private final RowMapper<PetVO> petRowMapper = new RowMapper<PetVO>() {
		@Override
		public PetVO mapRow(ResultSet rs, int rowNum) throws SQLException {
			PetVO pet = new PetVO();
			pet.setPetId(rs.getInt("PET_ID"));
			pet.setMemId(rs.getInt("MEM_ID"));
			pet.setTypeId(rs.getInt("TYPE_ID"));
			pet.setSizeId(rs.getInt("SIZE_ID"));
			pet.setPetName(rs.getString("PET_NAME"));
			pet.setPetGender(rs.getInt("PET_GENDER"));
			pet.setPetAge(rs.getObject("PET_AGE", Integer.class));
			pet.setPetDescription(rs.getString("PET_DESCRIPTION"));
			pet.setPetImage(rs.getBytes("PET_IMAGE"));
			pet.setCreatedTime(rs.getObject("CREATED_TIME", java.time.LocalDateTime.class));
			pet.setUpdatedAt(rs.getObject("UPDATED_AT", java.time.LocalDateTime.class));

			if (hasColumn(rs, "CREATED_TIME")) {
				pet.setCreatedTime(rs.getObject("CREATED_TIME", java.time.LocalDateTime.class));
			}

			if (hasColumn(rs, "UPDATED_AT")) {
				pet.setUpdatedAt(rs.getObject("UPDATED_AT", java.time.LocalDateTime.class));
			}

			return pet;
		}
	};

	private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		java.sql.ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		for (int x = 1; x <= columns; x++) {
			if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
				return true;
			}
		}
		return false;
	}

	public Optional<PetVO> findByPrimaryKey(Integer petId) {
		String sql = "SELECT p.*, pt.TYPE_NAME, ps.SIZE_NAME " + "FROM PET p "
				+ "LEFT JOIN PET_TYPE pt ON p.TYPE_ID = pt.TYPE_ID "
				+ "LEFT JOIN PET_SIZE ps ON p.SIZE_ID = ps.SIZE_ID " + "WHERE p.PET_ID = ?";
		List<PetVO> list = jdbcTemplate.query(sql, petRowMapper, petId);
		return list.stream().findFirst();
	}

	public List<PetVO> findByMemId(Integer memId) {
		// 使用你原本就寫好的 petRowMapper
		return jdbcTemplate.query(GET_BY_MEM_ID, petRowMapper, memId);
	}
}