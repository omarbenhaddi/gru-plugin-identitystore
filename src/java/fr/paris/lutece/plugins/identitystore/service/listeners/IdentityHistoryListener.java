package fr.paris.lutece.plugins.identitystore.service.listeners;

import fr.paris.lutece.plugins.identitystore.service.IdentityChange;
import fr.paris.lutece.plugins.identitystore.service.IdentityChangeListener;

public class IdentityHistoryListener implements IdentityChangeListener {

	private static final String SERVICE_NAME = "Database logging IdentityChangeListener";
	
	@Override
	public String getName() 
	{
		return SERVICE_NAME;
	}

	@Override
	public void processIdentityChange(IdentityChange identityChange) 
	{
        // TODO : IdentityHome.log( identityChange );
	}

}
