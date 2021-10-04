package com.bea.edu.util;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.management.InstanceNotFoundException;
import weblogic.management.MBeanHome;
import weblogic.management.runtime.*;


public class JDBCConnectionPoolLogger extends MBeanLogger
{	private JDBCConnectionPoolRuntimeMBean jdbcConnectionPoolRuntimeMBean;
	String poolName;
	
	public JDBCConnectionPoolLogger(	String url, 
							String user, 
							String pwd, 
							String outputFileName, 
							String samplingFrequency, 
							String server,
							boolean readableTimeStamp,
							String poolName) throws NamingException
	{	super(url, user, pwd, outputFileName, samplingFrequency, server, readableTimeStamp);
		this.poolName = poolName;
		setMBeans();
	}
	
	protected void setMBeans() 
	{	try
		{	// For JDBCConnectionPoolRuntimeMBean, the first parameter is the pool name
			// Use java weblogic.Admin -username system -password weblogic GET -pretty -type JDBCConnectionPoolRuntime and look at Name in MBeanName
			jdbcConnectionPoolRuntimeMBean = (JDBCConnectionPoolRuntimeMBean) mBeanHome.getRuntimeMBean(poolName, "JDBCConnectionPoolRuntime");
		}
		catch (InstanceNotFoundException infe)
		{	printStream.println("Could not locate JDBCConnectionPoolRuntimeMBean.");
		}
	}
	
	protected void log()
	{	System.out.println("Logging data to " + outputFileName + " every " + samplingFrequency/1000 + " seconds..."); 
		String header = "";
		header = 	"ACCC = Active Connections Current Count\n" +
			     	"ACHC = Active Connections High Count\n" +
				"CDT = Connection Delay Time\n" +
				"CTC = Connections Total Count\n" +
				"FRC = Failures to Reconnect Count\n" +
				"LCC = Leaked Connection Count\n" +
				"WCCC = Waiting for Connection Current Count\n" +
				"WCHC = Waiting for Connection High Count\n" +
				"WSHC = Wait Seconds High Count\n";
		printStream.println(header);	
		if (readableTimeStamp == true)
		{	header = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
			header = "TIME                          ACCC      ACHC      CDT       CTC       FRC       LCC       WCCC      WCHC      WSHC";
			printStream.println(header);
		}
		else
		{	header = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
			header = "TIME                ACCC      ACHC      CDT       CTC       FRC       LCC       WCCC      WCHC      WSHC";
			printStream.println(header);
		}
		for (;;)
		{	StringBuffer oneLine = new StringBuffer();
			String oneEntry = null;
			int length = 0;
			if (readableTimeStamp == true)
				{	oneEntry = "" + new Date();
					oneLine.append(oneEntry); 
					oneLine.append(getBlankString(30-oneEntry.length()));
				}
			else
				{	oneEntry = "" + System.currentTimeMillis();
					oneLine.append(oneEntry); 
					oneLine.append(getBlankString(20-oneEntry.length()));
				}
			oneEntry = "" + jdbcConnectionPoolRuntimeMBean.getActiveConnectionsCurrentCount();	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jdbcConnectionPoolRuntimeMBean.getActiveConnectionsHighCount(); 		oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jdbcConnectionPoolRuntimeMBean.getConnectionDelayTime(); 			oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jdbcConnectionPoolRuntimeMBean.getConnectionsTotalCount(); 			oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jdbcConnectionPoolRuntimeMBean.getFailuresToReconnectCount(); 		oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jdbcConnectionPoolRuntimeMBean.getLeakedConnectionCount(); 			oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jdbcConnectionPoolRuntimeMBean.getWaitingForConnectionCurrentCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jdbcConnectionPoolRuntimeMBean.getWaitingForConnectionHighCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jdbcConnectionPoolRuntimeMBean.getWaitSecondsHighCount(); 			oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			printStream.println(oneLine.toString());
			try { Thread.sleep(samplingFrequency); } catch (Exception e) {}
		}
	}

	public static void main(String[] args)
	{	JDBCConnectionPoolLogger jdbcConnectionPoolLogger = null;
		if (args.length != 8)
		{	System.out.println("Usage: java com.bea.edu.util.JDBCConnectionPoolLogger url user password logFileName samplingFreq serverName readableTimeStamp poolName");
			System.out.println("Example: java com.bea.edu.util.JDBCConnectionPoolLogger t3://localhost:7001 system weblogic test.log 5 petstoreServer y petstorePool");
			System.exit(1);
		}
		try
		{	if (args[6].equals("y"))
			{	jdbcConnectionPoolLogger = new JDBCConnectionPoolLogger(args[0], args[1], args[2], args[3], args[4], args[5], true, args[7]);
			}
			else
			{	jdbcConnectionPoolLogger = new JDBCConnectionPoolLogger(args[0], args[1], args[2], args[3], args[4], args[5], false, args[7]);
			}
		}
		catch (NamingException ne) 
		{	System.out.println("MBeanHome not found.");
			System.exit(2);
		}
		jdbcConnectionPoolLogger.log();
	}
}