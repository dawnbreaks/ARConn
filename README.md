ARConn
========

A simple ORM framework, support database sharding. Do you agree that  Hibernate suck, too complex and not support database sharding? If yes,  ARConn is your best alternative.


Features
========

  * Simple, small code base, easy to learn API
  * Active Record design pattern
  * database sharding, ARconn simplify multi-database access.
  * reuse connections when you access different db on the same mysql instance.
Example
========
```java
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
			List<TblMail> mailList2 = conn.queryAll(TblMail.class, " sender = 308181687@qq.com ");
		}finally{
			conn.close();
		}
```
Build
========

To build the JAR file of NettyRPC, you need to install Maven (http://maven.apache.org), then type the following command:

    $ mvn package

To generate project files (.project, .classpath) for Eclipse, do

    $ mvn eclipse:eclipse

then import the folder from your Eclipse.


========
Please feel free to contact me(2005dawnbreaks@gmail.com) if you have any questions.
