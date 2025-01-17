/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2019.                            (c) 2019.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 5 $
*
************************************************************************
 */

package ca.nrc.cadc.dali.tables.votable;

import ca.nrc.cadc.dali.tables.BinaryTableData;
import ca.nrc.cadc.dali.tables.ListTableData;
import ca.nrc.cadc.dali.tables.TableData;
import ca.nrc.cadc.dali.util.Format;
import ca.nrc.cadc.dali.util.FormatFactory;
import ca.nrc.cadc.util.StringUtil;
import ca.nrc.cadc.xml.XmlUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author pdowler
 */
public class VOTableReader {

    private static final Logger log = Logger.getLogger(VOTableReader.class);

    protected static final String PARSER = "org.apache.xerces.parsers.SAXParser";
    protected static final String GRAMMAR_POOL = "org.apache.xerces.parsers.XMLGrammarCachingConfiguration";
    protected static final String VOTABLE_11_SCHEMA = "VOTable-v1.1.xsd";
    protected static final String VOTABLE_12_SCHEMA = "VOTable-v1.2.xsd";
    protected static final String VOTABLE_13_SCHEMA = "VOTable-v1.3.xsd";
    protected static final String VOTABLE_14_SCHEMA = "VOTable-v1.4.xsd";

    // Prefer BINARY2
    protected static final String[] BINARY_TYPES = new String[] {
        "BINARY2", "BINARY", "FITS"
    };

    protected static final String DEFAULT_STREAM_ENCODING = "base64";

    private static final String votable11SchemaUrl;
    private static final String votable12SchemaUrl;
    private static final String votable13SchemaUrl;
    private static final String votable14SchemaUrl;

    private FormatFactory formatFactory;

    static {
        votable11SchemaUrl = getSchemaURL(VOTABLE_11_SCHEMA);
        log.debug("votable11SchemaUrl: " + votable11SchemaUrl);

        votable12SchemaUrl = getSchemaURL(VOTABLE_12_SCHEMA);
        log.debug("votable12SchemaUrl: " + votable12SchemaUrl);

        votable13SchemaUrl = getSchemaURL(VOTABLE_13_SCHEMA);
        log.debug("votable13SchemaUrl: " + votable13SchemaUrl);

        votable14SchemaUrl = getSchemaURL(VOTABLE_14_SCHEMA);
        log.debug("votable14SchemaUrl: " + votable14SchemaUrl);
    }

    static String getSchemaURL(String name) {
        URL url = VOTableReader.class.getClassLoader().getResource(name);
        if (url == null) {
            throw new MissingResourceException("schema not found", VOTableReader.class.getName(), name);
        }
        return url.toString();
    }

    private SAXBuilder docBuilder;

    /**
     * Creates a VOTableReader that validates the VOTable.
     */
    public VOTableReader() {
        this(true);
    }

    /**
     * Creates a VOTableReader and optionally validate the VOTable.
     *
     * @param enableSchemaValidation validate the VOTable if true.
     */
    public VOTableReader(boolean enableSchemaValidation) {
        Map<String, String> schemaMap = null;
        if (enableSchemaValidation) {
            schemaMap = new HashMap<String, String>();
            schemaMap.put(ca.nrc.cadc.dali.tables.votable.VOTableWriter.VOTABLE_11_NS_URI, votable11SchemaUrl);
            schemaMap.put(ca.nrc.cadc.dali.tables.votable.VOTableWriter.VOTABLE_12_NS_URI, votable12SchemaUrl);
            // yes: targetNamespace is 1.3 but version=1.4
            schemaMap.put(ca.nrc.cadc.dali.tables.votable.VOTableWriter.VOTABLE_13_NS_URI, votable14SchemaUrl);
            log.debug("schema validation enabled");
        } else {
            log.debug("schema validation disabled");
        }
        this.docBuilder = XmlUtil.createBuilder(schemaMap);
    }

    public void setFormatFactory(FormatFactory formatFactory) {
        this.formatFactory = formatFactory;
    }

