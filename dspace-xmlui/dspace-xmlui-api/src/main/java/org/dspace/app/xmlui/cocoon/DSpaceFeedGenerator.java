/*
 * DSpaceFeedGenerator.java
 *
 * Version: $Revision: 4511 $
 *
 * Date: $Date: 2009-11-06 04:26:26 +0000 (Fri, 06 Nov 2009) $
 *
 * Copyright (c) 2002, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.dspace.app.xmlui.cocoon;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.util.HashUtil;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.SourceValidity;
import org.apache.log4j.Logger;

import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import org.dspace.app.xmlui.utils.FeedUtils;
import org.dspace.app.util.SyndicationFeed;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.browse.BrowseEngine;
import org.dspace.browse.BrowseException;
import org.dspace.browse.BrowseIndex;
import org.dspace.browse.BrowseItem;
import org.dspace.browse.BrowserScope;
import org.dspace.sort.SortException;
import org.dspace.sort.SortOption;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DCDate;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataSchema;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.handle.HandleManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Description;
import com.sun.syndication.feed.rss.Image;
import com.sun.syndication.feed.module.DCModuleImpl;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

/**
 *
 * Generate a syndication feed for DSpace, either a community or collection
 * or the whole repository. This code was adapted from the syndication found
 * in DSpace's JSP implementation, "org.dspace.app.webui.servlet.FeedServlet".
 *
 * Once thing that has been modified from DSpace's JSP implementation is what
 * is placed inside an item's description, we've changed it so that the list
 * of metadata fields is scanned until a value is found and the first one
 * found is used as the description. This means that we look at the abstract,
 * description, alternative title, title, etc... to and the first metadata
 * value found is used.
 *
 * I18N: Feed's are internationalized, meaning that they may contain refrences
 * to messages contained in the global messages.xml file using cocoon's i18n
 * schema. However the library used to build the feeds does not understand
 * this schema to work around this limitation I created a little hack. It
 * basicaly works like this, when text that needs to be localized is put into
 * the feed it is allways mangled such that a prefix is added to the messages's
 * key. Thus if the key were "xmlui.feed.text" then the resulting text placed
 * into the feed would be "I18N:xmlui.feed.text". After the library is finished
 * and produced it's final result the output is traversed to find these
 * occurances ande replace them with proper cocoon i18n elements.
 *
 *
 *
 * @author Scott Phillips, Ben Bosman, Richard Rodgers
 */

