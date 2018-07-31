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
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Data Access methods for QueryRecord objects
 */
public final class QueryRecordDAO implements IQueryRecordDAO
{
    // Constants
    private static final String SQL_QUERY_INSERT = " INSERT INTO searchstats_queries ( yyyy, mm, dd, hh, query, results_count ) VALUES ( ?, ?, ?, ?, ? , ? ) ";
    private static final String SQL_QUERY_SELECT_COUNT = " SELECT yyyy, mm, dd, count(*) FROM searchstats_queries ";
    private static final String SQL_QUERY_SELECT_COUNT_NO_RESULT = " WHERE results_count = 0 ";
    private static final String SQL_QUERY_SELECT_COUNT_GROUP = " GROUP BY yyyy, mm , dd ORDER BY yyyy * 365 + 31 * mm + dd  DESC ";
    private static final String SQL_QUERY_SELECT = " SELECT yyyy, mm, dd, hh, query FROM searchstats_queries ";
    private static final String SQL_FILTER_YEAR = " yyyy = ? ";
    private static final String SQL_FILTER_MONTH = " mm = ? ";
    private static final String SQL_FILTER_DAY = " dd = ? ";
    private static final String SQL_FILTER_HOUR = " hh = ? ";
    private static final String SQL_FILTER_NO_RESULTS = " results_count = 0 ";
    private static final String SQL_FILTER_FROM_DATE = " STR_TO_DATE(CONCAT(yyyy,'-',mm,'-',dd), '%Y-%m-%d') > STR_TO_DATE(CONCAT(?,'-',?,'-',?), '%Y-%m-%d') " ;
    private static final String SQL_ORDER_BY = " ORDER BY (yyyy * 365 + 31 * mm + dd) * 24 + hh  DESC ";
    private static final String SQL_QUERY_SELECTALL = " SELECT yyyy, mm, dd, hh, query, results_count FROM searchstats_queries ";

    /**
     * Insert a new record in the table.
     *
     * @param queryRecord
     *            The queryRecord object
     * @param plugin
     *            The Plugin using this data access service
     */

