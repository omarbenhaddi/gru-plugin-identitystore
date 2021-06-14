package fr.paris.lutece.plugins.identitystore.service.certifier;

import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
/**
 * 
 * IGenerateAutomaticCertifierAttribute
 * 
 *
 */
public interface IGenerateAutomaticCertifierAttribute {
	
	
	/**
	 * 
	 * @param identityDTO the identity DTO Informations
	 * @return true if the identity DTO contains informations necessary for adding the automatic certifier Attribute 
	 */
	boolean mustBeGenerated(IdentityDto identityDTO,String strCertifierCode);
	
	/**
	 * Return the value of the automatic certifier attribute
	 * @param identityDTO  the idedentity DTO Informations
	 * @return the value of the automatic certifier attribute
	 */
	String getValue(IdentityDto identityDTO);

}
