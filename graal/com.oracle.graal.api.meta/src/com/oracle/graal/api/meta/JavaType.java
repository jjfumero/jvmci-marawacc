/*
 * Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.api.meta;

/**
 * Represents a resolved or unresolved type. Types include primitives, objects, {@code void}, and
 * arrays thereof.
 */
public interface JavaType {

    /**
     * Returns the name of this type in internal form. The following are examples of strings
     * returned by this method:
     * 
     * <pre>
     *     "Ljava/lang/Object;"
     *     "I"
     *     "[[B"
     * </pre>
     */
    String getName();

    /**
     * Returns an unqualified name of this type.
     *
     * <pre>
     *     "Object"
     *     "Integer"
     * </pre>
     */
    default String getUnqualifiedName() {
        String name = getName();
        if (name.indexOf('/') != -1) {
            name = name.substring(name.lastIndexOf('/') + 1);
        }
        if (name.endsWith(";")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    /**
     * For array types, gets the type of the components, or {@code null} if this is not an array
     * type. This method is analogous to {@link Class#getComponentType()}.
     */
    JavaType getComponentType();

    /**
     * Gets the array class type representing an array with elements of this type.
     */
    JavaType getArrayClass();

    /**
     * Gets the kind of this type.
     */
    Kind getKind();

    /**
     * Resolved this type and returns a {@link ResolvedJavaType}. If this type is already a
     * {@link ResolvedJavaType}, it returns this type.
     * 
     * @param accessingClass the class that requests resolving this type
     * @return the resolved Java type
     */
    ResolvedJavaType resolve(ResolvedJavaType accessingClass);
}
