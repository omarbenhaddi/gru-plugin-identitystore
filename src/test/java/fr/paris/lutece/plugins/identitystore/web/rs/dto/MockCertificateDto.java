package fr.paris.lutece.plugins.identitystore.web.rs.dto;

public class MockCertificateDto
{
    public static CertificateDto create( String strCertifierCode )
    {
        CertificateDto certificateDto = new CertificateDto( );
        certificateDto.setCertifierCode( strCertifierCode );

        return certificateDto;
    }
}
