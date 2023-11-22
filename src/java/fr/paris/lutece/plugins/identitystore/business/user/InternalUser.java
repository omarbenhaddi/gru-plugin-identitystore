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
package fr.paris.lutece.plugins.identitystore.business.user;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.api.user.UserRole;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalUser implements Principal, Serializable, Cloneable, User
{

    private final String lastName;
    private final String firstName;
    private final String email;
    private final String code;
    private final String type;

    public InternalUser( String lastName, String firstName, String email, String code, String type )
    {
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.code = code;
        this.type = type;
    }

    @Override
    public Map<String, UserRole> getUserRoles( )
    {
        return new HashMap<>( );
    }

    @Override
    public String getAccessCode( )
    {
        return code;
    }

    @Override
    public String getEmail( )
    {
        return email;
    }

    @Override
    public String getLastName( )
    {
        return lastName;
    }

    @Override
    public String getFirstName( )
    {
        return firstName;
    }

    @Override
    public List<String> getUserWorkgroups( )
    {
        return new ArrayList<>( );
    }

    @Override
    public String getRealm( )
    {
        return type;
    }

    @Override
    public String getName( )
    {
        return lastName;
    }

    @Override
    public InternalUser clone( )
    {
        try
        {
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return (InternalUser) super.clone( );
        }
        catch( CloneNotSupportedException e )
        {
            throw new AssertionError( );
        }
    }
}
