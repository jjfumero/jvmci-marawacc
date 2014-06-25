/*
 * Copyright (c) 2009, 2014, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.nodes;

import com.oracle.graal.compiler.common.type.*;
import com.oracle.graal.graph.*;
import com.oracle.graal.graph.spi.*;
import com.oracle.graal.nodes.type.*;

/**
 * The {@code PhiNode} represents the merging of dataflow in the graph. It refers to a merge and a
 * variable.
 */
@NodeInfo(nameTemplate = "ValuePhi({i#values})")
public class ValuePhiNode extends PhiNode implements Simplifiable {

    @Input final NodeInputList<ValueNode> values;

    /**
     * Create a value phi with the specified stamp.
     *
     * @param stamp the stamp of the value
     * @param merge the merge that the new phi belongs to
     */
    public ValuePhiNode(Stamp stamp, MergeNode merge) {
        super(stamp, merge);
        assert stamp != StampFactory.forVoid();
        values = new NodeInputList<>(this);
    }

    /**
     * Create a value phi with the specified stamp and the given values.
     *
     * @param stamp the stamp of the value
     * @param merge the merge that the new phi belongs to
     * @param values the initial values of the phi
     */
    public ValuePhiNode(Stamp stamp, MergeNode merge, ValueNode[] values) {
        super(stamp, merge);
        assert stamp != StampFactory.forVoid();
        this.values = new NodeInputList<>(this, values);
    }

    @Override
    public NodeInputList<ValueNode> values() {
        return values;
    }

    @Override
    public boolean inferStamp() {
        return updateStamp(StampTool.meet(values()));
    }
}
