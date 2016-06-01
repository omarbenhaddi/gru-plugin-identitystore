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


/**
 * This class provides constants for formatting
 *
 */
public final class FormatConstants
{
    public static final String KEY_IDENTITY = "identity";
    public static final String KEY_ATTRIBUTES = "attributes";
    public static final String KEY_ATTRIBUTE = "attribute";
    public static final String KEY_ATTRIBUTE_KEY = "key";
    public static final String KEY_ATTRIBUTE_VALUE = "value";
    public static final String KEY_ATTRIBUTE_TYPE = "type";
    public static final String KEY_ATTRIBUTE_WRITABLE = "readable";
    public static final String KEY_ATTRIBUTE_READABLE = "writable";
    public static final String KEY_ATTRIBUTE_CERTIFIABLE = "certifiable";
    public static final String KEY_ATTRIBUTE_CERTIFIED = "certified";
    public static final String KEY_CONNECTION_ID = "connection_id";
    public static final String KEY_CUSTOMER_ID = "customer_id";
    public static final String KEY_ID = "id";
    public static final String KEY_LABEL = "label";
    public static final String KEY_ERRORS = "errors";
    public static final String KEY_ERROR = "error";
    public static final String KEY_STATUS = "status";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_RESPONSE = "response";
    public static final int INDENT = 4;
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";

    /**
     * Private constructor
     */
    private FormatConstants(  )
    {
    }
}
