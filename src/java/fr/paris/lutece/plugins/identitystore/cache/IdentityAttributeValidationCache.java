package fr.paris.lutece.plugins.identitystore.cache;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import org.apache.log4j.Logger;

import java.util.regex.Pattern;

public class IdentityAttributeValidationCache extends AbstractCacheableService {

    private static Logger _logger = Logger.getLogger(IdentityAttributeValidationCache.class);

    public static final String SERVICE_NAME = "IdentityAttributeValidationCache";

    public IdentityAttributeValidationCache() {
        this.initCache();
    }

    public void refresh( )
    {
        _logger.info( "Init Attribute Validation cache" );
        this.resetCache( );
        AttributeKeyHome.getAttributeKeysList()
                        .forEach(attributeKey -> this.put(attributeKey.getKeyName(), Pattern.compile(attributeKey.getValidationRegex())));
    }

    public void put( final String keyName, final Pattern validationPattern)
    {
        if ( this.getKeys( ).contains( keyName ) )
        {
            this.removeKey( keyName );
        }
        this.putInCache( keyName, validationPattern );
        _logger.info( "Validation Pattern added to cache: " + keyName );
    }

    public void remove( final String keyName )
    {
        if ( this.getKeys( ).contains( keyName ) )
        {
            this.removeKey( keyName );
        }

        _logger.info( "Validation Pattern removed from cache: " + keyName );
    }

    public Pattern get( final String keyName ) throws IdentityAttributeNotFoundException
    {
        Pattern validationPattern = (Pattern) this.getFromCache( keyName );
        if ( validationPattern == null )
        {
            validationPattern = this.getFromDatabase( keyName );
            this.put( keyName, validationPattern );
        }
        return validationPattern;
    }

    public Pattern getFromDatabase( final String keyName ) throws IdentityAttributeNotFoundException
    {
        final AttributeKey attributeKey = AttributeKeyHome.findByKey( keyName );
        if ( attributeKey == null )
        {
            throw new IdentityAttributeNotFoundException( "No attribute key could be found with key " + keyName );
        }
        return Pattern.compile(attributeKey.getValidationRegex());
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }
}
