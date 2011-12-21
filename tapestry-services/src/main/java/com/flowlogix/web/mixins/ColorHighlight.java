package com.flowlogix.web.mixins;

import com.flowlogix.web.services.AssetMinimizer;
import javax.validation.constraints.NotNull;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * <a href="http://code.google.com/p/flowlogix/wiki/TLColorHighlight"
 *    target="_blank">See Documentation</a>
 * 
 * @author lprimak
 */
public class ColorHighlight
{
    @SetupRender
    void init()
    {
        js.addScript(script, highlightColor);
    }
    
    
    private @Environmental JavaScriptSupport js;
    private @Parameter(required=true, defaultPrefix=BindingConstants.LITERAL) 
            @NotNull String highlightColor;
    private @Inject @Path("ColorHighlight.js") Asset scriptAsset;
    private @Inject AssetMinimizer minimizer;
    private final String script = minimizer.minimize(scriptAsset);
}