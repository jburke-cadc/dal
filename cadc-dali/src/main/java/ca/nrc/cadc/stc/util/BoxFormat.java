/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2009.                            (c) 2009.
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
*  $Revision: 4 $
*
************************************************************************
*/
package ca.nrc.cadc.stc.util;

import ca.nrc.cadc.stc.Box;
import ca.nrc.cadc.stc.StcsParsingException;

/**
 * Class to parse a STC-S phrase to a Box object, and format a Box object to a
 * STC-S phrase.
 * 
 */
public class BoxFormat extends RegionFormat implements Format<Box>
{
    /**
     * Parses a String to a Box.
     *
     * @param phrase the String to parse.
     * @return Box value of the String.
     */
    public Box parse(String phrase)
        throws StcsParsingException
    {
        parseRegion(phrase);

        // CoordPair x.
        Double x = null;
        if (currentWord == null)
        {
            if (words.hasNextDouble())
                x = words.nextDouble();
            else if (words.hasNext())
                throw new StcsParsingException("Invalid CoordPair element " + words.next());
            else
                throw new StcsParsingException("Unexpected end to STC-S phrase before CoordPair element");
        }
        else
        {
            try
            {
                x = Double.valueOf(currentWord);
            }
            catch (NumberFormatException e)
            {
                throw new StcsParsingException("Invalid CoordPair " + currentWord + " in " + phrase);
            }
        }

        // CoordPair y.
        Double y;
        if (words.hasNextDouble())
            y = words.nextDouble();
        else
            throw new StcsParsingException("Coordpair not found in " + phrase);

        // width
        double width;
        if (words.hasNextDouble())
            width = words.nextDouble();
        else
            throw new StcsParsingException("Width not found in " + phrase);

        // height
        double height;
        if (words.hasNextDouble())
            height = words.nextDouble();
        else
            throw new StcsParsingException("Height not found in " + phrase);

        return new Box(frame, refpos, flavor, x, y, width, height);
    }

    /**
     * Takes a Box and returns a String representation.
     * If the Box is null an empty String is returned.
     *
     * @param box Box to format
     * @return String representation of the Box.
     */
    public String format(Box box)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(formatRegion(box));
        sb.append(" ");
        if (box.getCoordPair() != null)
        {
            sb.append(box.getCoordPair());
            sb.append(" ");
        }
        sb.append(box.getWidth());
        sb.append(" ");
        sb.append(box.getHeight());
        return sb.toString().trim();
    }

}