    /**
     * Read a XML VOTable from a String and build a VOTable object.
     *
     * @param xml String of the VOTable XML.
     * @return a VOTable object.
     * @throws IOException if unable to read the VOTable.
     */
    public VOTableDocument read(String xml)
            throws IOException {
        Reader reader = new StringReader(xml);
        return read(reader);
    }

    /**
     * Read a XML VOTable from an InputStream and build a VOTable object.
     *
     * @param istream InputStream to read from.
     * @return a VOTable object.
     * @throws IOException is problem reading the InputStream.
     */
    public VOTableDocument read(InputStream istream)
            throws IOException {
        Reader reader = new BufferedReader(new InputStreamReader(istream, "UTF-8"));
        return read(reader);
    }

    /**
     * Read a XML VOTable from a Reader and build a VOTable object.
     *
     * @param reader Reader to read from.
     * @return a VOTable object.
     * @throws IOException if problem reading from the reader.
     */
    public VOTableDocument read(Reader reader)
            throws IOException {
        if (formatFactory == null) {
            this.formatFactory = new FormatFactory();
        }
        return readImpl(reader);
    }

    protected VOTableDocument readImpl(Reader reader)
            throws IOException {
        // Parse the input document.
        Document document;
        try {
            document = docBuilder.build(reader);
        } catch (JDOMException e) {
            throw new IOException("Unable to parse " + e.getMessage());
        }

        // Returned VOTable object.
        VOTableDocument votable = new VOTableDocument();

        // Document root element.
        Element root = document.getRootElement();

        // Namespace for the root element.
        Namespace namespace = root.getNamespace();
        log.debug("Namespace: " + namespace);

        // document INFO elements
        List<Element> documentInfos = root.getChildren("INFO", namespace);
        votable.getInfos().addAll(getInfos(documentInfos, namespace));

        // RESOURCE elements
        List<Element> resources = root.getChildren("RESOURCE", namespace);
        for (Element resource : resources) {
            Attribute typeAttr = resource.getAttribute("type");
            VOTableResource votResource = new VOTableResource(typeAttr.getValue());
            votable.getResources().add(votResource);

            // Get the RESOURCE utype attribute.
            Attribute utypeAttr = resource.getAttribute("utype");
            if (utypeAttr != null) {
                votResource.utype = utypeAttr.getValue();
            }

            // Get the RESOURCE name attribute.
            Attribute nameAttr = resource.getAttribute("name");
            if (nameAttr != null) {
                votResource.setName(nameAttr.getValue());
            }

            // GET the RESOURCE ID attribute
            Attribute idAttr = resource.getAttribute("ID");
            if (idAttr != null) {
                votResource.id = idAttr.getValue();
            }

            // DESCRIPTION element
            Element description = resource.getChild("DESCRIPTION", namespace);
            if (description != null) {
                votResource.description = description.getText();
            }

            // resource INFO elements
            List<Element> resourceInfos = resource.getChildren("INFO", namespace);
            log.debug("found resource.info: " + resourceInfos.size());
            votResource.getInfos().addAll(getInfos(resourceInfos, namespace));

            // PARAM elements
            List<Element> params = resource.getChildren("PARAM", namespace);
            log.debug("found resource.param: " + params.size());
            votResource.getParams().addAll(getParams(params, namespace));

            // GROUP elements
            List<Element> groups = resource.getChildren("GROUP", namespace);
            log.debug("found resource.group: " + groups.size());
            votResource.getGroups().addAll(getGroups(groups, namespace));

            // TABLE element.
            Element table = resource.getChild("TABLE", namespace);
            if (table != null) {
                VOTableTable vot = new VOTableTable();
                votResource.setTable(vot);

                // table INFO elements
                List<Element> tableInfos = table.getChildren("INFO", namespace);
                log.debug("found resource.table.info: " + tableInfos.size());
                vot.getInfos().addAll(getInfos(tableInfos, namespace));

                // PARAM elements
                List<Element> tparams = table.getChildren("PARAM", namespace);
                log.debug("found resource.table.param: " + tparams.size());
                vot.getParams().addAll(getParams(tparams, namespace));

                // FIELD elements.
                List<Element> tfields = table.getChildren("FIELD", namespace);
                log.debug("found resource.table.field: " + tfields.size());
                vot.getFields().addAll(getFields(tfields, namespace));

                // DATA element.
                Element data = table.getChild("DATA", namespace);
                if (data != null) {
                    // TABLEDATA element.
                    Element tableData = data.getChild("TABLEDATA", namespace);

                    if (tableData != null) {
                        vot.setTableData(getTableData(tableData, namespace, vot.getFields()));
                    } else {
                        final Element binaryData = getBinaryData(data, namespace);
                        if (binaryData == null) {
                            throw new UnsupportedOperationException("Unknown DATA");
                        } else {
                            final Element streamData = binaryData.getChild("STREAM", namespace);
                            if (streamData == null) {
                                vot.setTableData(new ListTableData());
                            } else {
                                // Default to base64 encoding
                                // TODO: check for href in which case encoding may be irrelevant?
                                final String encoding =
                                        streamData.getAttributeValue("encoding",
                                                                     VOTableReader.DEFAULT_STREAM_ENCODING);
                                vot.setTableData(new BinaryTableData(vot.getFields(),
                                                                     new ByteArrayInputStream(
                                                                             streamData.getText().getBytes(
                                                                                     StandardCharsets.UTF_8)),
                                                                     encoding,
                                                                     binaryData.getName().equals("BINARY2")));
                            }
                        }
                    }
                }
            }
        }

        return votable;
    }

