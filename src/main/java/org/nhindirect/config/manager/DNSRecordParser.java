/* 
Copyright (c) 2010, NHIN Direct Project
All rights reserved.

Authors:
   Greg Meyer      gm2552@cerner.com
 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
in the documentation and/or other materials provided with the distribution.  Neither the name of the The NHIN Direct Project (nhindirect.org). 
nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.nhindirect.config.manager;

import java.net.InetAddress;

import org.nhindirect.common.tooling.StringArrayUtil;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TXTRecord;

/**
 * Parses an array of strings into DNS records.
 * @author Greg Meyer
 *
 * @since 1.0
 */
public class DNSRecordParser 
{
	public static final String PARSE_ANAME_USAGE = "  hostname ipaddress ttl [notes]" +
	      "\r\n\t hostname: host name for the record" + 
	      "\r\n\t ipaddress: IP address in dot notation" +
	      "\r\n\t ttl: time to live in seconds, 32bit int";
	
	public static final String PARSE_SOA_USAGE = "  domainname primarysourcedomain responsibleemail serialnumber ttl [refresh] [retry] [expire] [minimum] [notes]" +
	      "\r\n\t domainname: The domain name of the name server that was the primary source for this zone" +
	      "\r\n\t responsibleemail: Email mailbox of the hostmaster" +
	      "\r\n\t serialnumber: Version number of the original copy of the zone." +
	      "\r\n\t ttl: time to live in seconds, 32bit int" +
	      "\r\n\t [refresh]: Number of seconds before the zone should be refreshed." + 
	      "\r\n\t [retry]: Number of seconds before failed refresh should be retried." + 
	      "\r\n\t [expire]: Number of seconds before records should be expired if not refreshed" +
	      "\r\n\t [minimum]: Minimum TTL for this zone.";
	
	public static final String PARSE_MX_USAGE = "  domainname exchange ttl [preference] [notes]" + 
	      "\r\n\t domainname: email domain name for the record" +
	      "\r\n\t exchange: smtp server host name for the domain" + 
	      "\r\n\t ttl: time to live in seconds" +
	      "\r\n\t [preference]: short value indicating preference of the record";
	
	public static final String PARSE_NS_USAGE = "  domainname target ttl" + 
		      "\r\n\t domainname: email domain name for the record" +
		      "\r\n\t target: the dns server name that will handle requests for the domain" + 
		      "\r\n\t ttl: time to live in seconds";
	
	public static final String PARSE_CNAME_USAGE = "  name alias ttl" + 
		      "\r\n\t name: the name that will be aliased" +
		      "\r\n\t alias: the value of the alias" + 
		      "\r\n\t ttl: time to live in seconds";
	
	public static final String PARSE_TXT_USAGE = "  name text ttl" + 
		      "\r\n\t name: the name of the text entry" +
		      "\r\n\t alias: the text value of the record" + 
		      "\r\n\t ttl: time to live in seconds";
	
	public static final String PARSE_SRV_USAGE = "  name target port priority weight ttl" + 
		      "\r\n\t name: the name of the SRV entry" +
		      "\r\n\t target: the server that hosts the service of the SRV entry" +			
		      "\r\n\t port: the IP port to use when conneting to the target" +			      
		      "\r\n\t priority: the priority the record compared to other SRV records of the same name" + 
		      "\r\n\t weight: the weight the record compared to other SRV records of the same name and priority" + 		      
		      "\r\n\t ttl: time to live in seconds";	
	
	/**
	 * Default empty constructor
	 * 
	 * @since 1.0
	 */
	public DNSRecordParser()
	{
	}
	
	/*
	 * converts a string to a dnsjava Name
	 */
	private Name nameFromString(String str)
	{
		if (!str.endsWith("."))
			str += ".";
	
		try
		{
			return Name.fromString(str);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Invalid DNS name");
		}
	}
	
	/*
	 * converts a string to a InetAddress object
	 */
	private InetAddress inetFromString(String str)
	{
		try
		{
			return InetAddress.getByName(str);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Invalid ip address");
		}
	}
	
