/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.hotspot.snippets;

import java.lang.reflect.*;

import com.oracle.graal.api.code.*;
import com.oracle.graal.api.meta.*;
import com.oracle.graal.compiler.*;
import com.oracle.graal.compiler.phases.*;
import com.oracle.graal.compiler.util.*;
import com.oracle.graal.cri.*;
import com.oracle.graal.debug.*;
import com.oracle.graal.graph.*;
import com.oracle.graal.nodes.*;
import com.oracle.graal.nodes.java.*;

public class IntrinsifyArrayCopyPhase extends Phase {
    private final ExtendedRiRuntime runtime;
    private RiResolvedMethod arrayCopy;
    private RiResolvedMethod byteArrayCopy;
    private RiResolvedMethod shortArrayCopy;
    private RiResolvedMethod charArrayCopy;
    private RiResolvedMethod intArrayCopy;
    private RiResolvedMethod longArrayCopy;
    private RiResolvedMethod floatArrayCopy;
    private RiResolvedMethod doubleArrayCopy;
    private RiResolvedMethod objectArrayCopy;

    public IntrinsifyArrayCopyPhase(ExtendedRiRuntime runtime) {
        this.runtime = runtime;
        try {
            byteArrayCopy = getArrayCopySnippet(runtime, byte.class);
            charArrayCopy = getArrayCopySnippet(runtime, char.class);
            shortArrayCopy = getArrayCopySnippet(runtime, short.class);
            intArrayCopy = getArrayCopySnippet(runtime, int.class);
            longArrayCopy = getArrayCopySnippet(runtime, long.class);
            floatArrayCopy = getArrayCopySnippet(runtime, float.class);
            doubleArrayCopy = getArrayCopySnippet(runtime, double.class);
            objectArrayCopy = getArrayCopySnippet(runtime, Object.class);
            arrayCopy = runtime.getRiMethod(System.class.getDeclaredMethod("arraycopy", Object.class, int.class, Object.class, int.class, int.class));
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static RiResolvedMethod getArrayCopySnippet(RiRuntime runtime, Class<?> componentClass) throws NoSuchMethodException {
        Class<?> arrayClass = Array.newInstance(componentClass, 0).getClass();
        return runtime.getRiMethod(ArrayCopySnippets.class.getDeclaredMethod("arraycopy", arrayClass, int.class, arrayClass, int.class, int.class));
    }

    @Override
    protected void run(StructuredGraph graph) {
        boolean hits = false;
        for (MethodCallTargetNode methodCallTarget : graph.getNodes(MethodCallTargetNode.class)) {
            RiResolvedMethod targetMethod = methodCallTarget.targetMethod();
            RiResolvedMethod snippetMethod = null;
            if (targetMethod == arrayCopy) {
                ValueNode src = methodCallTarget.arguments().get(0);
                ValueNode dest = methodCallTarget.arguments().get(2);
                assert src != null && dest != null;
                RiResolvedType srcType = src.objectStamp().type();
                RiResolvedType destType = dest.objectStamp().type();
                if (srcType != null
                                && srcType.isArrayClass()
                                && destType != null
                                && destType.isArrayClass()) {
                    RiKind componentKind = srcType.componentType().kind(false);
                    if (srcType.componentType() == destType.componentType()) {
                        if (componentKind == RiKind.Int) {
                            snippetMethod = intArrayCopy;
                        } else if (componentKind == RiKind.Char) {
                            snippetMethod = charArrayCopy;
                        } else if (componentKind == RiKind.Long) {
                            snippetMethod = longArrayCopy;
                        } else if (componentKind == RiKind.Byte) {
                            snippetMethod = byteArrayCopy;
                        } else if (componentKind == RiKind.Short) {
                            snippetMethod = shortArrayCopy;
                        } else if (componentKind == RiKind.Float) {
                            snippetMethod = floatArrayCopy;
                        } else if (componentKind == RiKind.Double) {
                            snippetMethod = doubleArrayCopy;
                        } else if (componentKind == RiKind.Object) {
                            snippetMethod = objectArrayCopy;
                        }
                    } else if (componentKind == RiKind.Object
                                    && srcType.componentType().isSubtypeOf(destType.componentType())) {
                        snippetMethod = objectArrayCopy;
                    }
                }
            }

            if (snippetMethod != null) {
                StructuredGraph snippetGraph = (StructuredGraph) snippetMethod.compilerStorage().get(Graph.class);
                assert snippetGraph != null : "ArrayCopySnippets should be installed";
                hits = true;
                Debug.log("%s > Intinsify (%s)", Debug.currentScope(), snippetMethod.signature().argumentTypeAt(0, snippetMethod.holder()).componentType());
                InliningUtil.inline(methodCallTarget.invoke(), snippetGraph, false);
            }
        }
        if (GraalOptions.OptCanonicalizer && hits) {
            new CanonicalizerPhase(null, runtime, null).apply(graph);
        }
    }
}
