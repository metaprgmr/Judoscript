/* The following approach does not work */

package com.bea.edu.util;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import weblogic.management.MBeanHome;
import weblogic.management.runtime.*;
import weblogic.management.WebLogicObjectName;
import weblogic.management.WebLogicMBean;

public class EJBLogger extends MBeanLogger
{	private WebLogicMBean webLogicMBean = null;
	private EntityEJBRuntimeMBean entityEJBRuntimeMBean = null;
	private StatelessEJBRuntimeMBean statelessEJBRuntimeMBean = null;
	private StatefulEJBRuntimeMBean statefulEJBRuntimeMBean = null;
	private String mbeanName;
	
	public EJBLogger(String url, 
				String user, 
				String pwd, 
				String outputFileName, 
				String samplingFrequency, 
				String server,
				boolean readableTimeStamp,
				String mbeanName) throws NamingException
	{	super(url, user, pwd, outputFileName, samplingFrequency, server, readableTimeStamp);
		this.mbeanName = mbeanName;
		setMBeans();
	}
	
	protected void setMBeans() 
	{	try
		{	webLogicMBean = (WebLogicMBean)mBeanHome.getMBean(new WebLogicObjectName(mbeanName));
		}
		catch (MalformedObjectNameException infe)
		{	printStream.println("Could not locate WebLogicMBean.");
			System.exit(1);
		}
		catch (InstanceNotFoundException infe)
		{	printStream.println("Could not locate WebLogicMBean.");
			System.exit(1);
		}
		if (webLogicMBean.getType().equals("EntityEJBRuntime"))
			entityEJBRuntimeMBean = (EntityEJBRuntimeMBean)webLogicMBean;
		else 	if (webLogicMBean.getType().equals("StatelessEJBRuntime"))
				statelessEJBRuntimeMBean = (StatelessEJBRuntimeMBean)webLogicMBean;
			else	if (webLogicMBean.getType().equals("StatefulEJBRuntime"))
					statefulEJBRuntimeMBean = (StatefulEJBRuntimeMBean)webLogicMBean;
				else 
				{	System.out.println("EJB Runtime Type mismatch:" + webLogicMBean.getType());
					System.exit(1);
				}
	}
	
	protected void log()
	{	System.out.println("Logging data to " + outputFileName + " every " + samplingFrequency/1000 + " seconds..."); 
		String header = "";
		header = 	"TCTC = Transactions Committed Total Count\n" +
			     	"TRBTC = Transactions Rolled BAck Total Couna\n" +
				"TTOTC = Transactions Timed Out Total Count\n" +
				"AC = Activation Count\n" +
				"PC = Passivation Count\n" +
				"CAC = Cached Access Count\n" +
				"CBCC = Cached Beans Current Count\n" +
				"CHC = Cached Hit Count\n" +
				"BIUC = Beans In Use Count\n" +
				"IBC = Idle Beans Count\n" +
				"TOTC = Time Out Total Count\n" +
				"WTC = Waiter Total Count\n";
		printStream.println(header);	
		if (readableTimeStamp == true)
		{	header = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
			header = "TIME                          TCTC      TRBTC     TTOTC     AC        PC        CAC       CBCC      CHC       BIUC      IBC       TOTC      WTC";
			printStream.println(header);
		}
		else
		{	header = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
			header = "TIME                TCTC      TRBTC     TTOTC     AC        PC        CAC       CBCC      CHC       BIUC      IBC       TOTC      WTC";
			printStream.println(header);
		}
		
		EJBTransactionRuntimeMBean ejbTransactionRuntimeMBean = null;
		EJBCacheRuntimeMBean ejbCacheRuntimeMBean = null;
		EJBPoolRuntimeMBean ejbPoolRuntimeMBean = null;
		
		if (entityEJBRuntimeMBean != null)
		{	ejbTransactionRuntimeMBean = entityEJBRuntimeMBean.getTransactionRuntime();
			ejbCacheRuntimeMBean = entityEJBRuntimeMBean.getCacheRuntime();
			ejbPoolRuntimeMBean = entityEJBRuntimeMBean.getPoolRuntime();
		}
		else	if (statelessEJBRuntimeMBean != null)
			{	ejbTransactionRuntimeMBean = statelessEJBRuntimeMBean.getTransactionRuntime();
				ejbCacheRuntimeMBean = null;
				ejbPoolRuntimeMBean = statelessEJBRuntimeMBean.getPoolRuntime();
			}
			else 	if (statefulEJBRuntimeMBean != null)
				{	ejbTransactionRuntimeMBean = statefulEJBRuntimeMBean.getTransactionRuntime();
					ejbCacheRuntimeMBean = statefulEJBRuntimeMBean.getCacheRuntime();
					ejbPoolRuntimeMBean = null;
				}
				else
				{	System.out.println("webLogicMBean not type cast to any EJBRuntimeMBean.");
					System.exit(1);
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
			
			oneEntry = "" + ejbTransactionRuntimeMBean.getTransactionsCommittedTotalCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + ejbTransactionRuntimeMBean.getTransactionsRolledBackTotalCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			oneEntry = "" + ejbTransactionRuntimeMBean.getTransactionsTimedOutTotalCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			
			if (ejbCacheRuntimeMBean != null)
			{	oneEntry = "" + ejbCacheRuntimeMBean.getActivationCount(); 			oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "" + ejbCacheRuntimeMBean.getPassivationCount(); 			oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "" + ejbCacheRuntimeMBean.getCacheAccessCount(); 		oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "" + ejbCacheRuntimeMBean.getCachedBeansCurrentCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "" + ejbCacheRuntimeMBean.getCacheHitCount(); 			oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			}
			else
			{	oneEntry = "0"; 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "0";	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "0";	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "0";	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "0";	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			}
			
			if (ejbPoolRuntimeMBean != null)
			{	oneEntry = "" + ejbPoolRuntimeMBean.getBeansInUseCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "" + ejbPoolRuntimeMBean.getIdleBeansCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "" + ejbPoolRuntimeMBean.getTimeoutTotalCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "" + ejbPoolRuntimeMBean.getWaiterTotalCount(); 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			}
			else
			{	oneEntry = "0"; 	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "0";	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "0";	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
				oneEntry = "0";	oneLine.append(oneEntry); oneLine.append(getBlankString(10-oneEntry.length()));
			}
			
			printStream.println(oneLine.toString());
			try { Thread.sleep(samplingFrequency); } catch (Exception e) {}
		}
	}

	public static void main(String[] args)
	{	EJBLogger ejbLogger = null;
		if (args.length != 8)
		{	System.out.println("Usage: java com.bea.edu.util.EJBLogger url user password logFileName samplingFreq serverName readableTimeStamp ejbJNDIName");
			System.out.println("Example: java com.bea.edu.util.EJBLogger t3://localhost:7001 system weblogic test.log 5 petstoreServer y mbeanName");
			System.exit(1);
		}
		try
		{	if (args[6].equals("y"))
			{	ejbLogger = new EJBLogger(args[0], args[1], args[2], args[3], args[4], args[5], true, args[7]);
			}
			else
			{	ejbLogger = new EJBLogger(args[0], args[1], args[2], args[3], args[4], args[5], false, args[7]);
			}
		}
		catch (NamingException ne) 
		{	System.out.println("MBeanHome not found.");
			System.exit(1);
		}
		ejbLogger.log();
	}
}