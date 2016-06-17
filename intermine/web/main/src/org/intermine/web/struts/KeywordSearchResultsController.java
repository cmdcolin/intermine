package org.intermine.web.struts;

/*
 * Copyright (C) 2002-2016 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.actions.TilesAction;
import org.intermine.api.InterMineAPI;
import org.intermine.api.profile.InterMineBag;
import org.intermine.api.profile.Profile;
import org.intermine.api.search.SearchResults;
import org.intermine.web.logic.config.WebConfig;
import org.intermine.web.logic.session.SessionMethods;
import org.intermine.web.search.KeywordSearchResult;
import org.intermine.web.search.SearchUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Controller for keyword search.
 *
 * @author nils
 */
public class KeywordSearchResultsController extends TilesAction
{
    private static final String QUERY_TERM_ALL = "*:*";
    private static final Logger LOG = Logger.getLogger(KeywordSearchResultsController.class);
    private static Logger searchLog = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ComponentContext context,
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // term
        String searchTerm = request.getParameter("searchTerm");
        LOG.debug("SEARCH TERM: '" + searchTerm + "'");

        // show overview by default
        if (StringUtils.isBlank(searchTerm)) {
            return null;
            // searchTerm = QUERY_TERM_ALL;
        }

        long time = System.currentTimeMillis();
        final InterMineAPI im = SessionMethods.getInterMineAPI(request.getSession());
        ServletContext servletContext = request.getSession().getServletContext();
        String contextPath = servletContext.getRealPath("/");
        int totalHits = 0;

        //track the keyword search
        Profile profile = SessionMethods.getProfile(request.getSession());
        im.getTrackerDelegate().trackKeywordSearch(searchTerm, profile,
                request.getSession().getId());
        WebConfig wc = SessionMethods.getWebConfig(request);

        // search in bag (list)
        String searchBag = request.getParameter("searchBag");
        if (searchBag == null) {
            searchBag = "";
        }
        List<Integer> ids = getBagIds(im, request, searchBag);
        int offset = getOffset(request);
        LOG.debug("Initializing took " + (System.currentTimeMillis() - time) + " ms");


        long searchTime = System.currentTimeMillis();

        SearchResults results = SearchResults.doFilteredSearch(searchTerm);
        totalHits = results.getNumHits();

        LOG.debug("SEARCH RESULTS FOR " + searchTerm  + ": " + totalHits);

        // don't display *:* in search box
        if (QUERY_TERM_ALL.equals(searchTerm)) {
            searchTerm = "";
        }

        // there are needed in the form too so we have to use request (i think...)
        request.setAttribute("searchResults", results);
        request.setAttribute("searchTerm", searchTerm);
        request.setAttribute("searchBag", searchBag);


        context.putAttribute("searchResults", request.getAttribute("searchResults"));
        context.putAttribute("searchTerm", request.getAttribute("searchTerm"));
        context.putAttribute("searchBag", request.getAttribute("searchBag"));

        // pagination
        context.putAttribute("searchOffset", Integer.valueOf(offset));
        context.putAttribute("searchPerPage", Integer.valueOf(10));
        context.putAttribute("searchTotalHits", Integer.valueOf(totalHits));

        //TODO
        // used for re-running the search in case of creating a list for ALL results
        // facet lists
        // facet values
        

        // time for debugging
        long totalTime = System.currentTimeMillis() - time;
        context.putAttribute("searchTime", new Long(totalTime));
        LOG.debug("--> TOTAL: " + (System.currentTimeMillis() - time) + " ms");
        return null;
    }

    private int getOffset(HttpServletRequest request) {
        // offset (-> paging)
        Integer offset = new Integer(0);
        try {
            if (!StringUtils.isBlank(request.getParameter("searchOffset"))) {
                offset = Integer.valueOf(request.getParameter("searchOffset"));
            }
        } catch (NumberFormatException e) {
            LOG.info("invalid offset", e);
        }
        LOG.debug("SEARCH OFFSET: " + offset + "");
        return offset.intValue();
    }

    private  List<Integer> getBagIds(InterMineAPI im, HttpServletRequest request,
            String searchBag) {
        List<Integer> ids = new ArrayList<Integer>();
        if (!StringUtils.isEmpty(searchBag)) {
            LOG.debug("SEARCH BAG: '" + searchBag + "'");
            InterMineBag bag = im.getBagManager().getBag(
                    SessionMethods.getProfile(request.getSession()), searchBag);
            if (bag != null) {
                ids = bag.getContentsAsIds();
                LOG.debug("SEARCH LIST: " + Arrays.toString(ids.toArray()) + "");
            }
        }
        return ids;
    }
}