    /**
     * Check for a BINARY2, BINARY, or FITS stream.
     * @param dataElement   The parent DATA element.
     * @param namespace     The Namespace attached.
     * @return  The binary element that will contain a STREAM child.  Null if none exists.
     */
    Element getBinaryData(final Element dataElement, final Namespace namespace) {
        for (final String nextTagName : VOTableReader.BINARY_TYPES) {
            final Element nextPotentialBinaryElement = dataElement.getChild(nextTagName, namespace);
            if (nextPotentialBinaryElement != null) {
                return nextPotentialBinaryElement;
            }
        }

        return null;
    }

    /**
     * Builds a List of Info objects from a List of INFO Elements.
     *
     * @param elements List of INFO Elements.
     * @param namespace document namespace.
     * @return List of Info objects.
     */
    protected List<VOTableInfo> getInfos(List<Element> elements, Namespace namespace) {
        ArrayList<VOTableInfo> infos = new ArrayList<VOTableInfo>();
        for (Element element : elements) {
            String name = element.getAttributeValue("name");
            String value = element.getAttributeValue("value");
            if (StringUtil.hasText(name) && StringUtil.hasText(value)) {
                VOTableInfo i = new VOTableInfo(name, value);
                String s = element.getText();
                log.debug("INFO content: " + s);
                if (StringUtil.hasText(s)) {
                    i.content = s;
                }
                String id = element.getAttributeValue("ID");
                if (StringUtil.hasText(id)) {
                    i.id = id;
                }
                infos.add(i);
            }
        }
        return infos;
    }

    /**
     * Builds a List of Info objects from a List of INFO Elements.
     *
     * @param elements List of INFO Elements.
     * @param namespace document namespace.
     * @return List of Info objects.
     */
    protected List<VOTableGroup> getGroups(List<Element> elements, Namespace namespace) {
        ArrayList<VOTableGroup> ret = new ArrayList<VOTableGroup>();
        for (Element element : elements) {
            String name = element.getAttributeValue("name");
            VOTableGroup vg = new VOTableGroup(name);

            // PARAM elements
            List<Element> params = element.getChildren("PARAM", namespace);
            log.debug("found group.param: " + params.size());
            vg.getParams().addAll(getParams(params, namespace));

            // GROUP elements
            List<Element> groups = element.getChildren("GROUP", namespace);
            log.debug("found group.group: " + groups.size());
            vg.getGroups().addAll(getGroups(groups, namespace));

            ret.add(vg);
        }
        return ret;
    }

