package project.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnector { 
//		private static Connection conn;
		private final static int limit = 10;
		private static int count = 0;
		private static Properties prop;
		public static void init_setting(){
			//load db properties from the file
			prop = new Properties();
			FileInputStream fis;
			try {
				fis = new FileInputStream(System.getProperty("com.sun.aas.instanceRoot")+"/config/config.properties");
				prop.load(fis);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public static Connection init() throws Exception{
			String driverName = "com.mysql.jdbc.Driver"; 
		    try {
		    	Class.forName(driverName).newInstance();;
		    } catch (ClassNotFoundException e) {
		    	e.printStackTrace();
		    }
		    Connection conn = DriverManager.getConnection("jdbc:mysql://"+prop.getProperty("host_name")+":3306/"+prop.getProperty("db_name"),prop.getProperty("db_user"), "");
		    count++;
		    return conn;
		}
		
		public static Connection getConnection(){
			try{
//				if(){
					return init();
//				}else{
//					throw new Exception("Too many connection exist");
//				}
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
}
