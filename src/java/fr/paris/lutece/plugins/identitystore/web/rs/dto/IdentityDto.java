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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;

import java.util.Map;


/**
 *
 *
 */
@JsonRootName( value = FormatConstants.KEY_IDENTITY )
@JsonPropertyOrder( {FormatConstants.KEY_CONNECTION_ID,
    FormatConstants.KEY_CUSTOMER_ID,
    FormatConstants.KEY_ATTRIBUTES
} )
public class IdentityDto implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Map<String, AttributeDto> _mapAttributes;
    private String _strConnectionId;
    private String _strCustomerId;

    /**
     * @return the _mapAttributes
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTES )
    public Map<String, AttributeDto> getAttributes(  )
    {
        return _mapAttributes;
    }

    /**
     * @param mapAttributes the lstAttributes to set
     */
    @JsonProperty( FormatConstants.KEY_ATTRIBUTES )
    public void setAttributes( Map<String, AttributeDto> mapAttributes )
    {
        this._mapAttributes = mapAttributes;
    }

    /**
     * @return the _connectionId
     */
    @JsonProperty( FormatConstants.KEY_CONNECTION_ID )
    public String getConnectionId(  )
    {
        return _strConnectionId;
    }

    /**
     * @param connectionId the connectionId to set
     */
    @JsonProperty( FormatConstants.KEY_CONNECTION_ID )
    public void setConnectionId( String connectionId )
    {
        this._strConnectionId = connectionId;
    }

    /**
     * @return the _customerId
     */
    @JsonProperty( FormatConstants.KEY_CUSTOMER_ID )
    public String getCustomerId(  )
    {
        return _strCustomerId;
    }

    /**
     * @param strCustomerId the strCustomerId to set
     */
    @JsonProperty( FormatConstants.KEY_CUSTOMER_ID )
    public void setCustomerId( String strCustomerId )
    {
        this._strCustomerId = strCustomerId;
    }
}
