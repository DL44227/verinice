/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.gsm;

import sernet.verinice.graph.IElementFilter;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Filter to reject all (objects at the first level below organization.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TopElementFilter implements IElementFilter {

    private Integer orgId;
    
    public TopElementFilter(Integer orgId) {
        super();
        this.orgId = orgId;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.graph.IElementFilter#check(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public boolean check(CnATreeElement element) {
        return element!=null && !element.getParentId().equals(orgId);
    }

}
