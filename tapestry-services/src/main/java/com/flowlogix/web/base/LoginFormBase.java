/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.web.base;

import com.flowlogix.web.components.security.LoginForm;
import com.flowlogix.web.mixins.ExternalPageLink;
import com.flowlogix.web.services.SecurityModule;
import java.io.IOException;
import lombok.SneakyThrows;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tynamo.security.services.PageService;
import org.tynamo.security.services.SecurityService;

/**
 * <a href="http://code.google.com/p/flowlogix/wiki/TLLoginBase"
 *    target="_blank">See Documentation</a>
 * 
 * @author lprimak
 */
@Import(library = "DetectJS.js")
public class LoginFormBase
{
    @SneakyThrows({InterruptedException.class})
    public Object login(String tynamoLogin, String tynamoPassword, boolean tynamoRememberMe, String host) throws ShiroException
    {
        Subject currentUser = securityService.getSubject();

        if (currentUser == null)
        {
            throw new IllegalStateException("Subject can`t be null");
        }

        UsernamePasswordToken token = new UsernamePasswordToken(tynamoLogin, tynamoPassword);
        token.setRememberMe(tynamoRememberMe);
        token.setHost(host);

        try
        {
            currentUser.login(token);
        } catch(AuthenticationException ae)
        {
            if(authDelayInterval > 0)
            {
                Thread.sleep(authDelayInterval * 1000);
            }
            throw ae;
        }

        SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(requestGlobals.getHTTPServletRequest());
        final String successLink = externalLink.createLink(pageService.getSuccessPage());

        if (savedRequest != null && savedRequest.getMethod().equalsIgnoreCase("GET"))
        {
            try
            {
                response.sendRedirect(savedRequest.getRequestUrl());
                return null;
            } catch (IOException e)
            {
                logger.warn("Can't redirect to saved request.");
                return successLink;
            }
        } else
        {
            return successLink;
        }
    }
    
    
    @SetupRender
    public void resetJavaScriptDisabled()
    {
        javaScriptDisabled = true;
    }
    
    
    @AfterRender
    public void detectJavaScript()
    {
        Link link = componentResources.createEventLink(ENABLE_JS_EVENT);
        String eventURI = link.toAbsoluteURI(isSecure);
        jsSupport.addInitializerCall("detectJS", eventURI);
    }
    
    
    @OnEvent(value = ENABLE_JS_EVENT)
    public void enableJavaScriptAvail()
    {
        javaScriptDisabled = false;
    }
    

    private @Inject Response response;
    private @Inject RequestGlobals requestGlobals;
    private @Inject SecurityService securityService;
    private @Inject PageService pageService;
    private @Inject @Symbol(SymbolConstants.SECURE_ENABLED) boolean isSecure;  
    private @Inject @Symbol(SecurityModule.Symbols.INVALID_AUTH_DELAY) int authDelayInterval;
    
    private @Environmental JavaScriptSupport jsSupport;
    private @Inject ComponentResources componentResources;
    private @SessionAttribute Boolean javaScriptDisabled;
    private @Mixin ExternalPageLink externalLink;

    public static final String ENABLE_JS_EVENT = "enableJSOnLogin";
    private static final Logger logger = LoggerFactory.getLogger(LoginForm.class);
}
