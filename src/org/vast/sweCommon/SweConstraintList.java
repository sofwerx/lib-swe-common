/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "SensorML DataProcessing Engine".
 
 The Initial Developer of the Original Code is the VAST team at the
 
 Contributor(s): 
    Alexandre Robin <robin@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.sweCommon;

import java.util.ArrayList;


/**
 * <p><b>Title:</b>
 * SweConstraintList
 * </p>
 *
 * <p><b>Description:</b><br/>
 * List of constraints for a given field in SWE Common v1.0
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @date Feb 7, 2007
 * @version 1.0
 */
public class SweConstraintList<DataType> extends ArrayList<SweConstraint<DataType>> implements SweConstraint<DataType>
{
    private static final long serialVersionUID = 8873758049042174380L;

    
    public class IntervalConstraint implements SweConstraint<Double>
    {
        public double min;
        public double max;
        
        public IntervalConstraint(double min, double max)
        {
        	this.min = min;
        	this.max = max;
        }
        
        public boolean validate(Double value)
        {
        	if (value >= min && value <= max)
        		return true;
        	else
        		return false;
        }
    }
    
    
    public class NumberEnumConstraint implements SweConstraint<Double>
    {
    	public double[] valueList;
    	
    	public NumberEnumConstraint(double[] valueList)
        {
        	this.valueList = valueList;
        }
    	
    	public boolean validate(Double value)
        {
        	for (int i=0; i<valueList.length; i++)
        		if (valueList[i] == value)
        			return true;
        	
        	return false;
        }
    }
    
    
    public class TokenEnumConstraint implements SweConstraint<String>
    {
    	public String[] valueList;
    	
    	public TokenEnumConstraint(String[] valueList)
        {
        	this.valueList = valueList;
        }
    	
    	public boolean validate(String value)
        {
        	for (int i=0; i<valueList.length; i++)
        		if (valueList[i].equals(value))
        			return true;
        	
        	return false;
        }
    }
    
    
    public boolean validate(DataType value)
    {
    	for (int i=0; i<size(); i++)
    	{
    		boolean valid = get(i).validate(value);
    		if (valid)
    			return true;
    	}
    	
    	return false;
    }
}