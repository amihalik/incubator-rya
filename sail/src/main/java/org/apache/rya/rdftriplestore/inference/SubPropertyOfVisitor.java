/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rya.rdftriplestore.inference;

import java.util.Set;
import java.util.UUID;

import org.apache.rya.api.RdfCloudTripleStoreConfiguration;
import org.apache.rya.api.utils.NullableStatementImpl;
import org.apache.rya.rdftriplestore.utils.FixedStatementPattern;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

/**
 * All predicates are changed
 * Class SubPropertyOfVisitor
 * Date: Mar 29, 2011
 * Time: 11:28:34 AM
 */
public class SubPropertyOfVisitor extends AbstractInferVisitor {

    public SubPropertyOfVisitor(final RdfCloudTripleStoreConfiguration conf, final InferenceEngine inferenceEngine) {
        super(conf, inferenceEngine);
        include = conf.isInferSubPropertyOf();
    }

    @Override
    protected void meetSP(final StatementPattern node) throws Exception {
        final StatementPattern sp = node.clone();
        final Var predVar = sp.getPredicateVar();

        final URI pred = (URI) predVar.getValue();
        final String predNamespace = pred.getNamespace();

        final Var objVar = sp.getObjectVar();
        final Var cntxtVar = sp.getContextVar();
        if (objVar != null &&
                !RDF.NAMESPACE.equals(predNamespace) &&
                !SESAME.NAMESPACE.equals(predNamespace) &&
                !RDFS.NAMESPACE.equals(predNamespace)
                && !EXPANDED.equals(cntxtVar)) {
            /**
             *
             * { ?subProp rdfs:subPropertyOf ub:worksFor . ?y ?subProp <http://www.Department0.University0.edu> }\n" +
             "       UNION " +
             "      { ?y ub:worksFor <http://www.Department0.University0.edu> }
             */
//            String s = UUID.randomUUID().toString();
//            Var subPropVar = new Var(s);
//            StatementPattern subPropOf = new StatementPattern(subPropVar, new Var("c-" + s, SESAME.DIRECTSUBPROPERTYOF), predVar, EXPANDED);
//            StatementPattern subPropOf2 = new StatementPattern(sp.getSubjectVar(), subPropVar, objVar, EXPANDED);
//            InferJoin join = new InferJoin(subPropOf, subPropOf2);
//            join.getProperties().put(InferConstants.INFERRED, InferConstants.TRUE);
//            node.replaceWith(join);

//            Collection<URI> parents = inferenceEngine.findParents(inferenceEngine.subPropertyOfGraph, (URI) predVar.getValue());
//            if (parents != null && parents.size() > 0) {
//                StatementPatterns statementPatterns = new StatementPatterns();
//                statementPatterns.patterns.add(node);
//                Var subjVar = node.getSubjectVar();
//                for (URI u : parents) {
//                    statementPatterns.patterns.add(new StatementPattern(subjVar, new Var(predVar.getName(), u), objVar));
//                }
//                node.replaceWith(statementPatterns);
//            }
//            if (parents != null && parents.size() > 0) {
//                VarCollection vc = new VarCollection();
//                vc.setName(predVar.getName());
//                vc.values.add(predVar);
//                for (URI u : parents) {
//                    vc.values.add(new Var(predVar.getName(), u));
//                }
//                Var subjVar = node.getSubjectVar();
//                node.replaceWith(new StatementPattern(subjVar, vc, objVar, node.getContextVar()));
//            }

            final URI subprop_uri = (URI) predVar.getValue();
            final Set<URI> parents = InferenceEngine.findParents(inferenceEngine.getSubPropertyOfGraph(), subprop_uri);
            if (parents != null && parents.size() > 0) {
                final String s = UUID.randomUUID().toString();
                final Var typeVar = new Var(s);
                final FixedStatementPattern fsp = new FixedStatementPattern(typeVar, new Var("c-" + s, RDFS.SUBPROPERTYOF), predVar, cntxtVar);
//                fsp.statements.add(new NullableStatementImpl(subprop_uri, RDFS.SUBPROPERTYOF, subprop_uri));
                //add self
                parents.add(subprop_uri);
                for (final URI u : parents) {
                    fsp.statements.add(new NullableStatementImpl(u, RDFS.SUBPROPERTYOF, subprop_uri));
                }

                final StatementPattern rdfType = new DoNotExpandSP(sp.getSubjectVar(), typeVar, sp.getObjectVar(), cntxtVar);
                final InferJoin join = new InferJoin(fsp, rdfType);
                join.getProperties().put(InferConstants.INFERRED, InferConstants.TRUE);
                node.replaceWith(join);
            }
        }
    }
}
