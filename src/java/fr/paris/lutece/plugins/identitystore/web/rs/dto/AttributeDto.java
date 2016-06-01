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
package fr.paris.lutece.plugins.identitystore.web.rs.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;


/**
 *
 *
 */
@JsonRootName( value = FormatConstants.KEY_ATTRIBUTES )
@JsonPropertyOrder( {FormatConstants.KEY_ATTRIBUTE_KEY,
    FormatConstants.KEY_ATTRIBUTE_TYPE,
    FormatConstants.KEY_ATTRIBUTE_VALUE,
    FormatConstants.KEY_ATTRIBUTE_CERTIFIED,
    FormatConstants.KEY_ATTRIBUTE_READABLE,
    FormatConstants.KEY_ATTRIBUTE_WRITABLE,
    FormatConstants.KEY_ATTRIBUTE_CERTIFIABLE
} )
public class AttributeDto implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String _strKey;
    private String _strValue;
    private String _strType;
    private boolean _bCertified;
    private boolean _bWritable;
    private boolean _bReadable;
    private boolean _bCertifiable;

    /**
     * @return the _strName
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_KEY )
    public String getKey(  )
    {
        return _strKey;
    }

    /**
     * @param strKey the strName to set
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_KEY )
    public void setKey( String strKey )
    {
        this._strKey = strKey;
    }

    /**
     * @return the _strValue
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_VALUE )
    public String getValue(  )
    {
        return _strValue;
    }

    /**
     * @param strValue the strValue to set
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_VALUE )
    public void setValue( String strValue )
    {
        this._strValue = strValue;
    }

    /**
     * @return the _strType
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_TYPE )
    public String getType(  )
    {
        return _strType;
    }

    /**
     * @param strType the _strType to set
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_TYPE )
    public void setType( String strType )
    {
        this._strType = strType;
    }

    /**
     * @return the _bWritable
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_WRITABLE )
    public boolean isWritable(  )
    {
        return _bWritable;
    }

    /**
     * @return the _bWritable
     */
    @JsonIgnore
    public boolean getWritable(  )
    {
        return _bWritable;
    }

    /**
     * @param bWritable the bWritable to set
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_WRITABLE )
    public void setWritable( boolean bWritable )
    {
        this._bWritable = bWritable;
    }

    /**
     * @return the _bReadable
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_READABLE )
    public boolean isReadable(  )
    {
        return _bReadable;
    }

    /**
     * @return the _bReadable
     */
    @JsonIgnore
    public boolean getReadable(  )
    {
        return _bReadable;
    }

    /**
     * @param bReadable the bReadable to set
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_READABLE )
    public void setReadable( boolean bReadable )
    {
        this._bReadable = bReadable;
    }

    /**
     * @return the _bCertifiable
     */
    @JsonIgnore
    public boolean getCertfiable(  )
    {
        return _bCertifiable;
    }

    /**
     * @param bCertfiable the bCertfiable to set
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_CERTIFIABLE )
    public void setCertifiable( boolean bCertfiable )
    {
        this._bCertifiable = bCertfiable;
    }

    /**
     * @return the _bCertifiable
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_CERTIFIABLE )
    public boolean isCertifiable(  )
    {
        return _bCertifiable;
    }

    /**
     * @return the _bCertified
     */
    @JsonIgnore
    public boolean getCertified(  )
    {
        return _bCertified;
    }

    /**
     * @param bCertified the bCertified to set
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_CERTIFIED )
    public void setCertified( boolean bCertified )
    {
        this._bCertified = bCertified;
    }

    /**
     * @return the _bCertified
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTE_CERTIFIED )
    public boolean isCertified(  )
    {
        return _bCertified;
    }
}
