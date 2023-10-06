/*
 * Copyright (c) 2002-2023, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.identitystore.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.grubusiness.business.demand.DemandType;
import fr.paris.lutece.plugins.grubusiness.business.web.rs.DemandDisplay;
import fr.paris.lutece.plugins.grubusiness.business.web.rs.DemandResult;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.cache.DemandTypeCacheService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.notificationstore.v1.web.service.NotificationStoreService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;

public final class PurgeIdentityService {

	private static final int ONE_DAY_IN_S = 86400;
	
	private static PurgeIdentityService _instance;
	private static NotificationStoreService _notificationStoreService ;
	private static DemandTypeCacheService _demandTypeCacheService ;
	private static ObjectMapper _mapper = new ObjectMapper( );
	
	public static PurgeIdentityService getInstance( )
	{
		if ( _instance == null )
		{
			_instance = new PurgeIdentityService( );
			
			_notificationStoreService = SpringContextService.getBean( "notificationStore.notificationStoreService" );
			_demandTypeCacheService = SpringContextService.getBean( "identitystore.demandTypeCacheService" );
				
		}
		return _instance;
	}
	
	/**
	 * purge identities 
	 * 
	 * @return log message 
	 */
	public String purge( ) 
	{
		StringBuilder msg = new StringBuilder();
		
		msg.append("{Not Implemented yet} ");
		
		// search identities with flag "deleted", with a passed peremption date, and not associated to a MonParis Account
		List<Identity> listIdentityToDelete = new ArrayList<>();
		Identity testId = new Identity();
		testId.setCustomerId( "TEST-TEST-TEST-TEST" );
		testId.setExpirationDate(  new Timestamp(System.currentTimeMillis( ) ) ); 
		listIdentityToDelete.add( testId );
		
		// for each identity : 
		// - check if exists merged identities
		
		
		// - check if exists recent Demands associated to the identity or the merged ones
		//   >> if true, calculate the new expiration date (date of demand last update + CGUs term)
		for ( Identity identity : listIdentityToDelete)
		{
			long identityExpirationDate = identity.getExpirationDate( ).getTime( );
			
			try 
			{
				DemandResult demandList = _notificationStoreService.getListDemand( identity.getCustomerId( ), null, null, null);
			
				long demandExpirationDateMAX = identityExpirationDate;
				
				for (DemandDisplay demand : demandList.getListDemandDisplay( ) )
				{
					
					String appCode = getAppCodeFromDemandTypeId( demand.getDemand( ).getTypeId( ) );
					List<String> clientCodeList = ServiceContractService.instance( ).getClientCodesFromAppCode( appCode );
					
					int nbMonthsCGUsMAX = 0;
					for ( String client_code : clientCodeList )
					{
						// if there is more than one client code for the app_code, keep the max value of cgus
						int nbMonthsCGUs = ServiceContractService.instance().getDataRetentionPeriodInMonths( client_code );
						if (nbMonthsCGUs > nbMonthsCGUsMAX)
						{
							nbMonthsCGUsMAX = nbMonthsCGUs;
						}								
					}
					
					long demandExpirationDate = demand.getDemand().getModifyDate( ) + nbMonthsCGUsMAX * ONE_DAY_IN_S * 31;
					
					// keep the max
					if ( demandExpirationDateMAX < demandExpirationDate )
					{
						demandExpirationDateMAX = demandExpirationDate;
					}					
				}
				
				
				// check if identity should be preserved or can be deleted
				if ( demandExpirationDateMAX > identityExpirationDate )
				{
					// update the expiration date of identity : it will be deleted later
					identity.setExpirationDate( new Timestamp( demandExpirationDateMAX ) );
					IdentityHome.update( identity );
					
					// add identity history
					
					
					msg.append( "Identity expiration date updated : [").append( identity.getCustomerId( ) ).append("]");
					
				}
				else
				{
					// delete !
					IdentityService.instance( ).delete( identity.getCustomerId( ) );
					msg.append( "Identity deleted for [").append( identity.getCustomerId( ) ).append("]");
					
					// delete notifications
					_notificationStoreService.deleteNotificationByCuid( identity.getCustomerId( ) );
					msg.append( "Notifications deleted for [").append( identity.getCustomerId( ) ).append("]");
				}
			}
			catch ( Exception e )
			{
				msg.append( "Daemon execution error : ").append( e.getMessage( ) );
				return msg.toString( );
			}
			
		}
		
		
		// if the peremption date still passed, delete the identity (and children as merged identities, 
		// suspicious, attributes and attributes history, etc ...) EXCEPT the identity history
		
		
		// return message for daemons
		return msg.toString();
	}

	/**
	 * get app code
	 * 
	 * @param typeId
	 * @return the app code
	 */
	private String getAppCodeFromDemandTypeId(String strTypeId) {
		
		DemandType demandType = _demandTypeCacheService.getResource( strTypeId );
		
		if ( demandType == null )
		{
			// refresh cache & search again
			demandType = reinitDemandTypeCacheAndGetDemandType( strTypeId );
			
			if ( demandType == null )
			{
				return null;
			}
		}
		
		return demandType.getAppCode( );
	}

	/**
	 * reinit Demand Type cache & search mandatory DemandType
	 * 
	 * @param strDemandTypeId
	 * @return the DemandType
	 */
	private DemandType reinitDemandTypeCacheAndGetDemandType( String strDemandTypeId) {
		
		DemandType foundDemandType = null; 
		
		try {
			String json = _notificationStoreService.getDemandTypes( );
			
			List<DemandType> list = _mapper.readValue( json, new TypeReference<ArrayList<DemandType>>(){}) ;
			
			for ( DemandType demand : list)
			{
				if ( _demandTypeCacheService.isCacheEnable( ) )
				{
					// refresh cache
					_demandTypeCacheService.putResourceInCache( demand ) ;
				}
				if ( strDemandTypeId.equals( String.valueOf( demand.getIdDemandType( ) ) ) )
				{
					foundDemandType = demand;
					
					if ( !_demandTypeCacheService.isCacheEnable( ) ) 
					{
						return foundDemandType;
					}
				}
			}
			
		} catch ( Exception e ) {
			AppLogService.error( "Notification Store Service Error", e );
		}		
		
		return foundDemandType;
	}
	
	
}
