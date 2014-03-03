package com.lubin.orm.dbpojo.util;

import com.lubin.orm.dbpojo.model.ClassInfo;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.StringWriter;
import java.io.Writer;

public class VelocityUtil {
    private final VelocityEngine ve;
    private Template t;

    public VelocityUtil(String templateName)
    {
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        t = ve.getTemplate(templateName);
    }
    
    public String genereateCode(ClassInfo classInfo) {
        Writer writer = null;
        try {
            VelocityContext ctx = new VelocityContext();
            ctx.put("pojo", classInfo);
            writer = new StringWriter();
            t.merge(ctx, writer);
            writer.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (writer != null) {
            return writer.toString();
        }
        return null;
    }
}
