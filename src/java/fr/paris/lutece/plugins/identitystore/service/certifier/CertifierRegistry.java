/*
 * Copyright (c) 2002-2017, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */

package fr.paris.lutece.plugins.identitystore.service.certifier;

import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * CertifierRegistry
 */
public class CertifierRegistry
{
    private static CertifierRegistry _singleton;
    private static Map<String, Certifier> _mapCertifiers;

    /**
     * Private constructor;
     */
    private CertifierRegistry( )
    {

    }

    /**
     * Returns the unique instance
     * 
     * @return The instance
     */
    public static CertifierRegistry instance( )
    {
        if ( _singleton == null )
        {
            _singleton = new CertifierRegistry( );
            List<Certifier> list = SpringContextService.getBeansOfType( Certifier.class );
            _mapCertifiers = new TreeMap<String, Certifier>( );
            for ( Certifier certifier : list )
            {
                _mapCertifiers.put( certifier.getCode( ), certifier );
                AppLogService.info( "New identitystore certifier registered : " + certifier.getName( ) );
            }
        }
        return _singleton;
    }

    /**
     * Get a certifier by its code
     * 
     * @param strCertifierCode
     *            The code
     * @return The certifier
     * @throws CertifierNotFoundException
     *             if the certifier is not found
     */
    public Certifier getCertifier( String strCertifierCode ) throws CertifierNotFoundException
    {
        Certifier certifier = _mapCertifiers.get( strCertifierCode );
        if ( certifier == null )
        {
            throw new CertifierNotFoundException( "Unknown certifier : " + strCertifierCode );
        }
        return certifier;
    }

    public Collection<Certifier> getCertifiersList( )
    {
        return _mapCertifiers.values( );
    }

}
