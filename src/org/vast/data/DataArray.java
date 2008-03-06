/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "SensorML DataProcessing Engine".
 
 The Initial Developer of the Original Code is the VAST team at the University of Alabama in Huntsville (UAH). <http://vast.uah.edu> Portions created by the Initial Developer are Copyright (C) 2007 the Initial Developer. All Rights Reserved. Please Contact Mike Botts <mike.botts@uah.edu> for more information.
 
 Contributor(s): 
    Alexandre Robin <robin@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.data;

import org.vast.cdm.common.CDMException;
import org.vast.cdm.common.DataBlock;
import org.vast.cdm.common.DataComponent;
import org.vast.sweCommon.SweConstants;


/**
 * <p><b>Title:</b><br/> DataArray</p>
 *
 * <p><b>Description:</b><br/>
 * Array of identical DataContainers. Can be of variable size.
 * In the case of a variable size array, size is actually given
 * by another component: sizeData which should be a DataValue
 * carrying an Integer value.
 * There are two cases of variable size component:
 *  - The component is explicitely listed in the component tree
 *    (in this case, the component has a parent) 
 *  - The component is implicitely given before the array data
 *    (in this case, the component has no parent)
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @version 1.0
 */
public class DataArray extends AbstractDataComponent
{
    public final static String ARRAY_SIZE_FIELD = "ArraySize";
	protected final static String errorBlockMixed = "Error: DataArrays should never contain a DataBlockMixed";
    protected AbstractDataComponent component = null;
    protected int arraySize = -1;
    protected boolean variableSize;
    protected DataValue sizeData = null;
    protected String sizeDataName = null;
    protected int stepsToSizeData = -1;

    
    public DataArray()
    {
    }  
    
    
    public DataArray(int arraySize)
    {
        this.arraySize = arraySize;
        this.variableSize = false;
    }
    
    
    public DataArray(DataValue sizeData, boolean variableSize)
    {
        this.sizeData = sizeData;
        this.variableSize = variableSize;
        
        if (variableSize)
        	this.arraySize = 1;
        else
        	this.arraySize = sizeData.getData().getIntValue();
    }


    @Override
    public DataArray copy()
    {
    	DataArray newArray = new DataArray();
    	newArray.name = this.name;
    	newArray.properties = this.properties;    	
    	newArray.arraySize = this.arraySize;
    	newArray.variableSize = this.variableSize;
        newArray.addComponent(this.component.copy());
        
        // also keep track of sizeData component
        if (this.variableSize && this.getSizeData() != null)
        {
        	// case of size data within DataArray
        	if (sizeData.parent == null)
            	newArray.setSizeData(sizeData.copy());
        	
        	// case of size data as a separate component
            else
            {
	        	// find where sizeData component is (name + steps to parent)
            	// cannot just copy cause we don't know what the new component
            	// will be until we reconstruct the whole component tree!
	            int step = 0;
	            AbstractDataComponent dataComponent = this;
	            while (dataComponent.getComponent(sizeData.name) != sizeData)
	            {
	                dataComponent = dataComponent.parent;
	                step++;
	            }
	            
	            newArray.sizeDataName = sizeData.name;
	            newArray.stepsToSizeData = step;
            }
        }
        
    	return newArray;
    }
    
    
    @Override
    protected void updateStartIndex(int startIndex)
    {
        dataBlock.startIndex = startIndex;
        
        if (dataBlock instanceof DataBlockMixed)
        {
            throw new IllegalStateException(errorBlockMixed);
        }
        else if (dataBlock instanceof DataBlockParallel)
        {
            component.updateStartIndex(startIndex);
        }
        else // case of primitive array
        {
            component.updateStartIndex(startIndex);
        }
    }
    
    
    @Override
    protected void updateAtomCount(int childAtomCountDiff)
    {
    	childAtomCountDiff = childAtomCountDiff*arraySize;
        
        if (dataBlock != null)
            dataBlock.atomCount += childAtomCountDiff;
        
        if (parent != null)
            parent.updateAtomCount(childAtomCountDiff);
    }
    
    
    @Override
    public void addComponent(DataComponent component)
    {
        String componentName = component.getName();
        if (componentName != null)
            this.names.put(componentName, 0);
        
    	this.component = (AbstractDataComponent)component;
        ((AbstractDataComponent)component).parent = this;
    }


