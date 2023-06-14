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
package fr.paris.lutece.plugins.identitystore.service.search;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttributeDto;

import java.util.List;

public interface ISearchIdentityService
{
    /**
     * returns a list of qualified identities from combination of attributes
     *
     * @param max
     *            the maximum number of results that must be returned, if set to 0 or null, must be ignored by implementation.
     * @param attributes
     *            list of values to search for some attributes with strict or fuzzy mode
     * @return a list of identities satisfying the criteria of the {@link SearchAttributeDto} list
     */
    List<QualifiedIdentity> getQualifiedIdentities( final List<SearchAttributeDto> attributes,final Integer minimalShouldMatch, final Integer maxMissingAttributes, final int max, final boolean connected );

    List<QualifiedIdentity> getQualifiedIdentities( final List<SearchAttributeDto> attributes, final int max, final boolean connected );

    /**
     * returns a list of qualified identities having values for specified atribute keys.
     *
     * @param max
     *            the maximum number of results that must be returned, if set to 0 or null, must be ignored by implementation.
     * @param attributeKeys
     *            the list of attribute keys
     * @param notMerged
     *            if the returned identities have to be not merged
     * @return a list of identities satisfying the criterias.
     */
    List<QualifiedIdentity> getQualifiedIdentitiesHavingAttributes( final List<AttributeKey> attributeKeys, final int max, final boolean notMerged,
            final boolean notSuspicious );
}
