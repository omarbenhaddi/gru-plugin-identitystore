package fr.paris.lutece.plugins.identitystore.v2.service;

import static fr.paris.lutece.plugins.identitystore.v2.business.IdentityUtil.createIdentityInDatabase;
import static fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityChangeDto.createIdentityChangeDtoFor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.v2.web.request.IdentityStoreUpdateRequest;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AuthorDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityDto;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.test.LuteceTestCase;

public class IdentityStoreUpdateRequestTest extends LuteceTestCase
{
    private static final String APPLICATION_CODE = "MyApplication";
    private static final String STRING = "String";
    // PARAMETERS
    private static final String PARAMETER_FIRST_NAME = "first_name";
    private static final String PARAMETER_FAMILY_NAME = "family_name";
    // VALUES
    private static final String VALUE_FIRST_NAME_UPDATED = "First Name Updated";
    private static final String VALUE_FAMILY_NAME_UPDATED = "Family Name Updated";

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

    public void testUpdateIdentity( ) throws JsonParseException, JsonMappingException, AppException, IOException
    {
        Identity identityReference = createIdentityInDatabase( );
        IdentityDto identityDto = MockIdentityDto.create( identityReference );
        IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
        AuthorDto authorDto = new AuthorDto( );
        authorDto.setApplicationCode( APPLICATION_CODE );
        authorDto.setType( 2 );
        authorDto.setId( "IdAuthor" );
        identityChangeDto.setAuthor( authorDto );
        Map<String, AttributeDto> mapAttributes = new HashMap<String, AttributeDto>( );
        AttributeDto attribute1 = new AttributeDto( );
        attribute1.setKey( PARAMETER_FAMILY_NAME );
        attribute1.setType( STRING );
        attribute1.setValue( VALUE_FAMILY_NAME_UPDATED );
        attribute1.setCertificate( null );
        attribute1.setCertified( false );
        mapAttributes.put( PARAMETER_FAMILY_NAME, attribute1 );
        AttributeDto attribute2 = new AttributeDto( );
        attribute2.setKey( PARAMETER_FIRST_NAME );
        attribute2.setType( STRING );
        attribute2.setValue( VALUE_FIRST_NAME_UPDATED );
        attribute2.setCertificate( null );
        attribute2.setCertified( false );
        mapAttributes.put( PARAMETER_FIRST_NAME, attribute2 );
        identityChangeDto.getIdentity( ).setAttributes( mapAttributes );

        IdentityStoreUpdateRequest identityUpdate = new IdentityStoreUpdateRequest( identityChangeDto, new HashMap<String, File>( ), _objectMapper );

        IdentityDto identityDtoUpdated = _objectMapper.readValue( identityUpdate.doRequest( ), IdentityDto.class );
        assertNotNull( identityDtoUpdated );
        // check if parameters are updated
        Map<String, AttributeDto> mapAttributesUpdated = identityDtoUpdated.getAttributes( );
        assertNotNull( mapAttributesUpdated );
        assertEquals( VALUE_FIRST_NAME_UPDATED, mapAttributesUpdated.get( PARAMETER_FIRST_NAME ).getValue( ) );
        assertEquals( VALUE_FAMILY_NAME_UPDATED, mapAttributesUpdated.get( PARAMETER_FAMILY_NAME ).getValue( ) );
        assertFalse( mapAttributesUpdated.get( PARAMETER_FIRST_NAME ).getCertified( ) );
        assertFalse( mapAttributesUpdated.get( PARAMETER_FAMILY_NAME ).getCertified( ) );
        assertNull( mapAttributesUpdated.get( PARAMETER_FIRST_NAME ).getCertificate( ) );
        assertNull( mapAttributesUpdated.get( PARAMETER_FAMILY_NAME ).getCertificate( ) );
    }
}
