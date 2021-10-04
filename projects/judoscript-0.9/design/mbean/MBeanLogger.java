package com.bea.edu.util;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.management.InstanceNotFoundException;
import weblogic.management.MBeanHome;
import weblogic.management.runtime.*;

public class MBeanLogger
{	protected String url;
	protected String user;
	protected String pwd;
	protected String outputFileName;
	protected long samplingFrequency;
	protected String server;
	protected boolean readableTimeStamp;
	
	protected MBeanHome mBeanHome;
	protected PrintStream printStream;
	private Hashtable env;
	private Context ic;
	
	public MBeanLogger(	String url, 
					String user, 
					String pwd, 
					String outputFileName, 
					String samplingFrequency, 
					String server,
					boolean readableTimeStamp) throws NamingException
	{	this.url = url;
		this.user = user;
		this.pwd = pwd;
		this.outputFileName = outputFileName;
		this.samplingFrequency = 1000 * Long.parseLong(samplingFrequency);
		this.server = server;
		this.readableTimeStamp = readableTimeStamp;
		env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
		env.put(Context.PROVIDER_URL, url);
		env.put(Context.SECURITY_PRINCIPAL, user);
		env.put(Context.SECURITY_CREDENTIALS, pwd);
		ic = new InitialContext(env);
		//mBeanHome = (MBeanHome)ic.lookup(MBeanHome.ADMIN_JNDI_NAME);
		mBeanHome = (MBeanHome)ic.lookup(MBeanHome.JNDI_NAME + "." + server);
		preparePrintStream();
	}
	
	protected void preparePrintStream()
	{	FileOutputStream fos = null;
		try
		{	fos = new FileOutputStream(outputFileName);	
		}
		catch (Exception e) 
		{	e.printStackTrace();
			try 
			{	fos = new FileOutputStream("MBeanLogger.log");
			}
			catch (Exception e2) {}
		}
		printStream = new PrintStream(fos);
	}

	protected String getBlankString(int n)
	{	StringBuffer sb = new StringBuffer(n);
		for (int i=0; i<n; i++)
			sb.append(" ");
		return sb.toString();
	}
	
	protected void setMBeans() {}
	
	// for testing	
	protected void log()
	{	System.out.println("Logging data to " + outputFileName + " every " + samplingFrequency/1000 + " seconds..."); 
		String header = "MBeanHome successfully located.";
		printStream.println(header);
		for (;;)
		{	StringBuffer oneLine = new StringBuffer();
			String oneEntry = null;
			int length = 0;
			if (readableTimeStamp == true)
				{	oneEntry = "" + new Date();
					oneLine.append(oneEntry); 
				}
			else
				{	oneEntry = "" + System.currentTimeMillis();
					oneLine.append(oneEntry); 
				}
			printStream.println(oneLine.toString());
			try { Thread.sleep(samplingFrequency); } catch (Exception e) {}
		}
	}
	
	// for testing
	public static void main(String[] args)
	{	MBeanLogger mBeanLogger = null;
		if (args.length != 7)
		{	System.out.println("Usage: java com.bea.edu.util.MBeanLogger url user password logFileName samplingFreq serverName readableTimeStamp");
			System.out.println("Example: java com.bea.edu.util.MBeanLogger t3://localhost:7001 weblogic weblogic test.log 5 petstoreServer y");
			System.exit(1);
		}
		try
		{	if (args[6].equals("y"))
			{	mBeanLogger = new MBeanLogger(args[0], args[1], args[2], args[3], args[4], args[5], true);
			}
			else
			{	mBeanLogger = new MBeanLogger(args[0], args[1], args[2], args[3], args[4], args[5], false);
			}
		}
		catch (NamingException ne) 
		{	System.out.println("MBeanHome not found.");
			System.exit(2);
		}
		mBeanLogger.setMBeans();
		mBeanLogger.log();
	}
}