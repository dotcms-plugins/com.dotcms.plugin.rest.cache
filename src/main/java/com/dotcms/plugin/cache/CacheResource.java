package com.dotcms.plugin.cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotcms.enterprise.cache.provider.CacheProviderAPIImpl;
import com.dotcms.repackage.javax.ws.rs.GET;
import com.dotcms.repackage.javax.ws.rs.Path;
import com.dotcms.repackage.javax.ws.rs.PathParam;
import com.dotcms.repackage.javax.ws.rs.core.Context;
import com.dotcms.repackage.javax.ws.rs.core.Response;
import com.dotcms.rest.annotation.NoCache;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.business.cache.provider.CacheProvider;
import com.dotmarketing.util.json.JSONArray;

/**
 * 
 * 
 * Call
 *
 */
@Path("/v1/cache")
public class CacheResource {


  @Context
  private HttpServletRequest httpRequest;

  private Method method = null;
  private CacheProviderAPIImpl providerAPI = null;

  private final List<CacheProvider> getProviders(final String group) {

    if (providerAPI == null) {
      providerAPI = (CacheProviderAPIImpl) APILocator.getCacheProviderAPI();
      try {
        method = providerAPI.getClass().getMethod("getProvidersForRegion", new Class[] {String.class});
        method.setAccessible(true);
      } catch (NoSuchMethodException | SecurityException e) {
        throw new DotStateException(e);
      }
    }

    try {
      return (List<CacheProvider>) method.invoke(providerAPI, group);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new DotStateException(e);
    }

  }

  private final CacheProvider getProvider(final String group, final String provider) {

    List<CacheProvider> providers = getProviders(group);

    for (CacheProvider cache : providers) {
      if (cache.getName().equals(group)) {
        return cache;
      }

    }
    throw new DotStateException("Unable to find " + provider + " provider for :" + group);



  }



  @NoCache
  @GET
  @Path("/providers/{group: .*}")
  public Response showProviders(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response, @PathParam("group") final String group) {

    final Response.ResponseBuilder responseBuilder = Response.ok(getProviders(group).toString());
    return responseBuilder.build();
  }


  @NoCache
  @GET
  @Path("/provider/{provider: .*}/{group: .*}")
  public Response showProviders(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response, @PathParam("provider") final String provider, @PathParam("group") final String group) {

    final Response.ResponseBuilder responseBuilder = Response.ok(getProvider(provider, group).toString());
    return responseBuilder.build();
  }



  @NoCache
  @GET
  @Path("/provider/{provider: .*}/keys/{group: .*}")
  public Response getKeys(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response, @PathParam("provider") final String provider,  @PathParam("group") final String group) {


    Set<String> keys = new HashSet<>();
    keys.addAll(getProvider(provider, group).getKeys(group));

    JSONArray jo = new JSONArray(keys);
    final Response.ResponseBuilder responseBuilder = Response.ok(jo.toString());
    return responseBuilder.build();
  }

  @NoCache
  @GET
  @Path("/provider/{provider: .*}/object/{group: .*}/{id: .*}")
  public Response showObject(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response,@PathParam("provider") final String provider,  @PathParam("group") final String group,
      @PathParam("id") final String id) {

    Object obj = getProvider(provider, group).get(group, id);

    obj = (obj == null) ? "NOPE" : obj;
    // JSONObject jo = new JSONObject(obj);

    final Response.ResponseBuilder responseBuilder = Response.ok(obj.toString());
    return responseBuilder.build();
  }

  
  @NoCache
  @GET
  @Path("/provider/{provider: .*}/flush/{group: .*}")
  public Response flushGroup(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response, @PathParam("provider") final String provider,  @PathParam("group") final String group) {

    getProvider(provider, group).remove(group);
    final Response.ResponseBuilder responseBuilder = Response.ok("flushed");
    return responseBuilder.build();
  }
  
  
  
  
  
  @NoCache
  @GET
  @Path("/provider/{provider: .*}/flush/{group: .*}/{id: .*}")
  public Response flushObject(@Context final HttpServletRequest request,
      @Context final HttpServletResponse response,@PathParam("provider") final String provider,  @PathParam("group") final String group,
      @PathParam("id") final String id) {
    getProvider(provider, group).remove(group, id);
    final Response.ResponseBuilder responseBuilder = Response.ok("flushed");
    return responseBuilder.build();
  }
}
