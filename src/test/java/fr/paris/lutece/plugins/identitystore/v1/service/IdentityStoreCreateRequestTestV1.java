package fr.paris.lutece.plugins.identitystore.v1.service;

import static fr.paris.lutece.plugins.identitystore.v1.business.IdentityUtil.createIdentityInDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.v1.web.request.IdentityStoreCreateRequest;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityDto;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.test.LuteceTestCase;

public class IdentityStoreCreateRequestTestV1 extends LuteceTestCase
{
    private ObjectMapper _objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );

        IdentityStoreTestContext.initContext( );

        _objectMapper = new ObjectMapper( );
        _objectMapper.enable( SerializationFeature.INDENT_OUTPUT );
        _objectMapper.enable( SerializationFeature.WRAP_ROOT_VALUE );
        _objectMapper.enable( DeserializationFeature.UNWRAP_ROOT_VALUE );
        _objectMapper.enable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
    }

    public void testCreateIdentity( ) throws JsonParseException, JsonMappingException, AppException, IOException
    {
        Identity identityReference = createIdentityInDatabase( );
        IdentityDto identityDto = MockIdentityDto.createIdentityV1( identityReference );
        IdentityChangeDto identityChangeDto = MockIdentityChangeDto.createIdentityChangeDtoV1For( identityDto );
        Map<String, File> mapAttachedFiles = new HashMap<String, File>( );

        IdentityStoreCreateRequest identityCreate = new IdentityStoreCreateRequest( identityChangeDto, mapAttachedFiles, _objectMapper );

        IdentityDto identityDtoCreated = _objectMapper.readValue( identityCreate.doRequest( ), IdentityDto.class );
        assertNotNull( identityDtoCreated );
    }
}
