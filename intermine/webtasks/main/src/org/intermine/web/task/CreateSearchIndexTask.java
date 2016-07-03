package org.intermine.web.task;

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
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.intermine.api.config.ClassKeyHelper;
import org.intermine.metadata.FieldDescriptor;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.metadata.ClassDescriptor;



import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;


/**
 * Create a the Lucene keyword search index for a mine.
 * @author Alex Kalderimis
 *
 */
public class CreateSearchIndexTask extends Task
{

    protected String osAlias = null;
    protected ObjectStore os;
    private ClassLoader classLoader;

    /**
     * Set the alias of the main object store.
     * @param osAlias the object store alias
     */
    public void setOSAlias(String osAlias) {
        this.osAlias = osAlias;
    }

    /**
     * Set the object store.
     * @param os The object store.
     */
    public void setObjectStore(ObjectStore os) {
        this.os = os;
    }

    private ObjectStore getObjectStore() throws Exception {
        if (os != null) {
            return os;
        }
        if (osAlias == null) {
            throw new BuildException("objectStoreWriter attribute is not set");
        }
        if (os == null) {
            System .out.println("Connecting to db: " + osAlias);
            os = ObjectStoreFactory.getObjectStore(osAlias);
        }
        return os;
    }

    /**
     * Set the class loader
     * @param loader The class loader.
     */
    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

    private ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        return this.getClass().getClassLoader();
    }

    @Override
    public void execute() {
        try {
            System .out.println("Creating lucene index for keyword search...");

            ObjectStore objectStore;
            objectStore = getObjectStore();
            
            if (!(objectStore instanceof ObjectStoreInterMineImpl)) {
                // Yes, yes, this is horrific...
                throw new RuntimeException("Got invalid ObjectStore - must be an "
                        + "instance of ObjectStoreInterMineImpl!");
            }


            String urlString = "http://localhost:8983/solr/new_core2";
            SolrClient solr = new HttpSolrClient(urlString);



            //read class keys to figure out what are keyFields during indexing
            InputStream is = getClassLoader().getResourceAsStream("class_keys.properties");
            Properties classKeyProperties = new Properties();
            try {
                classKeyProperties.load(is);
            } catch (NullPointerException e) {
                throw new BuildException("Could not find the class keys");
            } catch (IOException e) {
                throw new BuildException("Could not read the class keys", e);
            }
            Map<String, List<FieldDescriptor>> classKeys =
                ClassKeyHelper.readKeys(objectStore.getModel(), classKeyProperties);

            // hardcode field to index
            String fieldName = "primaryIdentifier";
            classKeys.remove("DataSet");
            classKeys.remove("DataSource");
            classKeys.remove("GOTerm");
            classKeys.remove("SOTerm");
            classKeys.remove("OntologyTerm");
            classKeys.remove("Organism");
            classKeys.remove("Author");
            classKeys.remove("Publication");

            for(String className : classKeys.keySet()) {
                System.out.println("Indexing: "+className);
                ClassDescriptor cld = os.getModel().getClassDescriptorByName(className);
                if (cld == null) {
                    throw new RuntimeException("a class mentioned in ObjectStore summary properties "
                                               + "file (" + className + ") is not in the model");
                }
                String classAndField = cld.getUnqualifiedName() + "." + fieldName;
                System.out .println("Indexing " + classAndField);


                Query q = new Query();
                q.setDistinct(true);
                QueryClass qc = new QueryClass(Class.forName(cld.getName()));
                q.addToSelect(new QueryField(qc, fieldName));
                q.addToSelect(new QueryField(qc, "id"));
                q.addFrom(qc);
                Results results = os.execute(q);

                for (Object resRow: results) {
                    @SuppressWarnings("rawtypes")
                    SolrInputDocument document = new SolrInputDocument();
                    Object fieldValue = ((ResultsRow) resRow).get(0);
                    Object fieldId = ((ResultsRow) resRow).get(1);
                    if(fieldValue!=null) {
                        document.addField("value", fieldValue.toString());
                        document.addField("type", classAndField);
                        document.addField("objectId", fieldId.toString());
                        System.out.println(classAndField + " " + fieldValue.toString() + " " + fieldId.toString()+" "+classAndField);
                    }
                    else {
                        System.out.println("ERROR?" + " "+fieldValue+ " "+fieldId+" "+classAndField);
                    }
                    UpdateResponse response = solr.add(document);
                }
                solr.commit();
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
        //KeywordSearch.saveIndexToDatabase(objectStore, classKeys);
        //KeywordSearch.deleteIndexDirectory();
    }


}
