/*
 * Copyright (c) 2002-2016, Mairie de Paris
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
package fr.paris.lutece.plugins.identitystore.business;

import java.io.Serializable;


/**
 * Class which manage attribute rights for Client
 */
public class AttributeRight implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations
    private AttributeKey _attributeKey;
    private ClientApplication _clientApplication;
    private boolean _bReadable;
    private boolean _bWritable;
    private boolean _bCertifiable;

    /**
     * @return the _clientApplication
     */
    public ClientApplication getClientApplication(  )
    {
        return _clientApplication;
    }

    /**
     * @param clientApplication the clientApplication to set
     */
    public void setClientApplication( ClientApplication clientApplication )
    {
        this._clientApplication = clientApplication;
    }

    /**
     * @return the _bReadable
     */
    public boolean isReadable(  )
    {
        return _bReadable;
    }

    /**
     * @param bReadable the bReadable to set
     */
    public void setReadable( boolean bReadable )
    {
        this._bReadable = bReadable;
    }

    /**
     * @return the _bWritable
     */
    public boolean isWritable(  )
    {
        return _bWritable;
    }

    /**
     * @param bWritable the bWritable to set
     */
    public void setWritable( boolean bWritable )
    {
        this._bWritable = bWritable;
    }

    /**
     * @return the _bCertifiable
     */
    public boolean isCertifiable(  )
    {
        return _bCertifiable;
    }

    /**
     * @param bCertifiable the bCertifiable to set
     */
    public void setCertifiable( boolean bCertifiable )
    {
        this._bCertifiable = bCertifiable;
    }

    /**
     * @return the _attribute
     */
    public AttributeKey getAttributeKey(  )
    {
        return _attributeKey;
    }

    /**
     * @param attributeKey the attributeKey to set
     */
    public void setAttributeKey( AttributeKey attributeKey )
    {
        this._attributeKey = attributeKey;
    }
}
