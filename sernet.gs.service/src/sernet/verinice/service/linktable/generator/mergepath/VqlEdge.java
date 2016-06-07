/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable.generator.mergepath;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an edge between {@link VqlNode}.
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
final public class VqlEdge {

    private static final String DELIMITER = ":";
    private final EdgeType edgeType;
    private final String path;
    private VqlNode source;
    private VqlNode target;
    private Set<String> propertyTypes;

    /**
     * There is no LT-Type in the memory representation. From a technical point
     * of view the LINK-Type is the same. The only difference between them, LT
     * has a property.
     *
     * Every LT-Type is mapped to a an edge of type LINK with at least one
     * property.
     *
     * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
     *
     */
    public enum EdgeType {
        LINK, PARENT, CHILD, PROP
    };

    VqlEdge(EdgeType edgeType, String path, VqlNode source, VqlNode target) {
        this.edgeType = edgeType;
        this.path = path;
        this.source = source;
        this.target = target;

        if (edgeType == EdgeType.LINK) {
            propertyTypes = new HashSet<>();
        }
    }

    public void addPropertyType(String propertyType) {
        propertyTypes.add(propertyType);
    }

    public Set<String> getPropertyTypes(String propertyType) {

        if (this.edgeType != EdgeType.LINK) {
            return Collections.emptySet();
        }

        return new HashSet<>(propertyTypes);
    }

    public String getPath() {
        return path;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public Set<String> getPropertyTypes() {
        return propertyTypes;
    }

    public String getPathforProperty(String propertyType) {
        if (!isMatch()) {
            throw new IllegalStateException("VqlEdge contains no properties: " + this);
        }

        if (!propertyTypes.contains(propertyType)) {
            throw new IllegalStateException("VqlEdge does not contain this property type: " + propertyType);
        }

        return path + DELIMITER + propertyType;
    }

    /**
     * Returns true if this is a link and property types are found.
     */
    public boolean isMatch() {
        return EdgeType.LINK == edgeType && !propertyTypes.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((edgeType == null) ? 0 : edgeType.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VqlEdge other = (VqlEdge) obj;
        if (edgeType != other.edgeType)
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "VqlEdge [edgeType=" + edgeType + ", path=" + path + ", source=" + source + ", target=" + target + ", propertyTypes=" + propertyTypes + "]";
    }
}