	/**
	 * Converts TXT record configuration information to an TXTRecord 
	 * @param args The TXT record configuration parameters.
	 * @return A DNS ARecord.
	 * 
	 * @since 1.0
	 */
	public TXTRecord parseTXT(String[] args)
	{
		
	    String name = StringArrayUtil.getRequiredValue(args, 0);
	    String text = StringArrayUtil.getRequiredValue(args, 1);
	    int ttl = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 2));
	    
	    return new TXTRecord(nameFromString(name), DClass.IN, ttl, text);

	}
	
	/**
	 * Converts CNAME record configuration information to an ARecord 
	 * @param args The A record configuration parameters.
	 * @return A DNS ARecord.
	 * 
	 * @since 1.0
	 */
	public CNAMERecord parseCNAME(String[] args)
	{
		
	    String domainName = StringArrayUtil.getRequiredValue(args, 0);
	    String alias = StringArrayUtil.getRequiredValue(args, 1);
	    int ttl = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 2));
	    
	    return new CNAMERecord(nameFromString(domainName), DClass.IN, ttl, nameFromString(alias));

	}
	
	/**
	 * Converts A record configuration information to an ARecord 
	 * @param args The A record configuration parameters.
	 * @return A DNS ARecord.
	 * 
	 * @since 1.0
	 */
	public ARecord parseANAME(String[] args)
	{
		
	    String domainName = StringArrayUtil.getRequiredValue(args, 0);
	    String ipAddress = StringArrayUtil.getRequiredValue(args, 1);
	    int ttl = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 2));
	    
	    return new ARecord(nameFromString(domainName), DClass.IN, ttl, inetFromString(ipAddress));

	}
	       
	/**
	 * Converts SAO record configuration information to an SOARecord 
	 * @param args The SOA record configuration parameters.
	 * @return A DNS SAORecord.
	 * 
	 * @since 1.0
	 */	
	public SOARecord parseSOA(String[] args)
	{
	    String domainName = StringArrayUtil.getRequiredValue(args, 0);
	    String primarySourceDomain = StringArrayUtil.getRequiredValue(args, 1);
	    String responsibleEmail = StringArrayUtil.getRequiredValue(args, 2);
	    int serialNumber = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 3));
	    int ttl = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 4));
	
	    int refresh = Integer.parseInt(StringArrayUtil.getOptionalValue(args, 5, "0"));
	    int retry = Integer.parseInt(StringArrayUtil.getOptionalValue(args, 6, "0"));
	    int expire = Integer.parseInt(StringArrayUtil.getOptionalValue(args, 7, "0"));
	    int minimum = Integer.parseInt(StringArrayUtil.getOptionalValue(args, 8, "0"));
	
	    return new SOARecord(nameFromString(domainName), DClass.IN, ttl, nameFromString(primarySourceDomain), 
	    		nameFromString(responsibleEmail), serialNumber, refresh, retry, expire, minimum);

	}
	  
	/**
	 * Converts MX record configuration information to an MXRecord 
	 * @param args The MX record configuration parameters.
	 * @return A DNS MXRecord.
	 * 
	 * @since 1.0
	 */		
	public MXRecord parseMX(String[] args)
	{        
		String domainName = StringArrayUtil.getRequiredValue(args, 0);
		String exchange = StringArrayUtil.getRequiredValue(args, 1);
	    int ttl = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 2));
	    short pref = Short.parseShort(StringArrayUtil.getOptionalValue(args, 3, "0"));
	
	    return new MXRecord(nameFromString(domainName), DClass.IN, ttl, pref, nameFromString(exchange));
	}
	
	/**
	 * Converts NS record configuration information to an NSRecord 
	 * @param args The NS record configuration parameters.
	 * @return A DNS NSRecord.
	 * 
	 * @since 1.3
	 */		
	public NSRecord parseNS(String[] args)
	{        
		String domainName = StringArrayUtil.getRequiredValue(args, 0);
		String target = StringArrayUtil.getRequiredValue(args, 1);
	    int ttl = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 2));
	
	    return new NSRecord(nameFromString(domainName), DClass.IN, ttl, nameFromString(target));
	}	
	
	/**
	 * Converts SRV record configuration information to an SRVRecord 
	 * @param args The NS record configuration parameters.
	 * @return A DNS NSRecord.
	 * 
	 * @since 6.0.1
	 */		
	public SRVRecord parseSRV(String[] args)
	{        
		String name = StringArrayUtil.getRequiredValue(args, 0);
		String target = StringArrayUtil.getRequiredValue(args, 1);
		int port = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 2));
		int priority = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 3));
		int weight = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 4));
	    int ttl = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 5));
	    
	    return new SRVRecord(nameFromString(name), DClass.IN, ttl, priority, weight, port, nameFromString(target));
	}		
}
