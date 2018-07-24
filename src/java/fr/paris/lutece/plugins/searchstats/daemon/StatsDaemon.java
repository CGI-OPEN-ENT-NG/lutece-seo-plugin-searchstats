package fr.paris.lutece.plugins.searchstats.daemon;

import java.util.Locale;

import fr.paris.lutece.plugins.searchstats.service.SearchStatsService;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.i18n.I18nService;

public class StatsDaemon extends Daemon
{

    @Override
    public void run( )
    {
        int nNbMail = SearchStatsService.sendEmail( );
        setLastRunLogs(I18nService.getLocalizedString("searchstats.StatsDaemon.last_run_log", Locale.getDefault())+String.valueOf(nNbMail));
    }
}
