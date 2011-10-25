package com.flowlogix.web.services.internal;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.internal.services.RequestImpl;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.internal.services.ResponseImpl;
import org.apache.tapestry5.internal.services.TapestrySessionFactory;
import org.apache.tapestry5.internal.services.assets.ContextAssetRequestHandler;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.HttpServletRequestFilter;
import org.apache.tapestry5.services.HttpServletRequestHandler;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;

/**
 * Cache and gzip compress GWT assets outside of Tapestry control
 * 
 * @author lprimak
 */
public class GwtCachingFilter implements HttpServletRequestFilter
{
    public GwtCachingFilter(ResourceStreamer streamer, @Service("ContextAssetFactory") AssetFactory contextAssetFactory,
            TapestrySessionFactory sessionFactory, RequestGlobals rg)
    {
        this.carh = new ContextAssetRequestHandler(streamer, contextAssetFactory.getRootResource());
        this.sessionFactory = sessionFactory;
        this.rg = rg;
    }

    
    @Override
    public boolean service(HttpServletRequest request, HttpServletResponse response, HttpServletRequestHandler chainHandler) throws IOException
    {
        String path = request.getServletPath();

        boolean neverExpire = false;
        if (!path.startsWith(RequestConstants.ASSET_PATH_PREFIX))
        {
            if (path.endsWith(".cache.html"))
            {
                neverExpire = true;
            } 
            else
            {
                if (path.endsWith(".nocache.js"))
                {
                    response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
                    response.setHeader("Pragma", "no-cache");        //HTTP 1.0
                    response.setDateHeader("Expires", 0);
                }
            }
        }

        if (neverExpire == false)
        {
            return chainHandler.service(request, response);
        }

        log.finer("GwtCachingFilter: Processing " + path);

        Request rq = new RequestImpl(request, applicationCharset, sessionFactory);
        Response rsp = new ResponseImpl(request, response);
        rg.storeRequestResponse(rq, rsp);

        rsp.setDateHeader("Expires", new Date().getTime() + InternalConstants.TEN_YEARS);

        try
        {
            return carh.handleAssetRequest(rq, rsp, path);
        }
        catch(Exception e)
        {
            return chainHandler.service(request, response);
        }
    }
    
    
    private final ContextAssetRequestHandler carh;
    private final TapestrySessionFactory sessionFactory;
    private @Inject @Symbol(SymbolConstants.CHARSET) String applicationCharset;
    private final RequestGlobals rg;
    
    private static final Logger log = Logger.getLogger(GwtCachingFilter.class.getName());
}
