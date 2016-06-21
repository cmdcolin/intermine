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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.SolrServerException;

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
public final class SearchResults
{
    private static final Logger LOG = Logger.getLogger(SearchResults.class);



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
    public static QueryResponse doFilteredSearch(String origQueryString)
        throws IOException, SolrServerException {

        Map<WebSearchable, String> highlightedDescMap = new HashMap<WebSearchable, String>();

        String queryString = prepareQueryString(origQueryString);

        LOG.info("Searching query "
                + " was:" + origQueryString + " now:" + queryString);

        String urlString = "http://localhost:8983/solr/new_core2";
        SolrClient client = new HttpSolrClient(urlString);

        QueryResponse resp = client.query(new SolrQuery(queryString));

        return resp;
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
