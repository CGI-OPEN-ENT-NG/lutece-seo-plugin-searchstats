/*
 * Copyright (c) 2002-2014, Mairie de Paris
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
package fr.paris.lutece.plugins.searchstats.business;

import fr.paris.lutece.plugins.searchstats.business.QueryRecord;
import fr.paris.lutece.plugins.searchstats.service.SearchStatsPlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for QueryRecord objects
 */
public final class QueryRecordHome
{
    private static IQueryRecordDAO _dao = (IQueryRecordDAO) SpringContextService.getPluginBean( SearchStatsPlugin.PLUGIN_NAME, "queryRecordDAO" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private QueryRecordHome( )
    {
    }

    /**
     * Creation of an instance of queryRecord
     *
     * @param queryRecord
     *            The instance of the queryRecord which contains the informations to store
     * @param plugin
     *            The current plugin using this method
     * @return The instance of queryRecord which has been created with its primary key.
     */
    public static QueryRecord create( QueryRecord queryRecord, Plugin plugin )
    {
        _dao.insert( queryRecord, plugin );

        return queryRecord;
    }

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns a list of QueryRecord objects filtered by criteria
     * 
     * @param plugin
     *            The current plugin using this method
     * @return A list of QueryRecord
     */
    public static List<QueryRecord> findQueryRecordsListByCriteria( Plugin plugin, RecordFilter filter )
    {
        return _dao.selectQueryRecordListByCriteria( plugin, filter );
    }
    
    /**
     * Returns a list of QueryRecord objects filtered by Date min
     * 
     * @param plugin
     *            The current plugin using this method
     * @return A list of QueryRecord
     */
    public static List<QueryRecord> selectQueryRecordListFromDate( Plugin plugin, RecordFilter filter )
    {
        return _dao.selectQueryRecordListFromDate( plugin, filter );
    }
    

    /**
     * Returns a collection of RecordCount objects
     * 
     * @param plugin
     *            The current plugin using this method
     * @return A collection of RecordCount
     */
    public static List<RecordCount> findRecordsDatesList( Plugin plugin, boolean bNoResult )
    {
        return _dao.selectQueryRecordDatesList( plugin, bNoResult );
    }

    public static List<QueryRecord> getQueryRecordList( Plugin plugin )
    {
        return _dao.selectQueryRecordList( plugin );
    }

}
