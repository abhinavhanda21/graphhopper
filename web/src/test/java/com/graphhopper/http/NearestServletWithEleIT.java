/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Parameters;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author svantulden
 */
public class NearestServletWithEleIT extends BaseServletTester {
    private static final String dir = "./target/monaco-gh/";

    @AfterClass
    public static void cleanUp() {
        Helper.removeDir(new File(dir));
        shutdownJetty(true);
    }

    @Before
    public void setUp() {
        CmdArgs args = new CmdArgs().
                put("graph.elevation.provider", "srtm").
                put("graph.elevation.cachedir", "../core/files/").
                put(Parameters.CH.PREPARE + "weightings", "no").
                put("prepare.min_one_way_network_size", "0").
                put("config", "../config-example.properties").
                put("datareader.file", "../core/files/monaco.osm.gz").
                put("graph.location", dir);
        setUpJetty(args);
    }

    @Test
    public void testWithEleQuery() throws Exception {
        JsonNode json = nearestQuery("point=43.730864,7.420771&elevation=true");
        assertFalse(json.has("error"));
        ArrayNode point = (ArrayNode) json.get("coordinates");
        assertTrue("returned point is not 3D: " + point, point.size() == 3);
        double lon = point.get(0).asDouble();
        double lat = point.get(1).asDouble();
        double ele = point.get(2).asDouble();
        assertTrue("nearest point wasn't correct: lat=" + lat + ", lon=" + lon + ", ele=" + ele, lat == 43.73070006215647 && lon == 7.421392181993846 && ele == 66.0);
    }

    @Test
    public void testWithoutEleQuery() throws Exception {
        JsonNode json = nearestQuery("point=43.730864,7.420771&elevation=false");
        assertFalse(json.has("error"));
        ArrayNode point = (ArrayNode) json.get("coordinates");
        assertTrue("returned point is not 2D: " + point, point.size() == 2);
        double lon = point.get(0).asDouble();
        double lat = point.get(1).asDouble();
        assertTrue("nearest point wasn't correct: lat=" + lat + ", lon=" + lon, lat == 43.73070006215647 && lon == 7.421392181993846);

        // Default elevation is false        
        json = nearestQuery("point=43.730864,7.420771");
        assertFalse(json.has("error"));
        point = (ArrayNode) json.get("coordinates");
        assertTrue("returned point is not 2D: " + point, point.size() == 2);
        lon = point.get(0).asDouble();
        lat = point.get(1).asDouble();
        assertTrue("nearest point wasn't correct: lat=" + lat + ", lon=" + lon, lat == 43.73070006215647 && lon == 7.421392181993846);
    }
}
