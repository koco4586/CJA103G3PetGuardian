package com.forum.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ForumJDBCDAO implements ForumDAO_interface {
	
	static String driver = "com.mysql.cj.jdbc.Driver";
	String url = "jdbc:mysql://localhost:3306/test_db?serverTimezone=Asia/Taipei";
	String userid = "root";
	String passwd = "qw881010";
	
	private static final String INSERT_STMT =
			"INSERT INTO forum (forum_name, forum_pic) VALUES (?, ?)";
	private static final String GET_ONE_STMT =
			"SELECT * FROM forum WHERE forum_id = ?";
	private static final String UPDATE_STATUS_STMT =
			"UPDATE forum SET forum_status = ? WHERE forum_id = ?";
	private static final String UPDATE_STMT = 
			"UPDATE forum SET forum_name = ?, forum_pic = ? WHERE forum_id = ?";
	private static final String SEARCH_BY_FORUMNAME_STMT =
			"SELECT * FROM forum WHERE forum_status = 1 AND forum_name LIKE ? ORDER BY forum_id DESC";
	private static final String GET_ALL_STMT =
			"SELECT * FROM forum ORDER BY forum_id DESC";
	private static final String GET_ALL_ACTIVE_STMT =
			"SELECT * FROM forum WHERE forum_status = 1 ORDER BY forum_id DESC";
	private static final String GET_PICTURE =
			"SELECT forum_pic FROM forum WHERE forum_id = ?";
	
	static {
		try {
			Class.forName(driver);
			// Handle any driver errors
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Couldn't load database driver. " + e.getMessage());
		}	
	}
	
	
	@Override
	public void insert(ForumVO forumVO) {
		
		try(Connection con = DriverManager.getConnection(url, userid, passwd);
			PreparedStatement pstmt = con.prepareStatement(INSERT_STMT);){
			
			pstmt.setString(1, forumVO.getForumName());
			pstmt.setBytes(2, forumVO.getForumPic());
			
			pstmt.executeUpdate();
					
			// Handle any SQL errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
		}
		
	}

	@Override
	public void updateStatus(ForumVO forumVO) {
		
		try(Connection con = DriverManager.getConnection(url, userid, passwd);
			PreparedStatement pstmt = con.prepareStatement(UPDATE_STATUS_STMT);){
			
			pstmt.setInt(1, forumVO.getForumStatus());
			pstmt.setInt(2, forumVO.getForumId());
			
			pstmt.executeUpdate();
			
			// Handle any SQL errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
		}
		
	}
	
	@Override
	public void update(ForumVO forumVO) {
		
		try(Connection con = DriverManager.getConnection(url, userid, passwd);
			PreparedStatement pstmt = con.prepareStatement(UPDATE_STMT);){
			
			pstmt.setString(1, forumVO.getForumName());
			pstmt.setBytes(2, forumVO.getForumPic());
			pstmt.setInt(3, forumVO.getForumId());
			
			pstmt.executeUpdate();
			
		}catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
		}
		
	}
	
	@Override
	public List<ForumVO> searchByForumName(String forumName) {
		
		List<ForumVO> forumList = new ArrayList<ForumVO>();
			
		try(Connection con = DriverManager.getConnection(url, userid, passwd);
			PreparedStatement pstmt = con.prepareStatement(SEARCH_BY_FORUMNAME_STMT);){
			
			pstmt.setString(1, "%" + forumName + "%");
			
			try(ResultSet rs = pstmt.executeQuery();){
					
				while(rs.next()) {
					ForumVO forumVO = new ForumVO();
					forumVO.setForumId(rs.getInt("forum_id"));
					forumVO.setForumName(rs.getString("forum_name"));
					forumVO.setCreatedAt(rs.getTimestamp("created_at"));
					forumVO.setForumPic(rs.getBytes("forum_pic"));
					forumVO.setForumStatus(rs.getInt("forum_status"));
					forumList.add(forumVO);
				}
				
			}
				// Handle any SQL errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
		}
		return forumList;
	}


	@Override
	public List<ForumVO> getAll() {
		
		List<ForumVO> forumList = new ArrayList<ForumVO>();
		
		try(Connection con = DriverManager.getConnection(url, userid, passwd);
			PreparedStatement pstmt = con.prepareStatement(GET_ALL_STMT);
			ResultSet rs = pstmt.executeQuery();){
			
				while(rs.next()) {
					ForumVO forumVO = new ForumVO();
					forumVO.setForumId(rs.getInt("forum_id"));
					forumVO.setForumName(rs.getString("forum_name"));
					forumVO.setCreatedAt(rs.getTimestamp("created_at"));
					forumVO.setForumPic(rs.getBytes("forum_pic"));
					forumVO.setForumStatus(rs.getInt("forum_status"));
					forumList.add(forumVO);
				}
							
				// Handle any SQL errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
		}
		return forumList;
	}


	@Override
	public List<ForumVO> getAllActive() {
		
		List<ForumVO> forumList = new ArrayList<ForumVO>();
		
		try(Connection con = DriverManager.getConnection(url, userid, passwd);
			PreparedStatement pstmt = con.prepareStatement(GET_ALL_ACTIVE_STMT);
			ResultSet rs = pstmt.executeQuery();){
			
				while(rs.next()) {
					ForumVO forumVO = new ForumVO();
					forumVO.setForumId(rs.getInt("forum_id"));
					forumVO.setForumName(rs.getString("forum_name"));
					forumVO.setCreatedAt(rs.getTimestamp("created_at"));
					forumVO.setForumPic(rs.getBytes("forum_pic"));
					forumVO.setForumStatus(rs.getInt("forum_status"));
					forumList.add(forumVO);
				}
					
			// Handle any SQL errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
		}
		return forumList;
	}

	@Override
	public ForumVO findByPrimaryKey(Integer forumId) {
		
		ForumVO forumVO = new ForumVO();
		
		try(Connection con = DriverManager.getConnection(url, userid, passwd);
			PreparedStatement pstmt = con.prepareStatement(GET_ONE_STMT);){
			
			pstmt.setInt(1, forumId);
			
			try(ResultSet rs = pstmt.executeQuery();){
			
				if(rs.next()) {	
					forumVO.setForumId(rs.getInt("forum_id"));
					forumVO.setForumName(rs.getString("forum_name"));
					forumVO.setCreatedAt(rs.getTimestamp("created_at"));
					forumVO.setForumPic(rs.getBytes("forum_pic"));
					forumVO.setForumStatus(rs.getInt("forum_status"));
				}
			}
			// Handle any SQL errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
		}
		
		return forumVO;
	}

	@Override
	public byte[] getPicture(Integer forumId) {
		
		byte[] forumPic = null;

		try (Connection con = DriverManager.getConnection(url, userid, passwd);
			PreparedStatement pstmt = con.prepareStatement(GET_PICTURE);){

			pstmt.setInt(1, forumId);
			
			try(ResultSet rs = pstmt.executeQuery();){
				
				if(rs.next()) {	
					forumPic = rs.getBytes("forum_pic");	
				}
			}
			// Handle any SQL errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
		}

		return forumPic;
	}
	
}
