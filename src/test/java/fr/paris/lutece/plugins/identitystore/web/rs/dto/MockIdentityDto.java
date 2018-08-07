package fr.paris.lutece.plugins.identitystore.web.rs.dto;

import java.util.HashMap;
import java.util.Map;

import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.util.IdGenerator;

public class MockIdentityDto
{
    public static IdentityDto create( )
    {
        IdentityDto identityDto = new IdentityDto( );
        Map<String, AttributeDto> mapAttributes = new HashMap<>( );

        identityDto.setCustomerId( Integer.toString( IdGenerator.generateId( ) ) );
        identityDto.setConnectionId( Integer.toString( IdGenerator.generateId( ) ) );
        identityDto.setAttributes( mapAttributes );

        return identityDto;
    }

    public static IdentityDto create( Identity identity )
    {
        IdentityDto identityDto = new IdentityDto( );
        Map<String, AttributeDto> mapAttributes = new HashMap<>( );

        identityDto.setCustomerId( identity.getCustomerId( ) );
        identityDto.setConnectionId( identity.getConnectionId( ) );
        identityDto.setAttributes( mapAttributes );

        return identityDto;
    }
}
