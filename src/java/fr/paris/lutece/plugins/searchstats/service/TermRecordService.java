/*
 * Copyright (c) 2002-2012, Mairie de Paris
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
package fr.paris.lutece.plugins.searchstats.service;

import fr.paris.lutece.plugins.searchstats.business.QueryRecord;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * TermRecordService
 */
public class TermRecordService
{

    public static List<TermRecord> getTopTerms( List<QueryRecord> listQueries )
    {
        HashMap<String, TermRecord> mapTerms = new HashMap<String, TermRecord>();

        String strTerms = "";
        for ( QueryRecord query : listQueries )
        {
            try
            {
                strTerms = query.getQuery();
                StringTokenizer st = new StringTokenizer( strTerms, " " );

                while ( st.hasMoreTokens() )
                {
                    String strTerm = st.nextToken();

                    if ( mapTerms.containsKey( strTerm ) )
                    {
                        TermRecord t = mapTerms.get( strTerm );
                        t.setCount( t.getCount() + 1 );
                    }
                    else
                    {
                        TermRecord t = new TermRecord();
                        t.setTerm( strTerm );
                        t.setCount( 1 );
                        mapTerms.put( strTerm, t );
                    }
                }
            }
            catch ( Exception e )
            {
                AppLogService.error( "Searchstats - getTopTerms error on Terms : " + strTerms + " : " + e.getMessage(), e.getCause() );
            }
        }

        List listTerms = new ArrayList<TermRecord>( mapTerms.values() );
        Collections.sort( listTerms, new TermRecordComparator() );

        return listTerms;
    }
}
