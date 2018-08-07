package fr.paris.lutece.plugins.identitystore.business;

import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.util.IdGenerator;

public class MockAttributeCertificate
{
    public static AttributeCertificate create( AbstractCertifier certifier )
    {
        AttributeCertificate attributeCertificate = certifier.generateCertificate( );
        attributeCertificate.setId( IdGenerator.generateId( ) );

        return attributeCertificate;
    }
}
