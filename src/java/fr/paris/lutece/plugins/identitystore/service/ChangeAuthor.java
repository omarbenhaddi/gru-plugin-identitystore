/*
 * Copyright (c) 2002-2015, Mairie de Paris
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


package fr.paris.lutece.plugins.identitystore.service;

/**
 * ChangeAuthor
 */
public class ChangeAuthor 
{
    public static final int TYPE_APPLICATION = 0;
    public static final int TYPE_USER_OWNER = 1;
    public static final int TYPE_USER_ADMINISTRATOR = 2;
    
    // Variables declarations 
    private String _strApplication;
    private String _strUserName;
    private String _strUserId;
    private int _nType;
    
    
       /**
        * Returns the Application
        * @return The Application
        */ 
    public String getApplication()
    {
        return _strApplication;
    }
    
       /**
        * Sets the Application
        * @param strApplication The Application
        */ 
    public void setApplication( String strApplication )
    {
        _strApplication = strApplication;
    }
    
       /**
        * Returns the UserName
        * @return The UserName
        */ 
    public String getUserName()
    {
        return _strUserName;
    }
    
       /**
        * Sets the UserName
        * @param strUserName The UserName
        */ 
    public void setUserName( String strUserName )
    {
        _strUserName = strUserName;
    }
    
       /**
        * Returns the UserId
        * @return The UserId
        */ 
    public String getUserId()
    {
        return _strUserId;
    }
    
       /**
        * Sets the UserId
        * @param strUserId The UserId
        */ 
    public void setUserId( String strUserId )
    {
        _strUserId = strUserId;
    }
    
       /**
        * Returns the Type
        * @return The Type
        */ 
    public int getType()
    {
        return _nType;
    }
    
       /**
        * Sets the Type
        * @param nType The Type
        */ 
    public void setType( int nType )
    {
        _nType = nType;
    }
}
