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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.intermine.api.solr.SolrSearchResult;

import org.apache.log4j.Logger;
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
        private final Iterator<SolrSearchResult> subiter;

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
            SolrSearchResult n = subiter.next();
            return new SearchResult(n, parent.hits.get(n), parent.descs.get(n), parent.tags.get(n));
        }

        @Override
        public void remove() {
            throw new RuntimeException("Not implemented");
        }

    }
    private final Map<SolrSearchResult, Float> hits = new HashMap<SolrSearchResult, Float>();
    private final Map<String, SolrSearchResult> items = new HashMap<String, SolrSearchResult>();
    private final Map<SolrSearchResult, String> descs = new HashMap<SolrSearchResult, String>();
    private final Map<SolrSearchResult, Set<String>> tags = new HashMap<SolrSearchResult, Set<String>>();

    // Constructor only available to the static methods below.
    private SearchResults(Map<SolrSearchResult, Float> hitMap) {
        this.hits.putAll(hitMap);
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
        //String queryString = origQueryString.replaceAll("(\\w+log\\b)", "$1ue $1");
        //queryString = queryString.replaceAll("[^a-zA-Z0-9]", " ").trim();
        //queryString = queryString.replaceAll("(\\w+)$", "$1 $1*");
        return origQueryString;
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

        Map<String, String> highlightedDescMap = new HashMap<String, String>();

        String queryString = prepareQueryString(origQueryString);

        LOG.info("Searching query "
                + " was:" + origQueryString + " now:" + queryString);

        String urlString = "http://localhost:8983/solr/new_core2";
        SolrClient client = new HttpSolrClient(urlString);

        QueryResponse resp = client.query(new SolrQuery(queryString));
        System.out.println(resp);
        Map<SolrSearchResult, Float> hits = new HashMap<SolrSearchResult, Float>();
        for(SolrDocument doc : resp.getResults()){
            Integer objectId = doc.getFieldValue("objectId");
            String tagtype = doc.getFieldValue("type");
            String value = doc.getFieldValue("value");
            String name = doc.getFieldValue("name");
            hits.put(new SolrSearchResult(value, tagtype, "test", "test2", object), 1.0f);
        }

//        Map<String, SolrSearchResult> blah1 = new HashMap<String, SolrSearchResult>();
//        Map<SolrSearchResult, String> blah2 = new HashMap<SolrSearchResult, String>();
//        Map<SolrSearchResult, HashSet<String>> blah3 = new HashMap<SolrSearchResult, HashSet<String>>();
//        SearchResults s = new SearchResults(hits, blah1, blah2, blah3);
//
        SearchResults s = new SearchResults(hits);
        return s;
    }
    public ArrayList<SolrSearchResult> getHits() {
        return new ArrayList<SolrSearchResult>(hits.keySet());
    }
    public Integer getNumHits() {
        return hits.size();
    }
    public static Set<Integer> getObjectIds(SearchResults result) {
        return new HashSet<Integer>();
    }
}
