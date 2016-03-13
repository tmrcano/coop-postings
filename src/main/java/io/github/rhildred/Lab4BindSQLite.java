package io.github.rhildred;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Lab4BindSQLite {
	public static void main(String[] args) {
		//set these to be null so that we can finally close them
        Connection connection = null;
        PreparedStatement oStmt = null;
        try{
        	//make a stmt from my SQL
        	connection = OpenShiftSQLiteSource.getConnection();
        	String sSQL = "SELECT * FROM PERSON WHERE id = ?";
        	oStmt = connection.prepareStatement(sSQL);
        	oStmt.setInt(1, 3);
        	ResultSet oRs = oStmt.executeQuery();
        	System.out.println(ResultSetValue.toJsonString(oRs));
            oRs.close();
        }catch(Exception e){
        	e.printStackTrace();
        }finally{
        	try{
        		if(oStmt != null) oStmt.close();
        		if(connection != null) connection.close();
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
	}
}
