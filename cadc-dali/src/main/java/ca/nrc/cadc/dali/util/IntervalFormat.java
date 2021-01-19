/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2020.                            (c) 2020.
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
************************************************************************
*/

package ca.nrc.cadc.dali.util;

import ca.nrc.cadc.dali.DoubleInterval;
import ca.nrc.cadc.dali.Interval;
import ca.nrc.cadc.dali.LongInterval;
import org.apache.log4j.Logger;

/**
 * Generic interval formatter. This can be used to format (output) any type of
 * interval but does not support parsing.
 * 
 * @author pdowler
 */
public class IntervalFormat implements Format<Interval> {
    private static final Logger log = Logger.getLogger(IntervalFormat.class);

    public IntervalFormat() { 
    }

    @Override
    public Interval parse(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String format(Interval t) {
        if (t == null) {
            return "";
        }
        
        // try casting to known types first
        if (t instanceof DoubleInterval) {
            DoubleIntervalFormat f = new DoubleIntervalFormat();
            return f.format((DoubleInterval) t);
        }
        if (t instanceof LongInterval) {
            LongIntervalFormat f = new LongIntervalFormat();
            return f.format((LongInterval) t);
        }
        
        throw new UnsupportedOperationException("unexpected interval type: " + t.getClass().getName());
        
        /*
        if (isFloatingPoint(t)) {
            // float to double like this is lossy (adds non-zero insignificant figs)
            DoubleInterval di = new DoubleInterval(t.getLower().doubleValue(), t.getUpper().doubleValue());
            DoubleIntervalFormat f = new DoubleIntervalFormat();
            return f.format(di);
        }
        
        // else: fixed point
        LongInterval li = new LongInterval(t.getLower().longValue(), t.getUpper().longValue());
        LongIntervalFormat f = new LongIntervalFormat();
        return f.format(li);
        */
    }
    
    // there are more types of fixed point so floating point is easier to detect
    // TODO: might need BigDecimal someday??
    private boolean isFloatingPoint(Interval i) {
        Number n1 = i.getLower();
        Number n2 = i.getUpper();
        return (n1 instanceof Double || n1 instanceof Float
                || n2 instanceof Double || n2 instanceof Float);
    }
}
