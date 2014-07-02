/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.wicket.panels;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.provider.SolrDocumentProvider;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Panel that renders an HTML form with hidden fields to submits the results of
 * a query selection to the Virtual Collection Registry in order to create a
 * new collection
 *
 * @author twagoo
 */
public class VirtualCollectionFormPanel extends GenericPanel<QueryFacetsSelection> {

    //TODO: make configurable
    private final int RESULTSET_SIZE_LIMIT = 10;
    //TODO: make configurable
    private final String vcrSubmitEndpoint = "http://localhost:8080/VirtualCollectionRegistry/service/submit";
    private final IDataProvider<SolrDocument> documentProvider;

    public VirtualCollectionFormPanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);

        //TOOD: optionally get provider through constructor (so that it can be shared)
        documentProvider = new SolrDocumentProvider(model);

        final WebMarkupContainer form = new WebMarkupContainer("vcrForm");
        form.add(new AttributeModifier("action", Model.of(vcrSubmitEndpoint)));
        add(form);

        final IModel<String> nameModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                final String query = getModelObject().getQuery();
                if (query != null) {
                    return "VLO search results: " + query;
                } else {
                    return "VLO search results";
                }
            }
        };

        final WebMarkupContainer collectionName = new WebMarkupContainer("collectionName");
        collectionName.add(new AttributeModifier("value", nameModel));
        form.add(collectionName);

        form.add(new DataView<SolrDocument>("metadataUris", documentProvider) {

            @Override
            protected void populateItem(Item<SolrDocument> item) {
                final WebMarkupContainer mdUri = new WebMarkupContainer("metadataUri");
                final IModel<String> linkModel = new SolrFieldStringModel(item.getModel(), FacetConstants.FIELD_SELF_LINK);
                if (linkModel.getObject() == null) {
                    mdUri.add(new AttributeModifier("value", new SolrFieldStringModel(item.getModel(), FacetConstants.FIELD_COMPLETE_METADATA)));
                } else {
                    mdUri.add(new AttributeModifier("value", linkModel));
                }
                item.add(mdUri);
            }
        });
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(documentProvider.size() < RESULTSET_SIZE_LIMIT);
    }

}
