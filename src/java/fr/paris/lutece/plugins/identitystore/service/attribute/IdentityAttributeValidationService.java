package fr.paris.lutece.plugins.identitystore.service.attribute;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.cache.IdentityAttributeValidationCache;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeStatus;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.regex.Pattern;

/**
 * Service class used to validate attribute values in requests
 */
public class IdentityAttributeValidationService {

    private final IdentityAttributeValidationCache _cache = SpringContextService.getBean("identitystore.identityAttributeValidationCache");
    private static IdentityAttributeValidationService _instance;

    public static IdentityAttributeValidationService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityAttributeValidationService( );
            _instance._cache.refresh( );
        }
        return _instance;
    }

    /**
     * @see IdentityAttributeValidationService#validateIdentityAttributeValues(Identity, ChangeResponse)
     */
    public void validateMergeRequestAttributeValues(final IdentityMergeRequest request, final IdentityMergeResponse response)
            throws IdentityAttributeNotFoundException {
        final boolean passedValidation = this.validateIdentityAttributeValues(request.getIdentity(), response);
        if(!passedValidation) {
            response.setStatus(IdentityMergeStatus.FAILURE);
            response.setMessage("Some attribute values are not passing validation. Please check in the attribute statuses for details.");
        }
    }

    /**
     * @see IdentityAttributeValidationService#validateIdentityAttributeValues(Identity, ChangeResponse)
     */
    public void validateChangeRequestAttributeValues(final IdentityChangeRequest request, final IdentityChangeResponse response)
            throws IdentityAttributeNotFoundException {
        final boolean passedValidation = this.validateIdentityAttributeValues(request.getIdentity(), response);
        if(!passedValidation) {
            response.setStatus( IdentityChangeStatus.FAILURE );
            response.setMessage("Some attribute values are not passing validation. Please check in the attribute statuses for details.");
        }
    }

    /**
     * Validates all attribute values stored in the provided identity, according to each attribute validation regex.
     * Adds validation error statuses in the response in case of invalid values.
     * @param identity the identity
     * @param response the response
     * @return true if all values are valid, false otherwise.
     */
    private boolean validateIdentityAttributeValues(final Identity identity, final ChangeResponse response)
            throws IdentityAttributeNotFoundException {
        boolean passedValidation = true;
        for(final CertifiedAttribute attribute : identity.getAttributes()) {
            final Pattern validationPattern = _cache.get(attribute.getKey());
            if(validationPattern != null) {
                if(!validationPattern.matcher(attribute.getValue()).matches()) {
                    passedValidation = false;
                    response.getAttributeStatuses( ).add( this.buildAttributeValidationErrorStatus(attribute.getKey()));
                }
            }
        }
        return passedValidation;
    }

    /**
     * Builds an attribute status for invalid value.
     * @param attrStrKey the attribute key
     * @return the status
     */
    private AttributeStatus buildAttributeValidationErrorStatus(final String attrStrKey)
            throws IdentityAttributeNotFoundException {
        final AttributeKey attributeKey = IdentityAttributeService.instance().getAttributeKey(attrStrKey);
        final AttributeStatus attributeStatus = new AttributeStatus( );
        attributeStatus.setKey( attrStrKey );
        attributeStatus.setStatus( AttributeChangeStatus.INVALID_VALUE );
        attributeStatus.setMessage(attributeKey.getValidationErrorMessage());

        return attributeStatus;
    }

}
