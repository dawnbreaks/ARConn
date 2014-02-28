package com.lubin.orm;

import org.hibernate.cfg.reveng.DelegatingReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.TableIdentifier;

public class MyStrategy extends DelegatingReverseEngineeringStrategy {

	public MyStrategy(ReverseEngineeringStrategy delegate) {
		super(delegate);
	}
	
	
	 public String columnToPropertyName(TableIdentifier table, String columnName) {     
		    return columnName;     
	}   
	 
//	 public String tableToClassName(TableIdentifier tableidentifier){
//		 return tableidentifier.getName();
//	 }
}