    /**
     * Build a List of TableParam objects from a List of PARAM Elements.
     *
     * @param elements List of PARAM Elements.
     * @param namespace document namespace.
     * @return List of TableParam objects.
     */
    protected List<VOTableParam> getParams(List<Element> elements, Namespace namespace) {
        ArrayList<VOTableParam> params = new ArrayList<VOTableParam>();
        for (Element element : elements) {
            String name = element.getAttributeValue("name");
            String datatype = element.getAttributeValue("datatype");
            String arraysize = element.getAttributeValue("arraysize");
            String value = element.getAttributeValue("value");
            VOTableParam tableParam = new VOTableParam(name, datatype, arraysize, value);
            updateTableField(tableParam, element, namespace);
            params.add(tableParam);
        }
        return params;
    }

    /**
     * Build a List of TableField objects from a List of FIELD Elements.
     *
     * @param elements List of FIELD Elements.
     * @param namespace document namespace.
     * @return List of TableField objects.
     */
    protected List<VOTableField> getFields(List<Element> elements, Namespace namespace) {
        ArrayList<VOTableField> fields = new ArrayList<VOTableField>();
        for (Element element : elements) {
            String name = element.getAttributeValue("name");
            String datatype = element.getAttributeValue("datatype");
            String arraysize = element.getAttributeValue("arraysize");
            VOTableField tableField = new VOTableField(name, datatype, arraysize);
            updateTableField(tableField, element, namespace);
            fields.add(tableField);
        }
        return fields;
    }

    /**
     * Populate a TableField object with values from the FIELD element.
     *
     * @param tableField TableField to populate.
     * @param element source Element.
     * @param namespace document namespace.
     */
    protected void updateTableField(VOTableField tableField, Element element, Namespace namespace) {
        tableField.id = element.getAttributeValue("ID");
        tableField.ucd = element.getAttributeValue("ucd");
        tableField.unit = element.getAttributeValue("unit");
        tableField.utype = element.getAttributeValue("utype");
        tableField.xtype = element.getAttributeValue("xtype");
        tableField.ref = element.getAttributeValue("ref");

        // DESCRIPTION element for the FIELD.
        Element description = element.getChild("DESCRIPTION", namespace);
        if (description != null) {
            tableField.description = description.getText();
        }

        // VALUES element for the PARAM.
        Element values = element.getChild("VALUES", namespace);
        if (values != null) {
            List<Element> options = values.getChildren("OPTION", namespace);
            if (!options.isEmpty()) {
                for (Element option : options) {
                    tableField.getValues().add(option.getAttributeValue("value"));
                }
            }
            if (values.hasAttributes()) {
                String nullValue = values.getAttributeValue("null");
                tableField.nullValue = nullValue == null ? null : nullValue.trim();
            }
        }
    }

    /**
     * Build a List that contains the TableData rows.
     *
     * @param element TABLEDATA element.
     * @param namespace document namespace.
     * @return TableData object containing rows of data.
     */
    TableData getTableData(Element element, Namespace namespace, List<VOTableField> fields) {
        ListTableData tableData = new ListTableData();

        if (element != null) {
            List<Element> trs = element.getChildren("TR", namespace);
            for (Element tr : trs) {
                List<Object> row = new ArrayList<Object>();
                List<Element> tds = tr.getChildren("TD", namespace);
                for (int i = 0; i < tds.size(); i++) {
                    Element td = tds.get(i);
                    VOTableField field = fields.get(i);
                    Format format = formatFactory.getFormat(field);
                    String text = td.getTextTrim();
                    if (text != null && text.length() == 0) {
                        text = null;
                    }
                    row.add(format.parse(text));
                }
                tableData.getArrayList().add(row);
            }
        }
        return tableData;
    }
}
