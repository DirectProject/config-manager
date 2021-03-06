package org.nhindirect.config.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nhind.config.rest.DomainService;
import org.nhind.config.rest.TrustBundleService;
import org.nhindirect.common.tooling.Command;
import org.nhindirect.common.tooling.StringArrayUtil;
import org.nhindirect.config.manager.printers.BundleAnchorRecordPrinter;
import org.nhindirect.config.manager.printers.TrustBundleRecordPrinter;
import org.nhindirect.config.model.Certificate;
import org.nhindirect.config.model.Domain;
import org.nhindirect.config.model.TrustBundle;
import org.nhindirect.config.model.TrustBundleAnchor;
import org.nhindirect.config.model.TrustBundleDomainReltn;

public class TrustBundleCommands 
{
    private static final String ADD_TRUST_BUNDLE = "Adds a trust bundle to the system." +
            "\r\n  bundleName URL refreshInterval [signingCert]" +
            "\r\n\t bundleName: The name of the bundle.  MUST be unique" +
            "\r\n\t URL: URL of the bundle." +
            "\r\n\t refreshInterval: The interval in minutes that the bundle will be refreshed." +            
            "\r\n\t signingCert: Optional certificate that signed the bundle.  This the location and file name of the signing certficate. "
            + "This is generally used for bundles not protected by HTTPS.";
	
    private static final String REMOVE_TRUST_BUNDLE = "Removes a trust bundle from the system.  The bundle is automatically removed from all domains" +
            "\r\n  bundleName " +
            "\r\n\t bundleName: The name of the bundle to remove.";
   
	
    private static final String REFRESH_TRUST_BUNDLE = "Refreshes a trust bundle in the system which redownloads the bundle and updates all anchors." +
            "\r\n  bundleName " +
            "\r\n\t bundleName: The name of the bundle to refresh.";    
    
	private static final String LIST_BUNDLES_USAGE = "Lists all trust bundles in the system";
    
    private static final String ADD_BUNDLE_TO_DOMAIN = "Adds a trust bundle to a domain." +
            "\r\n  bundleName domainName trustIncoming trustOutgoing" +
            "\r\n\t bundleName: The name of the bundle to add to the domain." +
            "\r\n\t domainName: The name of the domain that the bundle will be added to." +
            "\r\n\t trustIncoming: Indicates if the bundle should be used to trust incoming messages.  Valid values are true or false" +
            "\r\n\t trustOutgoing: Indicates if the bundle should be used to trust outgoing messages.  Valid values are true or false";
    
    private static final String UPDATE_BUNDLE_URL = "Updates the URL of a trust bundle." +
            "\r\n  bundleId url" +
            "\r\n\t bundleName: The name of the bundle to add to the domain." +
            "\r\n\t url: The new URL of the bundle";  
    
    private static final String REMOVE_BUNDLE_FROM_DOMAIN = "Removes a trust bundle from a domain." +
            "\r\n  bundleName domainName " +
            "\r\n\t bundleName: The name of the bundle to add to the domain." +
            "\r\n\t domainName: The name of the domain that the bundle will be added to."; 
    
	private static final String LIST_DOMAIN_BUNDLES_USAGE = "Lists all trust bundles associated to a domain" +
            "\r\n  domainName " +
            "\r\n\t domainName: The name of the domain to list bundles for.";

	private static final String LIST_BUNDLE_ANCHORS = "Lists all anchors within a trust bundle" +
            "\r\n  bundleName" +
            "\r\n\t bundleName: The name of the bundle to list anchors for.";
	
	protected TrustBundleService bundleService;
	
	protected DomainService domainService;
	
	protected TrustBundleRecordPrinter bundlePrinter;
	
	protected BundleAnchorRecordPrinter anchorPrinter;
	
	public TrustBundleCommands(TrustBundleService bundleService, DomainService domainService)
	{
		this.bundleService = bundleService;
		
		this.domainService = domainService;
		
		this.bundlePrinter = new TrustBundleRecordPrinter();
		
		this.anchorPrinter = new BundleAnchorRecordPrinter();
	}
	
	@Command(name = "AddTrustBundle", usage = ADD_TRUST_BUNDLE)
    public void addTrustBundle(String[] args)
	{
		final String bundleName = StringArrayUtil.getRequiredValue(args, 0);
		final String url = StringArrayUtil.getRequiredValue(args, 1);
		final int refreshInterval = Integer.parseInt(StringArrayUtil.getRequiredValue(args, 2)) * 60; // convert minutes to seconds
		final String signingCertFile = StringArrayUtil.getOptionalValue(args, 3, "");
		
		try
		{
			final TrustBundle exBundle = bundleService.getTrustBundle(bundleName);
			
			if (exBundle != null)
			{
				System.out.println("Bundle with name " +  bundleName + " already exists.");
			}
			else
			{
				
				final TrustBundle newBundle = new TrustBundle();
				newBundle.setBundleName(bundleName);
				newBundle.setBundleURL(url);
				newBundle.setRefreshInterval(refreshInterval);
				
				if (!StringUtils.isEmpty(signingCertFile))
				{
					final byte[] signCertData = FileUtils.readFileToByteArray(new File(signingCertFile));
					newBundle.setSigningCertificateData(signCertData);
				}
				bundleService.addTrustBundle(newBundle);
				System.out.println("Trust bundle " + bundleName + " added to the system.");
			}
			
		}
		catch (Exception e)
		{
			System.out.println("Error adding trust bundle " + bundleName + " : " + e.getMessage());
		}
		
	}

