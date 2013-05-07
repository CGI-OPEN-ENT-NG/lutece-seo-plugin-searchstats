/*
 * Copyright (c) 2002-2013, Mairie de Paris
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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.searchstats.business.QueryRecord;
import fr.paris.lutece.plugins.searchstats.business.QueryRecordHome;
import fr.paris.lutece.plugins.searchstats.business.RecordFilter;
import fr.paris.lutece.plugins.searchstats.service.TermRecord;
import fr.paris.lutece.plugins.searchstats.service.TermRecordService;
import fr.paris.lutece.portal.business.right.Right;
import fr.paris.lutece.portal.business.right.RightHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.dashboard.DashboardComponent;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;


/**
 * Search Stats Dashboard Component
 * This component displays today top searched terms
 */
public class SearchStatsDashboardComponent extends DashboardComponent
{
    private static final String TEMPLATE_DASHBOARD = "/admin/plugins/searchstats/searchstats_dashboard.html";
    private static final String MARK_TOP_TERMS = "top_terms_list";
    private static final String MARK_URL = "url";
    private static final String MARK_ICON = "icon";
    private static final String PLUGIN_NAME = "plugin_name";
    private static final String PROPERTY_TOP_TERMS_COUNT = "searchstats.topTermsCount";
    private static final int DEFAULT_TOP_TERMS_COUNT = 5;

    /**
     * The HTML code of the component
     * @param user The Admin User
     * @return The dashboard component
     */
    public String getDashboardData( AdminUser user, HttpServletRequest request )
    {
        Right right = RightHome.findByPrimaryKey( getRight(  ) );
        Plugin plugin = PluginService.getPlugin( right.getPluginName(  ) );
        UrlItem url = new UrlItem( right.getUrl(  ) );
        url.addParameter( PLUGIN_NAME, right.getPluginName(  ) );

        Map<String , Object> model = new HashMap<String , Object>(  );
        model.put( MARK_TOP_TERMS, getTopterms( plugin ) );
        model.put( MARK_URL, url.getUrl(  ) );
        model.put( MARK_ICON, plugin.getIconUrl(  ) );

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_DASHBOARD, user.getLocale(  ), model );

        return t.getHtml(  );
    }

    /**
     * Gets the top terms list
     * @param plugin The plugin
     * @return The list of top terms
     */
    private List<TermRecord> getTopterms( Plugin plugin )
    {
        RecordFilter filter = new RecordFilter(  );

        GregorianCalendar calendar = new GregorianCalendar(  );

        filter.setYear( calendar.get( GregorianCalendar.YEAR ) );
        filter.setMonth( calendar.get( GregorianCalendar.MONTH ) + 1 );
        filter.setDay( calendar.get( GregorianCalendar.DAY_OF_MONTH ) );

        List<QueryRecord> listQueries = QueryRecordHome.findQueryRecordsListByCriteria( plugin, filter );
        List<TermRecord> listTopTermsSorted = TermRecordService.getTopTerms( listQueries );
        List<TermRecord> listTopTerms = new ArrayList<TermRecord>(  );
        int nTermsCount = AppPropertiesService.getPropertyInt( PROPERTY_TOP_TERMS_COUNT, DEFAULT_TOP_TERMS_COUNT );

        for ( int i = 0; ( i < nTermsCount ) && ( i < listTopTermsSorted.size(  ) ); i++ )
        {
            listTopTerms.add( listTopTermsSorted.get( i ) );
        }

        return listTopTerms;
    }
}
