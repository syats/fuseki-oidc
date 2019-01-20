/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vdanyliuk.jena.security;

import static org.apache.jena.permissions.AssemblerConstants.EVALUATOR_IMPL;
import static org.apache.jena.permissions.AssemblerConstants.URI;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.NS;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pDefaultGraph;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pGraph;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pGraphAlt;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pGraphName;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pNamedGraph;
import static org.apache.jena.sparql.util.graph.GraphUtils.exactlyOneProperty;
import static org.apache.jena.sparql.util.graph.GraphUtils.getStringValue;
import static org.apache.jena.tdb.assembler.VocabTDB.pLocation;
import static org.apache.jena.tdb.assembler.VocabTDB.pUnionDefaultGraph;

import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.permissions.AssemblerConstants;
import org.apache.jena.permissions.SecuredAssembler;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluatorAssembler;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.core.assembler.DatasetAssemblerException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.sparql.util.MappingRegistry;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.assembler.DatasetAssemblerTDB;
import org.apache.jena.tdb.base.file.Location;

public class SecuredDatasetAssembler extends DatasetAssembler {
    private static boolean initialized;

    static {
        JenaSystem.init();
        init();
    }

    /**
     * Initialize the assembler.
     * Registers the prefix "sec" with the uri http://apache.org/jena/permission/Assembler#
     * and registers this assembler with the uri http://apache.org/jena/permission/Assembler#Model
     */
    static synchronized public void init() {
        if (initialized)
            return;
        MappingRegistry.addPrefixMapping("sec", AssemblerConstants.URI);
        registerWith(Assembler.general);
        initialized = true;
    }

    /**
     * Register this assembler in the assembler group.
     *
     * @param group The assembler group to register with.
     */
    static void registerWith(AssemblerGroup group) {
        if (group == null)
            group = Assembler.general;
        group.implementWith(ResourceFactory.createProperty(URI + "SecuredDataset"), new SecuredDatasetAssembler());
    }

    @Override
    public Dataset createDataset(Assembler a, Resource root, Mode mode) {
        return make(a, root);
    }

    private static Dataset make(Assembler a, Resource root) {
        if (!exactlyOneProperty(root, pLocation))
            throw new AssemblerException(root, "No location given");

        String dir = getStringValue(root, pLocation);
        Location loc = Location.create(dir);
        DatasetGraph dsg = TDBFactory.createDatasetGraph(loc);

        Resource evaluatorImpl = getUniqueResource(root, EVALUATOR_IMPL);
        if (evaluatorImpl == null) {
            throw new AssemblerException(root,
                    String.format("Property %s must be provided for %s", EVALUATOR_IMPL, root));
        }
        SecurityEvaluator securityEvaluator = getEvaluatorImpl(a, evaluatorImpl);

        dsg = new FullySecuredDatasetGraph(dsg, securityEvaluator);

        if (root.hasProperty(pUnionDefaultGraph)) {
            Node b = root.getProperty(pUnionDefaultGraph).getObject().asNode();
            NodeValue nv = NodeValue.makeNode(b);
            if (nv.isBoolean())
                dsg.getContext().set(TDB.symUnionDefaultGraph, nv.getBoolean());
            else
                Log.warn(DatasetAssemblerTDB.class, "Failed to recognize value for union graph setting (ignored): " + b);
        }

        /*
        <r> rdf:type tdb:DatasetTDB ;
            tdb:location "dir" ;
            //ja:context [ ja:cxtName "arq:queryTimeout" ;  ja:cxtValue "10000" ] ;
            tdb:unionGraph true ; # or "true"
        */
        AssemblerUtils.setContext(root, dsg.getContext());
        return DatasetFactory.wrap(dsg);
    }

    private static SecurityEvaluator getEvaluatorImpl(Assembler a, Resource evaluatorImpl) {
        Object obj = a.open(a, evaluatorImpl, Mode.ANY);
        if (obj instanceof SecurityEvaluator) {
            return (SecurityEvaluator) obj;
        }
        throw new AssemblerException(evaluatorImpl, String.format(
                "%s does not specify a SecurityEvaluator instance", evaluatorImpl));
    }

}