package org.intermine.web.struts;

/*
 * Copyright (C) 2002-2007 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.ArrayList;
import java.util.Map;

import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.util.TypeUtil;
import org.intermine.web.logic.Constants;
import org.intermine.web.logic.WebUtil;
import org.intermine.web.logic.bag.InterMineBag;
import org.intermine.web.logic.profile.Profile;
import org.intermine.web.logic.widget.EnrichmentWidgetLdr;

import java.lang.reflect.Constructor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.actions.TilesAction;

/**
 *
 * @author Julie Sullivan
 */
public class EnrichmentWidgetController extends TilesAction
{

    /**
     * {@inheritDoc}
     */
     public ActionForward execute(@SuppressWarnings("unused") ComponentContext context,
                                  @SuppressWarnings("unused") ActionMapping mapping,
                                  @SuppressWarnings("unused")  ActionForm form,
                                  HttpServletRequest request,
                                  @SuppressWarnings("unused") HttpServletResponse response)
     throws Exception {
         
         //EnrichmentWidgetForm ewf = (EnrichmentWidgetForm) form;
         EnrichmentWidgetForm ewf = new EnrichmentWidgetForm();
         
         String bagName = request.getParameter("bagName");
         String dataLoader = request.getParameter("ldr");
         
         String filters = request.getParameter("filters");
         String title = request.getParameter("title");
         String link = request.getParameter("link");
         String descr = request.getParameter("descr");
         String filterLabel = request.getParameter("filterLabel");
         String label = request.getParameter("label");
         
         
         ewf.setBagName(bagName);
         ewf.setLdr(dataLoader);
         ewf.setDescr(descr);
         ewf.setLabel(label);
         ewf.setLink(link);
         ewf.setTitle(title);
         ewf.setFilters(filters);
         ewf.setFilterLabel(filterLabel);
         
         HttpSession session = request.getSession();
         Profile profile = (Profile) session.getAttribute(Constants.PROFILE);
         ServletContext servletContext = session.getServletContext();
         ObjectStoreInterMineImpl os =
             (ObjectStoreInterMineImpl) servletContext.getAttribute(Constants.OBJECTSTORE);

         
         Map<String, InterMineBag> allBags =
             WebUtil.getAllBags(profile.getSavedBags(), servletContext);
         InterMineBag bag = allBags.get(bagName);
         if (bag == null) {
             return null;
         }
        ewf.setBag(bag);

         Class<?> clazz = TypeUtil.instantiate(dataLoader);
         Constructor<?> constr = clazz.getConstructor(new Class[]
                                                             {
             HttpServletRequest.class
                                                             });

         EnrichmentWidgetLdr ldr = (EnrichmentWidgetLdr) constr.newInstance(new Object[]
                                                                                       {
             request
                                                                                       });

         ArrayList<Map> results = WebUtil.statsCalc(os, ldr.getPopulation(), ldr.getSample(),
                                               bag, ldr.getTotal(os), 
                                               new Double(0 + ewf.getMax()),
                                               ewf.getErrorCorrection());

         if (results.isEmpty()) {
             return null;
         }
         request.setAttribute("pvalues", results.get(0));
         request.setAttribute("totals", results.get(1));
         request.setAttribute("labelToId", results.get(2));
         request.setAttribute("bagType", bag.getType());
         request.setAttribute("referencePopulation", "All " + bag.getType() + "s from  "
                              + ldr.getReferencePopulation().toString());

         return null;
     }
}

