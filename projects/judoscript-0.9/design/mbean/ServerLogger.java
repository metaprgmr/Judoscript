package com.bea.edu.util;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.management.InstanceNotFoundException;
import weblogic.management.MBeanHome;
import weblogic.management.runtime.*;


public class ServerLogger extends MBeanLogger
{	private JVMRuntimeMBean jvmRuntimeMBean;
	private ExecuteQueueRuntimeMBean executeQueueRuntimeMBean;
	private JTARuntimeMBean jtaRuntimeMBean;
	
	public ServerLogger(	String url, 
					String user, 
					String pwd, 
					String outputFileName, 
					String samplingFrequency, 
					String server,
					boolean readableTimeStamp) throws NamingException
	{	super(url, user, pwd, outputFileName, samplingFrequency, server, readableTimeStamp);
		setMBeans();
	}
	
	protected void setMBeans() 
	{	try
		{	// For JVMRuntimeMBean, the first parameter is the server name.  
			// Hint: Use java weblogic.Admin -username weblogic -password weblogic GET -pretty -type JVMRuntime and look at Name in MBeanName
			jvmRuntimeMBean = (JVMRuntimeMBean)mBeanHome.getRuntimeMBean(server, "JVMRuntime");
		}
		catch (InstanceNotFoundException infe)
		{	printStream.println("Could not locate JVMRuntimeMBean.");
		}
		try
		{	// For ExecuteQueueRuntimeMBean, the first parameter is the queue name ("default", "__weblogic_admin_html_queue", "__weblogic_admin_rmi_queue")
			// Hint: Use java weblogic.Admin -username weblogic -password weblogic GET -pretty -type ExecuteQueueRuntime and look at Name in MBeanName
			executeQueueRuntimeMBean = (ExecuteQueueRuntimeMBean)mBeanHome.getRuntimeMBean("default", "ExecuteQueueRuntime");
		}
		catch (InstanceNotFoundException infe)
		{	printStream.println("Could not locate ExecuteQueueRuntimeMBean.");
		}
		try 
		{	// For JTARuntimeMBean, the first parameter is "JTARuntime"	
			// Hint: java weblogic.Admin -username weblogic -password weblogic GET -pretty -type JTARuntime and look at Name in MBeanName
			jtaRuntimeMBean = (JTARuntimeMBean)mBeanHome.getRuntimeMBean("JTARuntime", "JTARuntime");
		}
		catch (InstanceNotFoundException infe)
		{	printStream.println("Could not locate JTARuntimeMBean.");
		}
	}
	
	protected void log()
	{	System.out.println("Logging data to " + outputFileName + " every " + samplingFrequency/1000 + " seconds..."); 
		String header = "Heap size: ";
		header += jvmRuntimeMBean.getHeapSizeCurrent();
		printStream.println(header);
		header = 	"FH = Free Heap\n" +
			     	"IET = Idle Execute Threads\n" +
				"PR = Pending Requests\n" +
				"TSR = Total Serviced Requests\n" +
				"TT = Total Transactions\n" +
				"TCT = Total Committed Transactions\n" +
				"TRBT = Total Rolled Back Transactions\n" +
				"TSRBT = Total System Rolled Back Transactions\n" +
				"TTRBT = Total Timeout Rolled Back Transactions\n";
		printStream.println(header);	
		if (readableTimeStamp == true)
		{	header = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
			header = "TIME                          FH        IET       PR        TSR       TT        TCT       TRBT      TSRBT     TTRBT";
			printStream.println(header);
		}
		else
		{	header = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
			header = "TIME                FH        IET       PR        TSR       TT        TCT       TRBT      TSRBT     TTRBT";
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
			oneEntry = "" + jvmRuntimeMBean.getHeapFreeCurrent(); 						oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + executeQueueRuntimeMBean.getExecuteThreadCurrentIdleCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + executeQueueRuntimeMBean.getPendingRequestCurrentCount(); 		oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + executeQueueRuntimeMBean.getServicedRequestTotalCount(); 		oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jtaRuntimeMBean.getTransactionTotalCount(); 					oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jtaRuntimeMBean.getTransactionCommittedTotalCount(); 			oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jtaRuntimeMBean.getTransactionRolledBackTotalCount(); 			oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jtaRuntimeMBean.getTransactionRolledBackSystemTotalCount(); 		oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + jtaRuntimeMBean.getTransactionRolledBackTimeoutTotalCount(); 		oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			printStream.println(oneLine.toString());
			try { Thread.sleep(samplingFrequency); } catch (Exception e) {}
		}
	}

	public static void main(String[] args)
	{	ServerLogger serverLogger = null;
		if (args.length != 7)
		{	System.out.println("Usage: java com.bea.edu.util.ServerLogger url user password logFileName samplingFreq serverName readableTimeStamp");
			System.out.println("Example: java com.bea.edu.util.ServerLogger t3://localhost:7001 system weblogic test.log 5 petstoreServer y");
			System.exit(1);
		}
		try
		{	if (args[6].equals("y"))
			{	serverLogger = new ServerLogger(args[0], args[1], args[2], args[3], args[4], args[5], true);
			}
			else
			{	serverLogger = new ServerLogger(args[0], args[1], args[2], args[3], args[4], args[5], false);
			}
		}
		catch (NamingException ne) 
		{	System.out.println("MBeanHome not found.");
			System.exit(2);
		}
		serverLogger.log();
	}
}