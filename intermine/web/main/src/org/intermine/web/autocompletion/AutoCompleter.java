package org.intermine.web.autocompletion;

/*
 * Copyright (C) 2002-2016 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.intermine.metadata.ClassDescriptor;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;

/**
 * Autocompleter class for initializing and using the autocompletion
 *
 * @author Dominik Grimm
 * @author Michael Menden
 */
public class AutoCompleter
{
    private  HashMap<String, String> fieldIndexMap = new HashMap<String, String>();
    private Properties prob;

    private static final File TEMP_DIR =
        new File("build" + File.separatorChar + "autocompleteIndexes");

    private static final Logger LOG = Logger.getLogger(AutoCompleter.class);

    /**
     * Autocompleter standard constructor.
     */
    public AutoCompleter() {
        // empty
    }

    /**
     * Autocompleter build index constructor.
     * @param os Objectstore
     * @param prob Properties
     */
    public AutoCompleter(ObjectStore os, Properties prob) {
        this.prob = prob;
        try {
            buildIndex(os);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }



    /**
     * Build the index from the database blob
     * @param os Objectstore
     * @throws IOException IOException
     * @throws ObjectStoreException ObjectStoreException
     * @throws ClassNotFoundException ClassNotFoundException
     */
    public void buildIndex(ObjectStore os)
        throws IOException, ObjectStoreException, ClassNotFoundException, SolrServerException {

        String urlString = "http://localhost:8983/solr/new_core2";
        SolrClient solr = new HttpSolrClient(urlString);

        if (TEMP_DIR.exists()) {
            if (!TEMP_DIR.isDirectory()) {
                throw new RuntimeException(TEMP_DIR + " exists but isn't a directory - remove it");
            }
        } else {
            TEMP_DIR.mkdirs();
        }

        for (Map.Entry<Object, Object> entry: prob.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (!key.endsWith(".autocomplete")) {
                continue;
            }
            String className = key.substring(0, key.lastIndexOf("."));
            ClassDescriptor cld = os.getModel().getClassDescriptorByName(className);
            if (cld == null) {
                throw new RuntimeException("a class mentioned in ObjectStore summary properties "
                                           + "file (" + className + ") is not in the model");
            }
            List<String> fieldNames = Arrays.asList(value.split(" "));
            for (Iterator<String> i = fieldNames.iterator(); i.hasNext();) {

                String fieldName = i.next();
                String classAndField = cld.getUnqualifiedName() + "." + fieldName;
                System.out .println("Indexing " + classAndField);
                fieldIndexMap.put(classAndField, classAndField);


                Query q = new Query();
                q.setDistinct(true);
                QueryClass qc = new QueryClass(Class.forName(cld.getName()));
                q.addToSelect(new QueryField(qc, fieldName));
                q.addFrom(qc);
                Results results = os.execute(q);

                for (Object resRow: results) {
                    @SuppressWarnings("rawtypes")
                    SolrInputDocument document = new SolrInputDocument();
                     
                    Object fieldValue = ((ResultsRow) resRow).get(0);
                    if (fieldValue != null) {
                        document.addField(classAndField, fieldValue.toString());
                        System.out.println(classAndField + " " + fieldValue.toString());
                    }
                    UpdateResponse response = solr.add(document);
                }
                solr.commit();
            }
        }
    }
    public boolean hasAutocompleter(String type, String field) {
        if (fieldIndexMap.get(type + "." + field) != null) {
            return true;
        }
        return false;
    }
}
