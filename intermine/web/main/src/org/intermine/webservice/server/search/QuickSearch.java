package org.intermine.webservice.server.search;

/*
 * Copyright (C) 2002-2016 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.api.InterMineAPI;
import org.intermine.api.bag.BagManager;
import org.intermine.api.profile.InterMineBag;
import org.intermine.api.profile.Profile;
import org.intermine.api.search.SearchResults;
import org.intermine.api.search.SearchResult;
import org.intermine.web.context.InterMineContext;
import org.intermine.web.logic.RequestUtil;
import org.intermine.web.logic.config.WebConfig;
import org.intermine.web.logic.export.Exporter;
import org.intermine.web.logic.export.ResponseUtil;
import org.intermine.web.search.KeywordSearchResult;
import org.intermine.web.search.SearchUtils;
import org.intermine.webservice.server.core.JSONService;
import org.intermine.webservice.server.exceptions.BadRequestException;
import org.intermine.webservice.server.output.JSONFormatter;
import org.intermine.webservice.server.output.Output;
import org.intermine.webservice.server.output.StreamedOutput;
import org.intermine.webservice.server.output.XMLFormatter;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

/**
 * A service that runs key-word searches.
 * @author Alex Kalderimis
 *
 */
public class QuickSearch extends JSONService
{

    private static final Logger LOG = Logger.getLogger(QuickSearch.class);

    private Map<String, Map<String, Object>> headerObjs
        = new HashMap<String, Map<String, Object>>();

    private final ServletContext servletContext;

    /**
     * @param im The InterMine state object
     * @param ctx The servlet context so that the index can be located.
     */
    public QuickSearch(InterMineAPI im, ServletContext ctx) {
        super(im);
        this.servletContext = ctx;
    }

    @Override
    protected void execute() throws Exception {
        String contextPath = servletContext.getRealPath("/");
        WebConfig wc = InterMineContext.getWebConfig();
        QuickSearchRequest input = new QuickSearchRequest();
        QueryResponse results = SearchResults.doFilteredSearch(input.searchTerm);
        SolrDocumentList rs = results.getResults();
        long numFound = rs.getNumFound();
        int current = 0;
        for(int j = 0; j < rs.size(); j++) {
            SolrDocument sdoc = rs.get(j);
            System.out.println("************************************************************** " + sdoc + "   " + numFound);
            output.addResultItem(Arrays.asList("************************************************************** " + sdoc + "   " + numFound));
        }
//            SolrDocument doc = iter.next();
//            Map<String, Collection<Object>> values = doc.getFieldValuesMap();
//
//            Iterator<String> names = doc.getFieldNames().iterator();
//            while (names.hasNext()) {
//                String name = names.next();
//                System.out.print(name);
//                System.out.print(" = ");
//
//                Collection<Object> vals = values.get(name);
//                Iterator<Object> valsIter = vals.iterator();
//                while (valsIter.hasNext()) {
//                    Object obj = valsIter.next();
//                    System.out.println(obj.toString());
//                }
//            }
//        }
    }

    @Override
    protected Map<String, Object> getHeaderAttributes() {
        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.putAll(super.getHeaderAttributes());
        if (formatIsJSON()) {
            attributes.put(JSONFormatter.KEY_INTRO, "\"results\":[");
            attributes.put(JSONFormatter.KEY_OUTRO, "]");
            attributes.put(JSONFormatter.KEY_HEADER_OBJS, headerObjs);
        }
        return attributes;
    }

    private class QuickSearchRequest
    {

        private final String searchTerm;
        private final int offset;
        private final Integer limit;
        private final String searchBag;

        QuickSearchRequest() {

            String query = request.getParameter("q");
            if (StringUtils.isBlank(query)) {
                searchTerm = "*:*";
            } else {
                searchTerm = query;
            }
            LOG.debug(String.format("SEARCH TERM: '%s'", searchTerm));

            String limitParam = request.getParameter("size");
            Integer lim = null;
            if (!StringUtils.isBlank(limitParam)) {
                try {
                    lim = Integer.valueOf(limitParam);
                } catch (NumberFormatException e) {
                    throw new BadRequestException("Expected a number for size: got " + limitParam);
                }
            }
            this.limit = lim;

            String offsetP = request.getParameter("start");
            int parsed = 0;
            if (!StringUtils.isBlank(offsetP)) {
                try {
                    parsed = Integer.valueOf(offsetP);
                } catch (NumberFormatException e) {
                    throw new BadRequestException("Expected a number for start: got " + offsetP);
                }
            }
            offset = parsed;

            searchBag = request.getParameter("list");
        }

        public boolean wantsMore(int i) {
            if (limit == null) {
                return true;
            }
            return i < limit;
        }


        public String toString() {
            return String.format("<%s searchTerm=%s offset=%d>",
                    getClass().getName(), searchTerm, offset);
        }

        public List<Integer> getListIds() {
            List<Integer> ids = new ArrayList<Integer>();
            if (!StringUtils.isBlank(searchBag)) {
                LOG.debug("SEARCH BAG: '" + searchBag + "'");
                final BagManager bm = im.getBagManager();
                final Profile p = getPermission().getProfile();
                final InterMineBag bag = bm.getBag(p, searchBag);
                if (bag == null) {
                    throw new BadRequestException(
                            "You do not have access to a bag named '" + searchBag + "'");
                }
                ids.addAll(bag.getContentsAsIds());
            }
            return ids;
        }
    }

    private class QuickSearchXMLFormatter extends XMLFormatter
    {
        @Override
        public String formatResult(List<String> resultRow) {
            return StringUtils.join(resultRow, "");
        }
    }

    private QuickSearchResultProcessor getProcessor() {
        if (formatIsJSON()) {
            return new QuickSearchJSONProcessor();
        } else if (formatIsXML()) {
            return new QuickSearchXMLProcessor();
        } else {
            final String separator;
            if (RequestUtil.isWindowsClient(request)) {
                separator = Exporter.WINDOWS_SEPARATOR;
            } else {
                separator = Exporter.UNIX_SEPARATOR;
            }
            return new QuickSearchTextProcessor(separator);
        }
    }

    @Override
    protected Output makeXMLOutput(PrintWriter out, String separator) {
        ResponseUtil.setXMLHeader(response, "search.xml");
        return new StreamedOutput(out, new QuickSearchXMLFormatter());
    }


}