	@Command(name = "RefreshTrustBundle", usage = REFRESH_TRUST_BUNDLE)
    public void refreshTrustBundle(String[] args)
	{  
		final String bundleName = StringArrayUtil.getRequiredValue(args, 0);
		try
		{
			final TrustBundle bundle = bundleService.getTrustBundle(bundleName);
			
			if (bundle == null)
			{
				System.out.println("Bundle with name " +  bundleName + " does not exist.");
				return;
			}
			
			bundleService.refreshTrustBundle(bundleName);
			
			System.out.println("Initiated refresh on bundle " + bundle.getBundleName());
			
		}
		catch (Exception e)
		{
			System.out.println("Error refreshing trust bundle: " + e.getMessage());
		}		
	}
	
	@Command(name = "DeleteTrustBundle", usage = REMOVE_TRUST_BUNDLE)
    public void removeTrustBundle(String[] args)
	{
		final String bundleName = StringArrayUtil.getRequiredValue(args, 0);
		
		try
		{
			final TrustBundle bundle = bundleService.getTrustBundle(bundleName);
			
			if (bundle == null)
			{
				System.out.println("Bundle with name " +  bundle + " does not exist.");
				return;
			}
			
			bundleService.deleteTrustBundle(bundleName);
			
			System.out.println("Trust bundle " + bundleName + " deleted");
			
		}
		catch (Exception e)
		{
			System.out.println("Error deleting trust bundle: " + e.getMessage());
		}
	}
	
	@Command(name = "ListTrustBundles", usage = LIST_BUNDLES_USAGE)
    public void listBundles(String[] args)
	{
		try
		{
			final Collection<TrustBundle> bundles = bundleService.getTrustBundles(false);
			
			if (bundles == null || bundles.size() == 0)
				System.out.println("No bundles found");
			else
			{
				bundlePrinter.printRecords(bundles);
			}
		}
		catch (Exception e)
		{
			System.out.println("Error getting trust bundles" + e.getMessage());
		}
	}
	
	
	@Command(name = "DeleteTrustBundleFromDomain", usage = REMOVE_BUNDLE_FROM_DOMAIN)
    public void deleteTrustBundleFromDomain(String[] args)
	{
		final String bundleName = StringArrayUtil.getRequiredValue(args, 0);
		final String domainName = StringArrayUtil.getRequiredValue(args, 1);
		
		try
		{
			final TrustBundle bundle = bundleService.getTrustBundle(bundleName);
			
			if (bundle == null)
			{
				System.out.println("Bundle with name " +  bundle + " does not exist.");
				return;
			}
			
			final Domain domain = domainService.getDomain(domainName);
			
			if (domain == null)
			{
				System.out.println("Domain with name " + domain + " does not exist.");
				return;
			}
			
			// make sure there is already an association
			boolean associationExists = false;
			final Collection<TrustBundleDomainReltn> reltns = bundleService.getTrustBundlesByDomain(domainName, false);
			if (reltns != null && reltns.size() > 0)
			{
				for (TrustBundleDomainReltn reltn : reltns) 
				{
					if (reltn.getTrustBundle().getId() == bundle.getId())
					{
						associationExists = true;
						break;
					}
				}
			}
			
			if (!associationExists)
			{
				System.out.println("Bundle " +  bundle.getBundleName() + " is not associated with domain " + domain.getDomainName());
				return;
			}
			
			bundleService.disassociateTrustBundleFromDomain(bundleName, domainName);
			
			System.out.println("Trust bundle " + bundle.getBundleName() + " removed from domain " + domain.getDomainName());
			
		}
		catch (Exception e)
		{
			System.out.println("Error removing bundle from domain : " + e.getMessage());
		}		
	}
	