    public void insert( QueryRecord queryRecord, Plugin plugin )
    {

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        daoUtil.setInt( 1, queryRecord.getYear( ) );
        daoUtil.setInt( 2, queryRecord.getMonth( ) );
        daoUtil.setInt( 3, queryRecord.getDay( ) );
        daoUtil.setInt( 4, queryRecord.getHour( ) );
        daoUtil.setString( 5, queryRecord.getQuery( ) );
        daoUtil.setInt( 6, queryRecord.getResultsCount( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Load the list of RecordCount for each date that have at least a record
     * 
     * @param plugin
     *            The Plugin using this data access service
     * @return The list of the RecordCount
     */
    public List<RecordCount> selectQueryRecordDatesList( Plugin plugin, boolean bNoResult )
    {
        List<RecordCount> listQueryRecords = new ArrayList<RecordCount>( );

        String strSQL = SQL_QUERY_SELECT_COUNT + ( ( bNoResult ) ? SQL_QUERY_SELECT_COUNT_NO_RESULT : "" ) + SQL_QUERY_SELECT_COUNT_GROUP;
        DAOUtil daoUtil = new DAOUtil( strSQL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            RecordCount queryRecord = new RecordCount( );
            queryRecord.setYear( daoUtil.getInt( 1 ) );
            queryRecord.setMonth( daoUtil.getInt( 2 ) );
            queryRecord.setDay( daoUtil.getInt( 3 ) );
            queryRecord.setCount( daoUtil.getInt( 4 ) );
            listQueryRecords.add( queryRecord );
        }

        daoUtil.free( );

        return listQueryRecords;
    }

    /**
     * Load the list of queryRecords
     * 
     * @param plugin
     *            The Plugin using this data access service
     * @param filter
     *            Filter containing criterias
     * @return The list of the QueryRecords
     */
    public List<QueryRecord> selectQueryRecordListByCriteria( Plugin plugin, RecordFilter filter )
    {
        List<QueryRecord> listQueryRecords = new ArrayList<QueryRecord>( );

        List<String> clauses = new ArrayList<String>( );
        List<String> parameters = new ArrayList<String>( );

        addFilter( filter.getYear( ), SQL_FILTER_YEAR, clauses, parameters );
        addFilter( filter.getMonth( ), SQL_FILTER_MONTH, clauses, parameters );
        addFilter( filter.getDay( ), SQL_FILTER_DAY, clauses, parameters );
        addFilter( filter.getHour( ), SQL_FILTER_HOUR, clauses, parameters );

        if ( filter.getNoResults( ) )
        {
            clauses.add( SQL_FILTER_NO_RESULTS );
        }

        // Build SQL query
        String strSQL = buildQuery( SQL_QUERY_SELECT, clauses );
        strSQL += SQL_ORDER_BY;

        DAOUtil daoUtil = new DAOUtil( strSQL, plugin );

        // Set parameters
        int i = 1;

        for ( String strValue : parameters )
        {
            daoUtil.setInt( i++, Integer.parseInt( strValue ) );
        }

        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            QueryRecord queryRecord = new QueryRecord( );
            queryRecord.setYear( daoUtil.getInt( 1 ) );
            queryRecord.setMonth( daoUtil.getInt( 2 ) );
            queryRecord.setDay( daoUtil.getInt( 3 ) );
            queryRecord.setHour( daoUtil.getInt( 4 ) );
            queryRecord.setQuery( daoUtil.getString( 5 ) );

            listQueryRecords.add( queryRecord );
        }

        daoUtil.free( );

        return listQueryRecords;
    }

    
        /**
     * Load the list of queryRecords
     * 
     * @param plugin
     *            The Plugin using this data access service
     * @param filter
     *            Filter containing criterias
     * @return The list of the QueryRecords
     */
    public List<QueryRecord> selectQueryRecordListFromDate( Plugin plugin, RecordFilter filter )
    {
        List<QueryRecord> listQueryRecords = new ArrayList<QueryRecord>( );

        List<String> clauses = new ArrayList<String>( );
        List<String> parameters = new ArrayList<String>( );

        clauses.add( SQL_FILTER_FROM_DATE );
        parameters.add( "" + filter.getYear( ) );            
        parameters.add( "" + filter.getMonth( ) );            
        parameters.add( "" + filter.getDay( ) );            
        
        
        // Build SQL query
        String strSQL = buildQuery( SQL_QUERY_SELECT, clauses );
        strSQL += SQL_ORDER_BY;

        DAOUtil daoUtil = new DAOUtil( strSQL, plugin );

        // Set parameters
        int i = 1;

        for ( String strValue : parameters )
        {
            daoUtil.setInt( i++, Integer.parseInt( strValue ) );
        }

        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            QueryRecord queryRecord = new QueryRecord( );
            queryRecord.setYear( daoUtil.getInt( 1 ) );
            queryRecord.setMonth( daoUtil.getInt( 2 ) );
            queryRecord.setDay( daoUtil.getInt( 3 ) );
            queryRecord.setHour( daoUtil.getInt( 4 ) );
            queryRecord.setQuery( daoUtil.getString( 5 ) );

            listQueryRecords.add( queryRecord );
        }

        daoUtil.free( );

        return listQueryRecords;
    }
    
    
    private void addFilter( int nFilterValue, String strClause, List<String> clauses, List<String> parameters )
    {
        if ( nFilterValue != RecordFilter.NOT_DEFINED )
        {
            clauses.add( strClause );
            parameters.add( "" + nFilterValue );
        }
    }

    private String buildQuery( String strSelect, List<String> clauses )
    {
        String strSQL = strSelect;
        boolean bFirst = true;

        for ( String strClause : clauses )
        {
            strSQL += ( ( bFirst ) ? " WHERE " : " AND " );
            strSQL += strClause;
            bFirst = false;
        }

        return strSQL;
    }

    
    
    
    public List<QueryRecord> selectQueryRecordList( Plugin plugin )
    {
        List<QueryRecord> queryRecordList = new ArrayList<QueryRecord>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            QueryRecord queryRecord = new QueryRecord( );

            queryRecord.setYear( daoUtil.getInt( 1 ) );
            queryRecord.setMonth( daoUtil.getInt( 2 ) );
            queryRecord.setDay( daoUtil.getInt( 3 ) );
            queryRecord.setHour( daoUtil.getInt( 4 ) );
            queryRecord.setQuery( daoUtil.getString( 5 ) );
            queryRecord.setResultsCount( daoUtil.getInt( 6 ) );

            queryRecordList.add( queryRecord );
        }

        daoUtil.free( );
        return queryRecordList;
    }
}