    @Override
    public AbstractDataComponent getComponent(int index)
    {
        checkIndex(index);
        
        if (dataBlock != null)
        {
            int startIndex = dataBlock.startIndex;
            
            if (dataBlock instanceof DataBlockMixed)
            {
                throw new IllegalStateException(errorBlockMixed);
            }            
            else if (dataBlock instanceof DataBlockParallel)
            {
                if (component instanceof DataArray)
                    startIndex += index * ((DataArray)component).arraySize;
                else
                    startIndex += index;
            }
            else if (dataBlock instanceof DataBlockList)
            {
                startIndex = 0;
                component.setData(((DataBlockList)dataBlock).get(index));
            }
            else // primitive block
            {
                startIndex += index * component.scalarCount;
            }
            
            // update child start index
            component.updateStartIndex(startIndex);
        }
        
        return component;
    }
    
    
    @Override
    public void setData(DataBlock dataBlock)
    {
    	this.dataBlock = (AbstractDataBlock)dataBlock;
    	
        // also update arraySize if variable
        if (variableSize)
        {
            // assign variable size value to arraySize
            if (this.getSizeData() != null)
            {
                DataBlock data = sizeData.getData();
                if (data != null)
                    arraySize = data.getIntValue();
            }
        }
        
		// also assign dataBlock to child
    	AbstractDataBlock childBlock = ((AbstractDataBlock)dataBlock).copy();
		childBlock.atomCount = component.scalarCount;
		component.setData(childBlock);
    }
    
    
    @Override
    public void clearData()
    {
        this.dataBlock = null;
        component.clearData();
    }
    
    
    @Override
    public void validateData() throws CDMException
    {
    	// do only if constraints are specified on descendants
    	if (hasConstraints(this))
    	{
    		for (int i = 0; i < getComponentCount(); i++)
    			getComponent(i).validateData();
    	}
    }
    
    
    /**
     * Recursively checks if constraints are specified in descendants
     * @param component
     * @return
     */
    private boolean hasConstraints(DataComponent component)
    {
    	
    	if (component instanceof DataArray)
    	{
    		return hasConstraints(((DataArray)component).component);
    	}
    	else if (component instanceof DataList)
    	{
    		return hasConstraints(((DataList)component).component);
    	}
    	else if (component instanceof DataValue)
    	{
    		return (component.getProperty(SweConstants.CONSTRAINTS) != null);
    	}
    	else if (component instanceof DataGroup || component instanceof DataChoice)
    	{
    		for (int i = 0; i < component.getComponentCount(); i++)
    		{
    			if (hasConstraints(component.getComponent(i)))
    				return true;
    		}
    		
    		return false;
    	}
    	else
    		return false;
    }
    
    
    /**
     * Create the right data block to carry this array data
     * It can be either a scalar array (DataBlockDouble, etc...)
     * or a group of mixed types parallel arrays (DataBlockMixed)
     */
    @Override
    protected AbstractDataBlock createDataBlock()
    {
    	AbstractDataBlock childBlock = component.createDataBlock();
    	AbstractDataBlock newBlock = null;
    	int newSize = 0;
        
    	if (arraySize > 0)
    	{
	    	// if child is parallel block, create bigger parallel block
	        if (childBlock instanceof DataBlockParallel)
	        {
	        	newBlock = childBlock.copy();
                newSize = childBlock.atomCount * arraySize;
	        }
            
            // if child is tuple block, create parallel block
            else if (childBlock instanceof DataBlockTuple)
            {
                DataBlockParallel parallelBlock = new DataBlockParallel();
                parallelBlock.blockArray = ((DataBlockTuple)childBlock).blockArray;
                newSize = childBlock.atomCount * arraySize;
                newBlock = parallelBlock;
            }
            
            // if child is mixed block, create list block
            else if (childBlock instanceof DataBlockMixed)
            {
                DataBlockList blockList = new DataBlockList();
                blockList.add(childBlock.copy());
                newBlock = blockList;
                newSize = arraySize;
            }
	        
	        // if child is already a primitive block, create bigger primitive block
	        else
	        {
	        	newBlock = childBlock.copy();
                newSize = childBlock.atomCount * arraySize; 	
	        }
    	}
        
        newBlock.resize(newSize);
        
        scalarCount = newBlock.atomCount;
        return newBlock;
    }


