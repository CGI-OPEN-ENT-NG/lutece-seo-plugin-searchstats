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
package fr.paris.lutece.plugins.searchstats.web;

import fr.paris.lutece.plugins.searchstats.business.QueryRecord;
import fr.paris.lutece.plugins.searchstats.business.QueryRecordHome;
import fr.paris.lutece.plugins.searchstats.business.RecordCount;
import fr.paris.lutece.plugins.searchstats.business.RecordFilter;
import fr.paris.lutece.plugins.searchstats.service.TermRecord;
import fr.paris.lutece.plugins.searchstats.service.TermRecordService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;


/*
 * This class provides the user interface to manage adminquery features
 */
public class SearchStatsJspBean extends PluginAdminPageJspBean
{
    /////////////////////////////////////////////////////////////////////////////////////////
    // Constants

    // Right
    public static final String RIGHT_MANAGE_SEARCH_STATS = "SEARCH_STATS_MANAGEMENT";

    //Templates
    private static final String TEMPLATE_MANAGE_SEARCH_STATS = "admin/plugins/searchstats/manage_searchstats.html";

    // Boomarks
    private static final String MARK_DATES = "records_list";
    private static final String MARK_QUERIES = "queries_list";
    private static final String MARK_TERMS = "terms_list";
    private static final String MARK_NO_RESULT_STATE = "no_result_state";
    private static final String MARK_DISPLAY_DETAILS = "display_details";
    private static final String MARK_DETAIL_DATE = "detail_date";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";

    // Parameters
    private static final String PARAMETER_YEAR = "year";
    private static final String PARAMETER_MONTH = "month";
    private static final String PARAMETER_DAY = "day";
    private static final String PARAMETER_NO_RESULT = "no_result";
    private static final String PARAMETER_ACTION = "action";
    private static final String ACTION_VIEW = "view";
    private static final String ACTION_REFRESH = "refresh";
    private static final String PARAMETER_PAGE_INDEX_DATES = "page_index_dates";
    private static final int DEFAULT_ITEMS_PER_PAGE = 20;
    private static final String PROPERTY_FORMAT_YEAR = "searchstats.format.year";
    private static final String PROPERTY_FORMAT_MONTH = "searchstats.format.month";
    private static final String PROPERTY_FORMAT_DAY = "searchstats.format.day";
    private int _nItemPerPageDates = 0;
    private String _strPageIndexDates;
    private boolean _bShowOnlyNoResultQueries;

    public String manageSearchStats( HttpServletRequest request )
    {
        String strYear = request.getParameter( PARAMETER_YEAR );
        String strMonth = request.getParameter( PARAMETER_MONTH );
        String strDay = request.getParameter( PARAMETER_DAY );
        String strAction = request.getParameter( PARAMETER_ACTION );
        String strNoResultQueries = request.getParameter( PARAMETER_NO_RESULT );

        RecordFilter filter = new RecordFilter(  );

        boolean bDisplayDetails = ( strAction != null ) && ( strAction.equals( ACTION_VIEW ) );

        if ( strYear != null )
        {
            filter.setYear( Integer.parseInt( strYear ) );
        }

        if ( strMonth != null )
        {
            filter.setMonth( Integer.parseInt( strMonth ) );
        }

        if ( strDay != null )
        {
            filter.setDay( Integer.parseInt( strDay ) );
        }

        if ( ( strAction != null ) && strAction.equals( ACTION_REFRESH ) )
        {
            _bShowOnlyNoResultQueries = ( strNoResultQueries != null ) ? true : false;
        }

        filter.setNoResults( _bShowOnlyNoResultQueries );

        List<RecordCount> listDates = QueryRecordHome.findRecordsDatesList( getPlugin(  ), _bShowOnlyNoResultQueries );
        List<QueryRecord> listQueries;

        if ( ( strAction != null ) && strAction.equals( ACTION_VIEW ) )
        {
            listQueries = QueryRecordHome.findQueryRecordsListByCriteria( getPlugin(  ), filter );
        }
        else
        {
            listQueries = new ArrayList<QueryRecord>(  );
        }

        List<TermRecord> listTopTerms = TermRecordService.getTopTerms( listQueries );
        String strDetailLabel = getDetailDateLabel( strYear, strMonth, strDay );

        _nItemPerPageDates = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE,
                _nItemPerPageDates, DEFAULT_ITEMS_PER_PAGE );
        _strPageIndexDates = Paginator.getPageIndex( request, PARAMETER_PAGE_INDEX_DATES, _strPageIndexDates );

        LocalizedPaginator paginatorDates = new LocalizedPaginator( listDates, _nItemPerPageDates, getHomeUrl( request ),
                PARAMETER_PAGE_INDEX_DATES, _strPageIndexDates , getLocale() );

        HashMap model = new HashMap(  );
        model.put( MARK_DATES, paginatorDates.getPageItems(  ) );
        model.put( MARK_PAGINATOR, paginatorDates );
        model.put( MARK_NB_ITEMS_PER_PAGE, "" + _nItemPerPageDates );
        model.put( MARK_DISPLAY_DETAILS, bDisplayDetails );
        model.put( MARK_DETAIL_DATE, strDetailLabel );
        model.put( MARK_QUERIES, listQueries );
        model.put( MARK_TERMS, listTopTerms );
        model.put( MARK_NO_RESULT_STATE, _bShowOnlyNoResultQueries ? "checked=\"on\"" : "" );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_SEARCH_STATS, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    private String getDetailDateLabel( String strYear, String strMonth, String strDay )
    {
        String strDateLabel = "";

        Calendar c = new GregorianCalendar(  );

        if ( strYear != null )
        {
            String strPattern;
            c.set( Calendar.YEAR, Integer.parseInt( strYear ) );

            if ( strMonth != null )
            {
                c.set( Calendar.MONTH, Integer.parseInt( strMonth ) - 1 );

                if ( strDay != null )
                {
                    c.set( Calendar.DAY_OF_MONTH, Integer.parseInt( strDay ) );
                    strPattern = I18nService.getLocalizedString( PROPERTY_FORMAT_DAY, getLocale(  ) );
                }
                else
                {
                    strPattern = I18nService.getLocalizedString( PROPERTY_FORMAT_MONTH, getLocale(  ) );
                }
            }
            else
            {
                strPattern = I18nService.getLocalizedString( PROPERTY_FORMAT_YEAR, getLocale(  ) );
            }

            strDateLabel = getFormattedDate( c, strPattern );
        }

        return strDateLabel;
    }

    private String getFormattedDate( Calendar c, String strFormatPattern )
    {
        SimpleDateFormat formatter = new SimpleDateFormat( strFormatPattern, getLocale(  ) );

        return formatter.format( c.getTime(  ) );
    }
}
