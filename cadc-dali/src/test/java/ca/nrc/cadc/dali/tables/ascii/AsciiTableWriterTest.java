/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2011.                            (c) 2011.
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

package ca.nrc.cadc.dali.tables.ascii;

import ca.nrc.cadc.dali.tables.TableWriter;
import ca.nrc.cadc.dali.tables.votable.*;
import ca.nrc.cadc.util.Log4jInit;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author pdowler
 */
public class AsciiTableWriterTest
{
    private static final Logger log = Logger.getLogger(AsciiTableWriterTest.class);

    static
    {
        Log4jInit.setLevel("ca.nrc.cadc.dali.tables", Level.INFO);
    }

    public AsciiTableWriterTest() { }

    @Test
    public void testContentType()
    {
        TableWriter<VOTableDocument> writer = new AsciiTableWriter(AsciiTableWriter.ContentType.CSV);
        Assert.assertEquals("text/csv; header=present", writer.getContentType());
        Assert.assertEquals("csv", writer.getExtension());

        TableWriter<VOTableDocument> voTableDocumentTableWriter =
                new VOTableWriter();
        Assert.assertEquals("application/x-votable+xml",
                            voTableDocumentTableWriter.getContentType());
        Assert.assertEquals("xml", voTableDocumentTableWriter.getExtension());
    }

    @Test
    public void testReadWriteCSV()
    {
        log.debug("testReadWriteCSV");
        try
        {
            String resourceName = "VOTable resource name";

            // Build a VOTable.
            VOTableDocument expected = new VOTableDocument();

            VOTableResource vr = new VOTableResource("meta");
            expected.getResources().add(vr);
            vr.getParams().addAll(VOTableReaderWriterTest.getMetaParams());
            vr.getGroups().add(VOTableReaderWriterTest.getMetaGroup());

            vr = new VOTableResource("results");
            expected.getResources().add(vr);
            vr.setName(resourceName);

            VOTableTable vot = new VOTableTable();
            vr.setTable(vot);
            vot.getInfos().addAll(VOTableReaderWriterTest.getTestInfos("a"));
            vot.getParams().addAll(VOTableReaderWriterTest.getTestParams());
            vot.getFields().addAll(VOTableReaderWriterTest.getTestFields());
            vot.setTableData(new VOTableReaderWriterTest.TestTableData());

            StringWriter sw = new StringWriter();
            TableWriter<VOTableDocument> writer = new AsciiTableWriter(AsciiTableWriter.ContentType.CSV);

            Assert.assertEquals("Should be csv extension.", "csv",
                                writer.getExtension());

            writer.write(expected, sw);
            String csv = sw.toString();
            log.info("CSV: \n\n" + csv);
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testReadWriteCSV_NoFields()
    {
        log.debug("testReadWriteCSV");
        try
        {
            String resourceName = "VOTable resource name";

            // Build a VOTable.
            VOTableDocument expected = new VOTableDocument();

            VOTableResource vr = new VOTableResource("meta");
            expected.getResources().add(vr);
            vr.getParams().addAll(VOTableReaderWriterTest.getMetaParams());
            vr.getGroups().add(VOTableReaderWriterTest.getMetaGroup());

            vr = new VOTableResource("results");
            expected.getResources().add(vr);
            vr.setName(resourceName);

            VOTableTable vot = new VOTableTable();
            vr.setTable(vot);
            //vot.getInfos().addAll(VOTableReaderWriterTest.getTestInfos());
            //vot.getParams().addAll(VOTableReaderWriterTest.getTestParams());
            //vot.getFields().addAll(VOTableReaderWriterTest.getTestFields());
            vot.setTableData(new VOTableReaderWriterTest.TestTableData());

            StringWriter sw = new StringWriter();
            TableWriter<VOTableDocument> writer = new AsciiTableWriter(AsciiTableWriter.ContentType.CSV);

            Assert.assertEquals("Should be csv extension.", "csv",
                                writer.getExtension());

            writer.write(expected, sw);
            String csv = sw.toString();
            log.info("CSV: \n\n" + csv);
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testReadWriteTSV()
    {
        log.debug("testReadWriteTSV");
        try
        {
            String resourceName = "VOTable resource name";

            // Build a VOTable.
            VOTableDocument expected = new VOTableDocument();

            VOTableResource vr = new VOTableResource("meta");
            expected.getResources().add(vr);
            vr.getParams().addAll(VOTableReaderWriterTest.getMetaParams());
            vr.getGroups().add(VOTableReaderWriterTest.getMetaGroup());

            vr = new VOTableResource("results");
            expected.getResources().add(vr);
            vr.setName(resourceName);

            VOTableTable vot = new VOTableTable();
            vr.setTable(vot);
            vot.getInfos().addAll(VOTableReaderWriterTest.getTestInfos("a"));
            vot.getParams().addAll(VOTableReaderWriterTest.getTestParams());
            vot.getFields().addAll(VOTableReaderWriterTest.getTestFields());
            vot.setTableData(new VOTableReaderWriterTest.TestTableData());

            StringWriter sw = new StringWriter();
            TableWriter<VOTableDocument> writer = new AsciiTableWriter(AsciiTableWriter.ContentType.TSV);

            Assert.assertEquals("Should be tsv extension.", "tsv",
                                writer.getExtension());

            writer.write(expected, sw);
            String tsv = sw.toString();
            log.info("TSV: \n\n" + tsv);
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testReadWriteWithMax()
    {
        log.debug("testReadWriteWithMax");
        long maxrec = 3L;
        try
        {
            String resourceName = "VOTable resource name";

            // Build a VOTable.
            VOTableDocument expected = new VOTableDocument();

            VOTableResource vr = new VOTableResource("meta");
            expected.getResources().add(vr);
            vr.getParams().addAll(VOTableReaderWriterTest.getMetaParams());
            vr.getGroups().add(VOTableReaderWriterTest.getMetaGroup());

            vr = new VOTableResource("results");
            expected.getResources().add(vr);
            vr.setName(resourceName);

            VOTableTable vot = new VOTableTable();
            vr.setTable(vot);
            vot.getInfos().addAll(VOTableReaderWriterTest.getTestInfos("a"));
            vot.getParams().addAll(VOTableReaderWriterTest.getTestParams());
            vot.getFields().addAll(VOTableReaderWriterTest.getTestFields());
            vot.setTableData(new VOTableReaderWriterTest.TestTableData(maxrec + 1));

            StringWriter sw = new StringWriter();
            TableWriter<VOTableDocument> writer = new AsciiTableWriter(AsciiTableWriter.ContentType.CSV);

            Assert.assertEquals("Should be csv extension.", "csv",
                                writer.getExtension());

            writer.write(expected, sw, maxrec);
            String csv = sw.toString();
            log.info("CSV: \n\n" + csv);

            LineNumberReader r = new LineNumberReader(new StringReader(csv));
            int numRows = 0;
            while (r.readLine() != null)
            {
                numRows++;
            }
            // 1 header + data rows
            Assert.assertEquals(maxrec + 1, numRows);

        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }


}
