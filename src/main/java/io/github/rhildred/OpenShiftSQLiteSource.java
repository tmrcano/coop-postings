package io.github.rhildred;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OpenShiftSQLiteSource {
	public static Connection getConnection(){
		Connection connection = null;
		try{
			//create the connection to our derby database that is updated with the migrations
			Class.forName("org.sqlite.JDBC").newInstance();
			String sDir = "";
			if(System.getenv("OPENSHIFT_DATA_DIR") != null){
				sDir = System.getenv("OPENSHIFT_DATA_DIR");
			}
			String sUrl = String.format("jdbc:sqlite:%sMyDbTest", sDir);
			connection = DriverManager.getConnection( sUrl);
		}catch(Exception e){
			e.printStackTrace();
		}

		return connection;
	}
}
