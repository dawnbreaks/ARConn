package com.lubin.orm.dbpojo.util;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class NameConversion {
	
    private static final Logger logger = Logger.getLogger(NameConversion.class);
    private Set<String> keywords;
    private String keywordsFile;

    public NameConversion(String confFile){
    	keywordsFile= confFile;
    	 loadKeyWords();
    }
    public String getKeywordsFile() {
        return keywordsFile;
    }

    public void setKeywordsFile(String keywordsFile) {
        this.keywordsFile = keywordsFile;
    }

    public String tableNameToClassName(String tableName){
        String preResult = tableName.toLowerCase();
        if(preResult.indexOf('$')!=-1){
            preResult = StringUtils.replace(preResult, "$", "_");
        }
        return LOWER_UNDERSCORE.to(UPPER_CAMEL,preResult);
    }
   
    public String columnNameToIdentifier(String columnName)
    {
        String preResult = columnName.toLowerCase();
        if(preResult.indexOf('$')!=-1){
            preResult = StringUtils.replace(preResult, "$", "_");
        }
        preResult = StringUtils.remove(preResult, "#");
        preResult = LOWER_UNDERSCORE.to(LOWER_CAMEL,preResult);
        if(isKeyword(preResult)){
            preResult = normalizeField(preResult);
        }
        return preResult;
    }

    public String normalizeField(String nameCandidate){
        return "changeMe___"+nameCandidate;
    }


    private Set<String> loadKeyWords()
    {
        keywords = new HashSet<String>();
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(keywordsFile));
            String keyword;
            while ((keyword = in.readLine()) != null){
                keywords.add(keyword);
            }
            in.close();
        } catch (IOException e) {
            logger.error(e);
        }
        return keywords;
    }
    private boolean isKeyword(String fieldName){
        return keywords.contains(fieldName);
    }
}
