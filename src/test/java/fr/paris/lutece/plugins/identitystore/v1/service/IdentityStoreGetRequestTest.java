package fr.paris.lutece.plugins.identitystore.v1.service;

import static fr.paris.lutece.plugins.identitystore.v1.business.IdentityUtil.createIdentityInDatabase;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.v1.web.request.IdentityStoreGetRequest;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityDto;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.test.LuteceTestCase;

public class IdentityStoreGetRequestTest extends LuteceTestCase
{
    private String _strQueryClientAppCode = "MyApplication";
    private ObjectMapper _objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );

        _objectMapper = new ObjectMapper( );
        _objectMapper.enable( SerializationFeature.INDENT_OUTPUT );
        _objectMapper.enable( SerializationFeature.WRAP_ROOT_VALUE );
        _objectMapper.enable( DeserializationFeature.UNWRAP_ROOT_VALUE );
        _objectMapper.enable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
    }

    public void testGetIdentity( ) throws JsonParseException, JsonMappingException, AppException, IOException
    {
        Identity identityReference = createIdentityInDatabase( );
        IdentityDto identityDto = MockIdentityDto.createIdentityV1( identityReference );

        IdentityStoreGetRequest identity = new IdentityStoreGetRequest( identityDto.getConnectionId( ), identityDto.getCustomerId( ), _strQueryClientAppCode,
                _objectMapper );

        IdentityDto identityDtoGetted = _objectMapper.readValue( identity.doRequest( ), IdentityDto.class );
        assertNotNull( identityDtoGetted );
        assertEquals( identityDto.getConnectionId( ), identityDtoGetted.getConnectionId( ) );
        assertNotNull( identityDtoGetted.getCustomerId( ) );
    }
}
