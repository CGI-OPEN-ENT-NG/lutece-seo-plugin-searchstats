/*
 * Copyright (c) 2002-2018, Mairie de Paris
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
import fr.paris.lutece.plugins.searchstats.business.QueryRecordHome;
import fr.paris.lutece.plugins.searchstats.business.RecordFilter;
import fr.paris.lutece.plugins.searchstats.business.TimedRecord;
import fr.paris.lutece.portal.business.mailinglist.Recipient;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.mailinglist.AdminMailingListService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public final class SearchStatsService
{

    private static String PROPERTY_MAIL_SENDER_NAME = AppPropertiesService.getProperty( "daemon.mail.sender_name" );
    private static String CONSTANT_MAIL_SENDER = MailService.getNoReplyEmail( );
    private static String MESSAGE_MAIL_SUBJECT = I18nService.getLocalizedString("searchstats.mail_content.subject", Locale.getDefault()) + AppPathService.getProdUrl();
    private static final String TEMPLATE_STATS_TEMPLATE = "/admin/plugins/searchstats/mail/mail_content.html";
    private static final String MARK_RECORDS_LIST = "records_list";
    private static final String MARK_SEARCH_STATS_SERVICE = "search_stats_service";
    private static final String STATS_DAEMON_INTERVAL = "core.daemon.SearchStatsDaemon.interval";
    private static final String DEFAULT_INTERVAL = "86400";
    private static final String MARK_TOP_RESULT_MAX = "nb_top_result_max";
    private static final String MARK_URL = "site_url";

    /**
     * Send email to list recipient
     * @throws IOException 
     */
    public static int sendEmail( )
    {
    	int nNbEmail=0;
        Map<String, Object> model = new HashMap<>( );
        Plugin plugin = null;        
        RecordFilter recordFilter = new RecordFilter();
        recordFilter.setDay( LocalDate.now().getDayOfMonth( ) );
        recordFilter.setMonth( LocalDate.now().getMonthValue( ) );
        recordFilter.setYear( LocalDate.now().getYear() - AppPropertiesService.getPropertyInt("searchstats.year_before", 1));
        model.put( MARK_RECORDS_LIST, QueryRecordHome.selectQueryRecordListFromDate(plugin, recordFilter) );
        model.put( MARK_SEARCH_STATS_SERVICE, new SearchStatsService( ) );
        model.put( MARK_TOP_RESULT_MAX, AppPropertiesService.getPropertyInt( "daemon.mail.nb_top_result_max", 10 ) );
        model.put( MARK_URL, AppPathService.getProdUrl());
        String strContent = AppTemplateService.getTemplate( TEMPLATE_STATS_TEMPLATE, Locale.getDefault( ), model ).getHtml( );

        Collection<Recipient> colMailingList = AdminMailingListService.getRecipients( null, AppPropertiesService.getProperty( "daemon.mail.recipient" ) );
        for ( Recipient recipient : colMailingList )
        {
            MailService.sendMailHtml( recipient.getEmail( ), PROPERTY_MAIL_SENDER_NAME, CONSTANT_MAIL_SENDER, MESSAGE_MAIL_SUBJECT, strContent );
            nNbEmail++;
        }
        return nNbEmail;
    }

    /**
     * @param nYear 
     * @param nMonth
     * @param nDay
     * @param nHour
     * @param listQueryRecords
     *            The list of Query Records
     * @param nLimit
     *            The limit number of queries
     * @return The list of the QueryRecords
     */
    public List<TermRecord> getSearchStatsAfterdate( int nYear, int nMonth, int nDay, int nHour, List<QueryRecord> listQueryRecords, int nLimit )
    {
        List<QueryRecord> listQueryRecordAfterDate = new ArrayList<>();

        TimedRecord timeReport = new TimedRecord( );
        timeReport.setYear( nYear );
        timeReport.setMonth( nMonth );
        timeReport.setDay( nDay );
        timeReport.setHour( nHour );

        for ( QueryRecord queryRecord : listQueryRecords )
        {
            if ( checkAfterDuration( timeReport, queryRecord ) )
            {
            	listQueryRecordAfterDate.add( queryRecord );
            }
        }
        List<TermRecord> listTermRecords = TermRecordService.getTopTerms(listQueryRecordAfterDate);
        return listTermRecords;
    }
    
    /**
     * @return The timeRecord
     */
    public static TimedRecord getIntervalTimeRecord( )
    {
        TimedRecord timedRecord = new TimedRecord( );
        String strNombreSecondes = DatastoreService.getInstanceDataValue( STATS_DAEMON_INTERVAL, DEFAULT_INTERVAL );
        int nStrNombreSecondes = Integer.parseInt( strNombreSecondes );
        // Converti les secondes en Y/M/D/H
        // 31536000 : seconds/year | 2628000 : seconds/month 
        int nYear = ( nStrNombreSecondes / 31536000 );
        int nMonth = ( ( nStrNombreSecondes - ( nYear * 31536000 ) ) / 2628000 ); 
        int nDay = ( ( nStrNombreSecondes - ( nYear * 31536000 + nMonth * 2628000 ) ) / 86400 );
        int nHour = ( ( nStrNombreSecondes - ( nYear * 31536000 + nMonth * 2628000 + nDay * 86400 ) ) / 3600 );
        // Set le timeRecord
        timedRecord.setYear( nYear );
        timedRecord.setMonth( nMonth );
        timedRecord.setDay( nDay );
        timedRecord.setHour( nHour );
        return timedRecord;
    }

    /**
     *
     * @param listQueryRecords
     * @param nLimit
     * @return The list of the QueryRecords after the last daemon
     */
    public List<TermRecord> getSearchStatsAfterLastDaemon( List<QueryRecord> listQueryRecords, int nLimit )
    {
        TimedRecord timeReport = getIntervalTimeRecord( );
        if ( timeReport.getYear( ) == 0 && timeReport.getMonth( ) == 0 && timeReport.getDay( ) == 0 )
        {
            timeReport.setDay( 1 );
        }
        return getSearchStatsAfterdate( timeReport.getYear( ), timeReport.getMonth( ), timeReport.getDay( ), timeReport.getHour( ), listQueryRecords, nLimit );
    }

    /**
     * @param timeReport
     *            The time from which the query can be taken into account
     * @param query
     * @return Verify if the queriy has been done after the timeReport
     */

    public static boolean checkAfterDuration( TimedRecord timeReport, QueryRecord query )

    {
        LocalDateTime compare = LocalDateTime.now( ).minus( timeReport.getYear( ), ChronoUnit.YEARS )
        											.minus( timeReport.getMonth( ), ChronoUnit.MONTHS )
        											.minus( timeReport.getDay( ), ChronoUnit.DAYS )
        											.minus( timeReport.getHour( ), ChronoUnit.HOURS );
        LocalDateTime queryLocalTime = LocalDateTime.of( query.getYear( ), query.getMonth( ), query.getDay( ), query.getHour( ), 0 );
        return ( queryLocalTime.compareTo( compare ) >= 0 );
    }
}
