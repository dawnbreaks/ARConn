package com.lubin.orm.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.lubin.orm.ARConn;


public class Main {
	public static void main(String[] args) throws SQLException {
		
		Connection jdbcConn = fetchConntion();//create or fetch a jdbc connection
		String dbName = chooseDatabase();// choose the database
		ARConn conn = new ARConn( jdbcConn, dbName);
		try{
			TblMail mail = conn.create(TblMail.class);
			mail.setAttach_flag("1");
			mail.setFolder_id(0);
			mail.setSender("308181687@qq.com");
			mail.setMail_server_uuid("308181687@qq.com");
			mail.setReceiver("308181687@qq.com");
			mail.setCc("308181687@qq.com");
			mail.setBcc("308181687@qq.com");
			mail.setSubject("test");
			mail.setReply_to("308181687@qq.com");
			mail.insert();	//insert to db
			
			TblMail mail_ = conn.find(TblMail.class, 1L);  // find by primary key
			mail_.setBcc("xxx@xx.xx");
			mail_.update();  //update db
			
			List<TblMail> mailList1 = conn.findAll(TblMail.class, "sender", "308181687@qq.com");
			mailList1 = conn.queryAll(TblMail.class, " sender = 308181687@qq.com ");
			System.out.print(mailList1.size());
		}finally{
			conn.close();
		}
	}

	private static String chooseDatabase() {
		// TODO Auto-generated method stub
		return null;
	}

	private static Connection fetchConntion() {
		// TODO Auto-generated method stub
		return null;
	}
}
