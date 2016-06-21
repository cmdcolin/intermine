package org.intermine.api.solr;

/*
 * Copyright (C) 2002-2016 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */
import org.intermine.api.search.WebSearchable;
import org.intermine.api.search.WebSearchWatcher;
import org.intermine.api.profile.Taggable;
import org.intermine.api.search.OriginatingEvent;

/**
 * An interface implemented by objects that return from Solr.
 *
 * @author Colin Diesh
 */
public class SolrSearchResult implements WebSearchable
{
    String title;
    String description;
    String tagtype;
    String name;

    public SolrSearchResult(String titl, String desc, String tag, String nam) {
        title = titl;
        description = desc;
        tagtype = tag;
        name = nam;
    }

    /**
     * The user-friendly title for this object.
     * @return the title
     */
    public String getName() {
        return name;
    }
    /**
     * The user-friendly title for this object.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Return the tag of this object.
     * @return the tag 
     */
    public String getTagType() {
        return tagtype;
    }
    /**
     * Return the description of this object.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Add this observer to the list of interested parties. The observer should be notified
     * of every change event this web searchable object has cause to issue.
     * @param wsw The observer.
     */
    public void addObserver(WebSearchWatcher wsw) {
    }

    /**
     * Remove this observer from the list of interested parties. The observer should not be notified
     * of any subsequent events this web searchable object has cause to generate.
     * @param wsw The observer.
     */
    public void removeObserver(WebSearchWatcher wsw) {
    }

    /**
     * Notify all your observers of this event which originates at this web searchable.
     * @param e The event that has just occurred.
     */
    public void fireEvent(OriginatingEvent e) {
    }


}
