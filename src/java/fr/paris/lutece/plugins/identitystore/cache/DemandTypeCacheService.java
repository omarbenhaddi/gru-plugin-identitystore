package fr.paris.lutece.plugins.identitystore.cache;

import java.util.Map;

import fr.paris.lutece.plugins.grubusiness.business.demand.DemandType;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;

public class DemandTypeCacheService  extends AbstractCacheableService
	{
	private static final String SERVICE_NAME = "DemandTypeCacheService";
	private static final String PREFIX = "DemandTypeCache_";

	   /**
	    * contructor
	    */
	   public DemandTypeCacheService( )
	   {
	       initCache();
	   }

	   /**
	    * get name
	    * @return the name
	    */
	   public String getName(  )
	   {
	       return SERVICE_NAME;
	   }

	   /**
	    * get demand type data.
	    * 
	    * 
	    * @param strDemandTypeId
	    * @return the demand type
	    */
	   public DemandType getResource( String strTypeId )
	   {
	        return (DemandType) getFromCache( PREFIX + strTypeId );
	   }
	   
	   /**
	    * 
	    * @param nDemandTypeId
	    * @param object
	    */
	   public void putResourceInCache( DemandType demand )
	   {
		   putInCache( PREFIX + demand.getIdDemandType( ), demand );
	   }
	   
	}