    /**
     * Check that the integer index given is in range: 0 to size of array - 1
     * @param index int
     * @throws DataException
     */
    protected void checkIndex(int index)
    {
        // error if index is out of range
        if ((index >= arraySize) || (index < 0))
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
    }
    
    
    /**
     * Dynamically update size of a VARIABLE SIZE array
     * Note that sizeData must carry the right value at this time
     */
    public void updateSize()
    {
    	if (variableSize)
    	{
    		int oldSize = arraySize;
    	
            // assign variable size value to arraySize
	    	if (this.getSizeData() != null)
	    	{
	    		DataBlock data = sizeData.getData();
	    		
	    		if (data != null)
                {
                    arraySize = data.getIntValue();
                    
                    // continue only if sized has really changed
                    if (arraySize == oldSize)
                        return;
                }
	    	}
            
            // take care of variable size child array
            if (component instanceof DataArray)
            {
                if (((DataArray)component).isVariableSize())
                    ((DataArray)component).updateSize();
            }
            
            // update scalarCount according to new arraySize
            this.scalarCount = component.scalarCount * arraySize;
            
            // stop here if parent also has variable size
            // in this case parent will have to resize datablock anyway!!
            if (parent instanceof DataArray)
            {
                if (((DataArray)parent).isVariableSize())
                    return;
            }
            
            // resize datablock if non null
            if (dataBlock != null)
            {
                dataBlock.resize(scalarCount);
                setData(dataBlock);
            }
            
            // propagate to parents atomCount as well
            if (parent != null)
                parent.updateAtomCount(component.scalarCount * (arraySize - oldSize));
    	}
    }
    
    
    /**
     * Set the size of this VARIABLE SIZE array to a new value
     * Automatically updates the sizeData component value.
     * @param newSize
     */
    public void updateSize(int newSize)
    {
        if (variableSize && newSize > 0)
        {
            int oldSize = arraySize;
            arraySize = newSize;
        
            // if variable size, change value of sizeData too
            if (this.getSizeData() != null)
            {
                DataBlock data = sizeData.getData();
                
                if (data != null)
                    data.setIntValue(newSize);
            }
            
            if (dataBlock != null)
            {
                this.scalarCount = component.scalarCount * arraySize;
                dataBlock.resize(scalarCount);
                setData(dataBlock);
            }
            
            if (parent != null)
                parent.updateAtomCount(component.scalarCount * (arraySize - oldSize));
        }
    }
    
    
    /**
     * Set the size of this array to a new FIXED value
     * @param newSize
     */
    public void setSize(int newSize)
    {
        if (newSize > 0)
        {
        	this.arraySize = newSize;
            this.variableSize = false;
        }
    }
    
    
    public boolean isVariableSize()
    {
        return this.variableSize;
    }
    

    @Override
    public int getComponentCount()
    {
    	if (variableSize)
    	{
    		if (this.getSizeData() != null)
            {   
                DataBlock data = sizeData.getData();
        		if (data != null)
        			return data.getIntValue();
            }
    	}
    	
    	return this.arraySize;
    }


    public String toString(String indent)
    {
        StringBuffer text = new StringBuffer();
        text.append("DataArray[");
        if (variableSize)
        	text.append("?=");
        text.append(getComponentCount());
        text.append("]: " + name + "\n");
        text.append(indent + "  ");
        text.append(component.toString(indent + "  "));

        return text.toString();
    }
    
    
    @Override
    public void removeAllComponents()
    {
        this.component = null;
        this.dataBlock = null;
    }
    
    
    @Override
    public void removeComponent(int index)
    {
        removeAllComponents();
    }


    public DataValue getSizeData()
    {
        if (sizeData != null)
            return sizeData;
        
        // if DataArray was cloned, try to find sizeData from parent hierarchy
        AbstractDataComponent dataComponent = this;
        for (int i=0; i<stepsToSizeData; i++)
            dataComponent = dataComponent.parent;
        sizeData = (DataValue)dataComponent.getComponent(sizeDataName);
        
        return sizeData;
    }


    public void setSizeData(DataValue sizeData)
    {
        this.sizeData = sizeData;
    }
}
