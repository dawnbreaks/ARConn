package com.lubin.orm.dbpojo;

import com.lubin.orm.dbpojo.model.ClassInfo;
import com.lubin.orm.dbpojo.util.VelocityUtil;

import de.hunsicker.jalopy.Jalopy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

public class PojoWriter {
	
	private static final Logger logger = Logger.getLogger(PojoWriter.class);
   
	private String fileName;
    private String path;
    private String templateName;
    private VelocityUtil velocityUtil;
    private boolean performFormat;
	private Jalopy jalopy;

    public boolean isPerformFormat() {
        return performFormat;
    }

    public void setPerformFormat(boolean performFormat) {
        this.performFormat = performFormat;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        
        final File f = new File(path);
        if(!f.exists()){
            f.mkdirs();
        }
        if(!path.endsWith(File.separator)) this.path+=File.separator;
        
        jalopy = new Jalopy();
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
        velocityUtil = new VelocityUtil(templateName);
    }

    public void write(ClassInfo classInfo)
    {
        String velocityTemplateGeneratedString = velocityUtil.genereateCode(classInfo);
        if(fileName!=null)
        {
            writeToFile(velocityTemplateGeneratedString,fileName,path, performFormat);
        }
        else
        {
            writeToFile(velocityTemplateGeneratedString,classInfo.getName(),path, performFormat);
        }
    }

    public static String packageToDirPath(String javaPackage) {
        return javaPackage.replace('.', File.separatorChar)+File.separatorChar;
    }
    
    
    public void writeToFile(String string, String fileName, String path, boolean performFormat) {
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(path+fileName));
            out.write(string);
            out.close();
        } catch (IOException e) {
            logger.error(e);
        }
        if(performFormat){
            formatCode(path+fileName);
        }
    }
    public void formatCode(String file)
    {
        try {
            jalopy.setInput(new File(file));
            jalopy.setOutput(new File(file));
             // format and overwrite the given input file
            jalopy.format();

        if (jalopy.getState() == Jalopy.State.OK)
            logger.debug(file + " successfully formatted");
        else if (jalopy.getState() == Jalopy.State.WARN)
            logger.debug(file + " formatted with warnings");
        else if (jalopy.getState() == Jalopy.State.ERROR)
            logger.debug(file + " could not be formatted");
        } catch (FileNotFoundException e) {
            logger.error(e);
        }
    }
}