	@Command(name = "UpdateBundleURL", usage = UPDATE_BUNDLE_URL)
    public void updateBundleURL(String[] args)
    {
		final String bundleName = StringArrayUtil.getRequiredValue(args, 0);
		final String bundleURL = StringArrayUtil.getRequiredValue(args, 1);		
		
		try
		{
			final TrustBundle bundle = bundleService.getTrustBundle(bundleName);
			
			if (bundle == null)
			{
				System.out.println("Bundle with name " +  bundleName + " does not exist.");
				return;
			}
			
			Certificate signingCert = null;
			if (bundle.getSigningCertificateData() != null)
			{
				signingCert = new Certificate();
				signingCert.setData(bundle.getSigningCertificateData());
			}
			
			bundle.setBundleURL(bundleURL);
			bundleService.updateTrustBundleAttributes(bundleName, bundle);
			
			System.out.println("Trust bundle " + bundle.getBundleName() + " URL updated to " + bundleURL);
			
		}
		catch (Exception e)
		{
			System.out.println("Error updateing bundle URL : " + e.getMessage());
		}		
    }
	
	@Command(name = "AddTrustBundleToDomain", usage = ADD_BUNDLE_TO_DOMAIN)
    public void addTrustBundleToDomain(String[] args)
	{
		final String bundleName = StringArrayUtil.getRequiredValue(args, 0);
		final String domainName = StringArrayUtil.getRequiredValue(args, 1);
		final boolean trustIncoming = Boolean.parseBoolean(StringArrayUtil.getRequiredValue(args, 2));
		final boolean trustOutgoing = Boolean.parseBoolean(StringArrayUtil.getRequiredValue(args, 3)); 
		
		try
		{
			final TrustBundle bundle = bundleService.getTrustBundle(bundleName);
			
			if (bundle == null)
			{
				System.out.println("Bundle with name " +  bundleName + " does not exist.");
				return;
			}
			
			final Domain domain = domainService.getDomain(domainName);
			
			if (domain == null)
			{
				System.out.println("Domain with id " +  domainName + " does not exist.");
				return;
			}
			
			// make sure there isn't already an association
			final Collection<TrustBundleDomainReltn> reltns = bundleService.getTrustBundlesByDomain(domainName, false);
			if (reltns != null && reltns.size() > 0)
			{
				for (TrustBundleDomainReltn reltn : reltns) 
				{
					if (reltn.getTrustBundle().getId() == bundle.getId())
					{
						System.out.println("Bundle " +  bundle.getBundleName() + " is already associated with domain " + domain.getDomainName());
						return;
					}
				}
			}
			
			bundleService.associateTrustBundleToDomain(bundleName, domainName, trustIncoming, trustOutgoing);
			
			System.out.println("Trust bundle " + bundle.getBundleName() + " added to domain " + domain.getDomainName());
			
		}
		catch (Exception e)
		{
			System.out.println("Error associating bundle to domain : " + e.getMessage());
		}
	}
	
	@Command(name = "ListDomainBundles", usage = LIST_DOMAIN_BUNDLES_USAGE)
    public void listDomainBundles(String[] args)
	{
	
		final String domainName = StringArrayUtil.getRequiredValue(args, 0);
		
		try
		{
			final Domain domain = domainService.getDomain(domainName);
			
			if (domain == null)
			{
				System.out.println("Domain with name " +  domainName + " does not exist.");
				return;
			}
			
			// make sure there isn't already an association
			final Collection<TrustBundleDomainReltn> reltns = bundleService.getTrustBundlesByDomain(domainName, false);
			if (reltns == null || reltns.size() == 0)
			{
				System.out.println("No bundles associated with domain " +  domain.getDomainName());
				return;
			}
			
			List<TrustBundle> bundles = new ArrayList<TrustBundle>();
			for (TrustBundleDomainReltn reltn : reltns) 
				bundles.add(reltn.getTrustBundle());

			System.out.println("Bundles associated with domain " +  domain.getDomainName());
			bundlePrinter.printRecords(bundles);
			
		}
		catch (Exception e)
		{
			System.out.println("Error getting domain bundles : " + e.getMessage());
		}
	}
	
	@Command(name = "ListTrustBundleAnchors", usage = LIST_BUNDLE_ANCHORS)
    public void listBundleAnchors(String[] args)
	{
		final String bundleName = StringArrayUtil.getRequiredValue(args, 0);
		
		try
		{
			final TrustBundle bundle = bundleService.getTrustBundle(bundleName);
			
			if (bundle == null)
			{
				System.out.println("Bundle with name " +  bundleName + " does not exist.");
				return;
			}

			if (bundle.getLastSuccessfulRefresh() == null)
			{
				System.out.println("Bundle has never been successfully downloaded.");
				return;
			}
			
			final Collection<TrustBundleAnchor> anchors = bundle.getTrustBundleAnchors();
			if (anchors == null || anchors.size() == 0)
			{
				System.out.println("Bundle has not anchors.");
				return;			
			}
			anchorPrinter.printRecords(anchors);

			
		}
		catch (Exception e)
		{
			System.out.println("Error deleting trust bundle: " + e.getMessage());
		}
	}
	
}