public class DSpaceFeedGenerator extends AbstractGenerator
                implements Configurable, CacheableProcessingComponent, Recyclable
{
    private static final Logger log = Logger.getLogger(DSpaceFeedGenerator.class);

    /** The feed's requested format */
    private String format = null;
    
    /** The feed's scope, null if no scope */
    private String handle = null;
    
    /** number of DSpace items per feed */
    private static int itemCount = 0;
    
    /**
     * How long should RSS feed cache entries be valid? milliseconds * seconds *
     * minutes * hours default to 24 hours if config parameter is not present or
     * wrong
     */
    private static final long CACHE_AGE;
    static
    {
        final String ageCfgName = "webui.feed.cache.age";
        final long ageCfg = ConfigurationManager.getIntProperty(ageCfgName, 24);
        CACHE_AGE = 1000 * 60 * 60 * ageCfg;
    }
    
        /**     default fields to display in item description */
    private static String defaultDescriptionFields = "dc.description.abstract, dc.description, dc.title.alternative, dc.title";

    /** configuration option to include Item which does not have READ by Anonymous enabled **/
    private static boolean includeRestrictedItems = ConfigurationManager.getBooleanProperty("harvest.includerestricted.rss", true);


    /** Cache of this object's validitity */
    private DSpaceValidity validity = null;
    
    /** The cache of recently submitted items */
    private Item recentSubmissionItems[];
    
    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey()
    {
        String key = "key:" + this.handle + ":" + this.format;
        return HashUtil.hash(key);
    }

    /**
     * Generate the cache validity object.
     *
     * The validity object will include the collection being viewed and
     * all recently submitted items. This does not include the community / collection
     * hierarch, when this changes they will not be reflected in the cache.
     */
    public SourceValidity getValidity()
    {
        if (this.validity == null)
        {
            try
            {
                DSpaceValidity validity = new FeedValidity();
                
                Context context = ContextUtil.obtainContext(objectModel);

                DSpaceObject dso = null;
                
                if (handle != null && !handle.contains("site"))
                    dso = HandleManager.resolveToObject(context, handle);
                
                validity.add(dso);
                
                // add reciently submitted items
                for(Item item : getRecentlySubmittedItems(context,dso))
                {
                    validity.add(item);
                }

                this.validity = validity.complete();
            }
            catch (Exception e)
            {
                // Just ignore all errors and return an invalid cache.
            }
        }
        return this.validity;
    }
    
    
    
    /**
     * Setup component wide configuration
     */
    public void configure(Configuration conf) throws ConfigurationException
    {
        itemCount = ConfigurationManager.getIntProperty("webui.feed.items");
    }
    
    
    /**
     * Setup configuration for this request
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
            Parameters par) throws ProcessingException, SAXException,
            IOException
    {
        super.setup(resolver, objectModel, src, par);
        
        this.format = par.getParameter("feedFormat", null);
        this.handle = par.getParameter("handle",null);
    }
    
    
    /**
     * Generate the syndication feed.
     */
    public void generate() throws IOException, SAXException, ProcessingException
    {
        try
        {
            Context context = ContextUtil.obtainContext(objectModel);
            DSpaceObject dso = null;
            
            if (handle != null && !handle.contains("site"))
            {
                dso = HandleManager.resolveToObject(context, handle);
                if (dso == null)
                {
                    // If we were unable to find a handle then return page not found.
                    throw new ResourceNotFoundException("Unable to find DSpace object matching the given handle: "+handle);
                }
                
                if (!(dso.getType() == Constants.COLLECTION || dso.getType() == Constants.COMMUNITY))
                {
                    // The handle is valid but the object is not a container.
                    throw new ResourceNotFoundException("Unable to syndicate DSpace object: "+handle);
                }
            }
        
            SyndicationFeed feed = new SyndicationFeed(SyndicationFeed.UITYPE_XMLUI);
            feed.populate(ObjectModelHelper.getRequest(objectModel),
                          dso, getRecentlySubmittedItems(context,dso), FeedUtils.i18nLabels);
            feed.setType(this.format);
            Document dom = feed.outputW3CDom();
            FeedUtils.unmangleI18N(dom);
            DOMStreamer streamer = new DOMStreamer(contentHandler, lexicalHandler);
            streamer.stream(dom);
        }
        catch (IllegalArgumentException iae)
        {
                throw new ResourceNotFoundException("Syndication feed format, '"+this.format+"', is not supported.");
        }
        catch (FeedException fe)
        {
                throw new SAXException(fe);
        }
        catch (SQLException sqle)
        {
                throw new SAXException(sqle);
        }
    }
    
    /**
     * @return recently submitted Items within the indicated scope
     */
    @SuppressWarnings("unchecked")
    private Item[] getRecentlySubmittedItems(Context context, DSpaceObject dso)
            throws SQLException
    {
        if (recentSubmissionItems != null)
                return recentSubmissionItems;

        String source = ConfigurationManager.getProperty("recent.submissions.sort-option");
        BrowserScope scope = new BrowserScope(context);
        if (dso instanceof Collection)
                scope.setCollection((Collection) dso);
        else if (dso instanceof Community)
                scope.setCommunity((Community) dso);
        scope.setResultsPerPage(itemCount);

        // FIXME Exception handling
        try
        {
            scope.setBrowseIndex(BrowseIndex.getItemBrowseIndex());
            for (SortOption so : SortOption.getSortOptions())
            {
                if (so.getName().equals(source))
                {
                    scope.setSortBy(so.getNumber());
                    scope.setOrder(SortOption.DESCENDING);
                }
            }

            BrowseEngine be = new BrowseEngine(context);
            this.recentSubmissionItems = be.browseMini(scope).getItemResults(context);

            // filter out Items taht are not world-readable
            if (!includeRestrictedItems)
            {
                List<Item> result = new ArrayList<Item>();
                for (Item item : this.recentSubmissionItems)
                {
                checkAccess:
                    for (Group group : AuthorizeManager.getAuthorizedGroups(context, item, Constants.READ))
                    {
                        if ((group.getID() == 0))
                        {
                            result.add(item);
                            break checkAccess;
                        }
                    }
                }
                this.recentSubmissionItems = result.toArray(new Item[result.size()]);
            }
        }
        catch (BrowseException bex)
        {
            log.error("Caught browse exception", bex);
        }
        catch (SortException e)
        {
            log.error("Caught sort exception", e);
        }
        return this.recentSubmissionItems;
    }
    
    /**
     * Recycle
     */
    
    public void recycle()
    {
        this.format = null;
        this.handle = null;
        this.validity = null;
        this.recentSubmissionItems = null;
        super.recycle();
    }
    
    /**
     * Extend the standard DSpaceValidity object to support assumed
     * caching. Since feeds will constantly be requested we want to
     * assume that a feed is still valid instead of checking it
     * against the database anew everytime.
     *
     * This validity object will assume that a cache is still valid,
     * without rechecking it, for 24 hours.
     *
     */
    private class FeedValidity extends DSpaceValidity
    {
                private static final long serialVersionUID = 1L;
                        
        /** When the cache's validity expires */
        private long expires = 0;
        
        /**
         * When the validity is completed record a timestamp to check later.
         */
        public DSpaceValidity complete()
        {
                this.expires = System.currentTimeMillis() + CACHE_AGE;
                
                return super.complete();
        }
        
        
        /**
         * Determine if the cache is still valid
         */
        public int isValid()
        {
            // Return true if we have a hash.
            if (this.completed)
            {
                if (System.currentTimeMillis() < this.expires)
                {
                        // If the cache hasn't expired the just assume that it is still valid.
                        return SourceValidity.VALID;
                }
                else
                {
                        // The cache is past its age
                        return SourceValidity.UNKNOWN;
                }
            }
            else
            {
                // This is an error, state. We are being asked whether we are valid before
                // we have been initialized.
                return SourceValidity.INVALID;
            }
        }

        /**
         * Determine if the cache is still valid based
         * upon the other validity object.
         *
         * @param other
         *          The other validity object.
         */
        public int isValid(SourceValidity otherValidity)
        {
            if (this.completed)
            {
                if (otherValidity instanceof FeedValidity)
                {
                    FeedValidity other = (FeedValidity) otherValidity;
                    if (hash == other.hash)
                    {
                        // Update both cache's expiration time.
                        this.expires = System.currentTimeMillis() + CACHE_AGE;
                        other.expires = System.currentTimeMillis() + CACHE_AGE;
                        
                        return SourceValidity.VALID;
                    }
                }
            }

            return SourceValidity.INVALID;
        }

    }
}
