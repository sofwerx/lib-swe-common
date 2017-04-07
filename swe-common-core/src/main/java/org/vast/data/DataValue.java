/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.data;

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.ScalarComponent;


/**
 * <p>
 * Atomic (no children) DataContainer usually containing a scalar value
 * 08-2014: Updated to implement new API autogenerated from XML schema
 * </p>
 *
 * @author Alex Robin <alex.robin@sensiasoftware.com>
 * */
public abstract class DataValue extends AbstractSimpleComponentImpl implements ScalarComponent
{
        
    public DataValue()
    {
        this.scalarCount = 1;
    }
    
    
    public DataValue(DataType type)
    {
        this.scalarCount = 1;
        this.dataType = type;
        //this.assignNewDataBlock();
    }
    
    
    public abstract DataValue copy();
    
    
    @Override
    public int getComponentCount()
    {
        return 0; //was 1 but i think it's wrong
    } 
    
    
    @Override
    protected void updateStartIndex(int startIndex)
    {
        dataBlock.startIndex = startIndex;
    }
        
    
    @Override
    public void setData(DataBlock dataBlock)
    {
    	if (dataBlock instanceof DataBlockTuple)
    	{
    		int blockIndex = ((DataBlockTuple)dataBlock).startIndex;
    		this.dataBlock = ((DataBlockTuple)dataBlock).blockArray[blockIndex];
    	}
    	else
    		this.dataBlock = (AbstractDataBlock)dataBlock;
    }
    
    
    @Override
    public AbstractDataBlock createDataBlock()
    {
    	switch (dataType)
        {
        	case BOOLEAN:
        		return new DataBlockBoolean(1);
            
        	case BYTE:
        		return new DataBlockByte(1);
                
            case UBYTE:
                return new DataBlockUByte(1);
                
            case SHORT:
            	return new DataBlockShort(1);
                
            case USHORT:
                return new DataBlockUShort(1);
                
            case INT:
            	return new DataBlockInt(1);
                
            case UINT:
                return new DataBlockUInt(1);
                
            case LONG:
            case ULONG:
            	return new DataBlockLong(1);
                                
            case FLOAT:
            	return new DataBlockFloat(1);
                
            case DOUBLE:
            	return new DataBlockDouble(1);
                
            case UTF_STRING:
            case ASCII_STRING:
            	return new DataBlockString(1);
                
            default:
            	throw new IllegalStateException("Unsupported data type " + dataType);
        }
    }
}
