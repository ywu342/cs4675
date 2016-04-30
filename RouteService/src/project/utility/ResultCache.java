package project.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import project.utility.RouteBoxer.LatLng;
import project.utility.RouteBoxer.LatLngBounds;

public class ResultCache {
	static Connection conn = null;
	static PreparedStatement stmt = null;
	public static void initial(){
		DBConnector.init_setting();
		conn = DBConnector.getConnection();
	}
	public static boolean updatePreviousResultTable(String origin_address, String destination_address, String result){
		// update PreviousResult table
		try{
			stmt=conn.prepareStatement("INSERT INTO PreviousResult(origin_address,destination_address,result) VALUES(?,?,?) ON DUPLICATE KEY UPDATE result=?");
			stmt.setString(1, origin_address);
			stmt.setString(2, destination_address);
			stmt.setString(3,result);
			stmt.setString(4,result);
			return stmt.execute();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static boolean updateFuzzyPreviousResultTable(LatLngBounds startBox, LatLngBounds endBox, String result){
		//update FuzzyPreviousResult tables
		try{
			stmt=conn.prepareStatement("INSERT INTO FuzzyPreviousResult(origin_swlatbounds,origin_nelatbounds,origin_swlngbounds,origin_nelngbounds,destination_swlatbounds,destination_nelatbounds,destination_swlngbounds,destination_nelngbounds,result) VALUES(?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE result=?");
			stmt.setDouble(1, startBox.getSouthWest().lat());
			stmt.setDouble(2, startBox.getNorthEast().lat());
			stmt.setDouble(3,startBox.getSouthWest().lng());
			stmt.setDouble(4,startBox.getNorthEast().lng());
			stmt.setDouble(5, endBox.getSouthWest().lat());
			stmt.setDouble(6, endBox.getNorthEast().lat());
			stmt.setDouble(7,endBox.getSouthWest().lng());
			stmt.setDouble(8,endBox.getNorthEast().lng());
			stmt.setString(9,result);
			stmt.setString(10,result);
			return stmt.execute();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static String getPreviousResult(String origin_address, String destination_address){
		try{
			stmt=conn.prepareStatement("SELECT result FROM PreviousResult WHERE origin_address=? and destination_address=?");
			stmt.setString(1, origin_address);
			stmt.setString(2, destination_address);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getString("result");
			}
			return "NO RESULT";
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "ERROR";
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static String getFuzzyCenterPreviousResult(LatLng startBoxCenter, LatLng endBoxCenter){
		//update FuzzyPreviousResult tables
		try{
			stmt=conn.prepareStatement("SELECT result FROM FuzzyPreviousResult WHERE origin_swlatbounds < ? and origin_nelatbounds > ? and origin_swlngbounds < ? and origin_nelngbounds > ? and destination_swlatbounds < ? and destination_nelatbounds > ? and destination_swlngbounds < ? and destination_nelngbounds > ?");
//			stmt.setDouble(1, startBox.getSouthWest().lat());
//			stmt.setDouble(2, startBox.getNorthEast().lat());
//			stmt.setDouble(3,startBox.getSouthWest().lng());
//			stmt.setDouble(4,startBox.getNorthEast().lng());
//			stmt.setDouble(5, endBox.getSouthWest().lat());
//			stmt.setDouble(6, endBox.getNorthEast().lat());
//			stmt.setDouble(7,endBox.getSouthWest().lng());
//			stmt.setDouble(8,endBox.getNorthEast().lng());
			stmt.setDouble(1, startBoxCenter.lat());
			stmt.setDouble(2, startBoxCenter.lat());
			stmt.setDouble(3,startBoxCenter.lng());
			stmt.setDouble(4,startBoxCenter.lng());
			stmt.setDouble(5, endBoxCenter.lat());
			stmt.setDouble(6, endBoxCenter.lat());
			stmt.setDouble(7,endBoxCenter.lng());
			stmt.setDouble(8,endBoxCenter.lng());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getString("result");
			} 
			return "NO RESULT";
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "ERROR";
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static String getFuzzyBoundsPreviousResult(LatLngBounds startBox, LatLngBounds endBox){
		//update FuzzyPreviousResult tables
		try{
			stmt=conn.prepareStatement("SELECT result FROM FuzzyPreviousResult WHERE origin_swlatbounds < ? and origin_nelatbounds > ? and origin_swlngbounds < ? and origin_nelngbounds > ? and destination_swlatbounds < ? and destination_nelatbounds > ? and destination_swlngbounds < ? and destination_nelngbounds > ?");
			stmt.setDouble(1, startBox.getSouthWest().lat());
			stmt.setDouble(2, startBox.getNorthEast().lat());
			stmt.setDouble(3,startBox.getSouthWest().lng());
			stmt.setDouble(4,startBox.getNorthEast().lng());
			stmt.setDouble(5, endBox.getSouthWest().lat());
			stmt.setDouble(6, endBox.getNorthEast().lat());
			stmt.setDouble(7,endBox.getSouthWest().lng());
			stmt.setDouble(8,endBox.getNorthEast().lng());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getString("result");
			} 
			return "NO RESULT";
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "ERROR";
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	

}
