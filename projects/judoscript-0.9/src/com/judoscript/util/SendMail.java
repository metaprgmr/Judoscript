/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

// install javax.mail; it is part of J2EE.
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.util.*;
import java.io.*;


public class SendMail
{
  public final static int FLD_FROM     = 0;
  public final static int FLD_TO       = 1;
  public final static int FLD_CC       = 2;
  public final static int FLD_BCC      = 3;
  public final static int FLD_SUBJECT  = 4;
  public final static int FLD_ATTACH   = 5;
  public final static int FLD_BODY     = 6;
  public final static int FLD_HTMLBODY = 7;

  public String protocol = "smtp"; // "imap"
  public String mailServer;
  public int port = -1;
  Session session;
  Transport transport = null;

  public SendMail(String mailServerName) {
    int idx = mailServerName.indexOf("://");
    if (idx > 0) {
      protocol = mailServerName.substring(0,idx);
      mailServerName = mailServerName.substring(idx+3);
    }
    idx = mailServerName.indexOf(':');
    if (idx > 0) { mailServer = mailServerName.substring(0,idx);
      try {
        port = Integer.parseInt(mailServerName.substring(idx+1));
      } catch(Exception e) {
        port = -1;
      }
    } else {
      mailServer = mailServerName;
      port = -1;
    }
    Properties properties = System.getProperties();
    session = Session.getInstance(properties, null);
  }

  public void connect(String username, String password)
                     throws NoSuchProviderException, MessagingException
  {
    if (isConnected()) return;
    transport = session.getTransport(protocol);
    transport.connect(mailServer, port, username, password);
  }

  public void disconnect() throws MessagingException {
    if (isConnected()) return;
    transport.close();
    transport = null;
  }
    
  public boolean isConnected() { return (transport != null) ; }

  String getMimeType(String mimeType, String cset) {
    if (cset == null) return mimeType;
    if (cset.startsWith("\""))
      return mimeType + "; charset=" + cset;
    return mimeType + "; charset=\"" + cset + "\"";
  }

  InternetAddress getInetAddr(Object o, String cset) throws MessagingException {
    if (o instanceof InternetAddress) return (InternetAddress)o;
    String s = o.toString();
    int idx = s.indexOf('<');
    String name = null;
    if (idx > 0) {
      name = s.substring(idx+1);
      s = s.substring(0,idx);
      idx = name.indexOf('>');
      if (idx > 0) name = name.substring(0,idx).trim();
    }
    try { return new InternetAddress(s, name, cset); }
    catch(UnsupportedEncodingException uee) { return new InternetAddress(s); }
  }

  public void send(Object FROM, Object[] TOs, Object[] CCs, Object[] BCCs, String subj,
                   String textBody, String htmlBody, String[] filenames, String[] csets)
              throws MessagingException 
  {
    if (!isConnected())
      throw new MessagingException("Mail server not connected.");

    String mt;
    DataHandler htmlDH = null;
    if (htmlBody != null)
      htmlDH = new DataHandler(htmlBody, getMimeType("text/html",csets[FLD_HTMLBODY]));
    int i, len;
    MimeMessage mm = new MimeMessage(session);
    mm.setFrom(getInetAddr(FROM, csets[FLD_FROM]));
    int len1 = (TOs==null) ? 0 : TOs.length;
    int len2 = (CCs==null) ? 0 : CCs.length;
    int len3 = (BCCs==null) ? 0 : BCCs.length;
    Address[] aa = new Address[len1+len2+len3];
    for (i=0; i<len1; i++) {
      aa[i] = getInetAddr(TOs[i], csets[FLD_TO]);
      mm.addRecipient(Message.RecipientType.TO, aa[i]);
    }
    int j;
    for (j=0; j<len2; j++) {
      aa[i] = getInetAddr(CCs[j], csets[FLD_CC]);
      mm.addRecipient(Message.RecipientType.CC, aa[i++]);
    }
    for (j=0; j<len3; ++j) {
      aa[i] = getInetAddr(BCCs[j], csets[FLD_BCC]);
      mm.addRecipient(Message.RecipientType.BCC, aa[i++]);
    }
    mm.setSubject(subj, csets[FLD_SUBJECT]);
    mm.setSentDate(new Date());

    MimeBodyPart mbp = null;
    len = (filenames==null) ? 0 : filenames.length;
    if (len == 0) {
      if ((textBody != null) && (htmlDH != null)) {
        Multipart mp = new MimeMultipart();
        mbp = new MimeBodyPart();
        mbp.setText(textBody,csets[FLD_BODY]);
        mp.addBodyPart(mbp);
        mbp = new MimeBodyPart();
        mbp.setDataHandler(htmlDH);
        mp.addBodyPart(mbp);
        mm.setContent(mp);
      } else if (textBody != null) {
        mm.setText(textBody,csets[FLD_BODY]);
      } else if (htmlDH != null) {
        mm.setDataHandler(htmlDH);
      }
    } else {
      Multipart mp = new MimeMultipart();

      if (textBody != null) {
        mbp = new MimeBodyPart();
        mbp.setText(textBody,csets[FLD_BODY]);
        mp.addBodyPart(mbp);
      }
      if (htmlDH != null) {
        mbp = new MimeBodyPart();
        mbp.setDataHandler(htmlDH);
        mp.addBodyPart(mbp);
      }

      for (i=0; i<len;++i) {
        mbp = new MimeBodyPart();
        FileDataSource fds = new FileDataSource(filenames[i]);
        mbp.setDataHandler(new DataHandler(fds));
        mbp.setFileName(fds.getName());
        mp.addBodyPart(mbp);
      }
      mm.setContent(mp);
    }

    transport.sendMessage(mm, aa);
  }

} // end of class SendMail.

