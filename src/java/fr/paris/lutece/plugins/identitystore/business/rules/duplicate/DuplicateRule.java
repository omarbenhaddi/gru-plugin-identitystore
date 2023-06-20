/*
 * Copyright (c) 2002-2023, City of Paris
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
package fr.paris.lutece.plugins.identitystore.business.rules.duplicate;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DuplicateRule implements Serializable
{
    private int _nId;
    private String _strName;
    private String _strDescription;
    private List<AttributeKey> _listCheckedAttributes = new ArrayList<>( );
    private int _nNbEqualAttributes;
    private int _nNbMissingAttributes;
    private List<DuplicateRuleAttributeTreatment> _listAttributeTreatments = new ArrayList<>( );
    private DuplicateRulePriority _priority;
    private boolean _bActive;
    private boolean _bDaemon;

    public int getId( )
    {
        return _nId;
    }

    public void setId( int _nId )
    {
        this._nId = _nId;
    }

    public String getName( )
    {
        return _strName;
    }

    public void setName( String _strName )
    {
        this._strName = _strName;
    }

    public String getDescription( )
    {
        return _strDescription;
    }

    public void setDescription( String _strDescription )
    {
        this._strDescription = _strDescription;
    }

    public List<AttributeKey> getCheckedAttributes( )
    {
        return _listCheckedAttributes;
    }

    public void setCheckedAttributes( List<AttributeKey> _listCheckedAttributes )
    {
        this._listCheckedAttributes = _listCheckedAttributes;
    }

    public int getNbEqualAttributes( )
    {
        return _nNbEqualAttributes;
    }

    public void setNbEqualAttributes( int _nNbEqualAttributes )
    {
        this._nNbEqualAttributes = _nNbEqualAttributes;
    }

    public int getNbMissingAttributes( )
    {
        return _nNbMissingAttributes;
    }

    public void setNbMissingAttributes( int _nNbMissingAttributes )
    {
        this._nNbMissingAttributes = _nNbMissingAttributes;
    }

    public List<DuplicateRuleAttributeTreatment> getAttributeTreatments( )
    {
        return _listAttributeTreatments;
    }

    public void setAttributeTreatments( List<DuplicateRuleAttributeTreatment> _listAttributeTreatments )
    {
        this._listAttributeTreatments = _listAttributeTreatments;
    }

    public DuplicateRulePriority getPriority( )
    {
        return _priority;
    }

    public void setPriority( DuplicateRulePriority _priority )
    {
        this._priority = _priority;
    }

    public boolean isActive( )
    {
        return _bActive;
    }

    public void setActive( boolean _bActive )
    {
        this._bActive = _bActive;
    }

    public boolean isDaemon( )
    {
        return _bDaemon;
    }

    public void setDaemon( boolean _bDaemon )
    {
        this._bDaemon = _bDaemon;
    }
}
