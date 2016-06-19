package org.intermine.api.search;

/*
 * Copyright (C) 2002-2016 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.split;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.SolrServerException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.intermine.api.profile.TagManager;
import org.intermine.api.profile.TagManagerFactory;
import org.intermine.template.TemplateQuery;

/**
 * Search engine results based on Apache Solr
 *   Previously based on lucene
 *
 * @author Alex Kalderimis
 * @author Thomas Riley
 * @author Kim Rutherford
 * @author Colin Diesh
 */
public final class SearchResults implements Iterable<SearchResult>
{
    private static final Logger LOG = Logger.getLogger(SearchResults.class);

    /** The iterator for this iterable **/
    private static final class SearchResultIt implements Iterator<SearchResult>
    {
        private final SearchResults parent;
        private final Iterator<WebSearchable> subiter;

        SearchResultIt(SearchResults parent) {
            this.parent = parent;
            subiter = parent.items.values().iterator();
        }

        @Override
        public boolean hasNext() {
            return subiter.hasNext();
        }


        @Override
        public SearchResult next() {
            WebSearchable n = subiter.next();
            return new SearchResult(n, parent.hits.get(n), parent.descs.get(n), parent.tags.get(n));
        }

        @Override
        public void remove() {
            throw new RuntimeException("Not implemented");
        }

    }

    ///// INSTANCE API /////

    private final Map<WebSearchable, Float> hits = new HashMap<WebSearchable, Float>();
    private final Map<String, WebSearchable> items = new HashMap<String, WebSearchable>();
    private final Map<WebSearchable, String> descs = new HashMap<WebSearchable, String>();
    private final Map<WebSearchable, Set<String>> tags = new HashMap<WebSearchable, Set<String>>();

    // Constructor only available to the static methods below.
    private SearchResults(
            Map<WebSearchable, Float> hitMap,
            Map<String, WebSearchable> items,
            Map<WebSearchable, String> descriptions,
            Map<WebSearchable, Set<String>> itemsTags) {
        this.hits.putAll(hitMap);
        this.items.putAll(items);
        this.descs.putAll(descriptions);
        this.tags.putAll(itemsTags);
    }

    /**
     *
     * @return size
     */
    public int size() {
        return items.size();
    }

    @Override
    public Iterator<SearchResult> iterator() {
        return new SearchResultIt(this);
    }

    ///// STATIC SEARCH API //////


    /**
     * Check if a websearchable is an invalid template.
     * @param webSearchable The item to check.
     * @return True if this a template and it is not valid.
     */
    public static boolean isInvalidTemplate(WebSearchable webSearchable) {
        if (webSearchable instanceof TemplateQuery) {
            TemplateQuery template = (TemplateQuery) webSearchable;
            return !template.isValid();
        }
        return false;
    }


    /**
     * Perform transformations on the query string to make it behave intuitively (for some
     * definition of intuition...).
     * @param origQueryString The original form of the query string.
     * @return A munged and transformed string, that will do what people actually want in Lucene.
     */
    private static String prepareQueryString(String origQueryString) {
        // special case for word ending in "log" eg. "ortholog" - add "orthologue" to the search
        String queryString = origQueryString.replaceAll("(\\w+log\\b)", "$1ue $1");
        queryString = queryString.replaceAll("[^a-zA-Z0-9]", " ").trim();
        queryString = queryString.replaceAll("(\\w+)$", "$1 $1*");
        return queryString;
    }

    /**
     * Actually filter the web searchable items we have to get a reduced list of matches.
     * @param origQueryString A query to filter the items against. Assumes the query
     *                        string is neither null not empty.
     * @param target Information about the scope and type of items to receive.
     * @param profileRepo The repository of the user who wants to find something.
     * @return A set of search results.
     * @throws IOException If there is an issue opening the indices.
     * @throws SolrServerException If there is an issue opening the indices.
     */
    public static SearchResults doFilteredSearch(String origQueryString)
        throws IOException, SolrServerException {

        Map<WebSearchable, String> highlightedDescMap = new HashMap<WebSearchable, String>();

        String queryString = prepareQueryString(origQueryString);

        LOG.info("Searching query "
                + " was:" + origQueryString + " now:" + queryString);

        String urlString = "http://localhost:8983/solr/new_core2";
        SolrClient client = new HttpSolrClient(urlString);

        QueryResponse resp = client.query(new SolrQuery(queryString));

        return null;
    }
    public ArrayList<String> getHits() {
        return new ArrayList<String>();
    }
    public Integer getNumHits() {
        return 0;
    }
    public static Set<Integer> getObjectIds(SearchResults result) {
        return new HashSet<Integer>();
    }
}
