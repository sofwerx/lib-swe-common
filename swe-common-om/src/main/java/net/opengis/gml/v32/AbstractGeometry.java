package net.opengis.gml.v32;



/**
 * POJO class for XML type AbstractGeometryType(@http://www.opengis.net/gml/3.2).
 *
 * This is a complex type.
 */
public interface AbstractGeometry extends AbstractGML
{
    
    
    /**
     * Gets the srsName property
     */
    public String getSrsName();
    
    
    /**
     * Checks if srsName is set
     */
    public boolean isSetSrsName();
    
    
    /**
     * Sets the srsName property
     */
    public void setSrsName(String srsName);
    
    
    /**
     * Gets the srsDimension property
     */
    public int getSrsDimension();
    
    
    /**
     * Checks if srsDimension is set
     */
    public boolean isSetSrsDimension();
    
    
    /**
     * Sets the srsDimension property
     */
    public void setSrsDimension(int srsDimension);
    
    
    /**
     * Unsets the srsDimension property
     */
    public void unSetSrsDimension();
    
    
    /**
     * Gets the axisLabels property
     */
    public String[] getAxisLabels();
    
    
    /**
     * Checks if axisLabels is set
     */
    public boolean isSetAxisLabels();
    
    
    /**
     * Sets the axisLabels property
     */
    public void setAxisLabels(String[] axisLabels);
    
    
    /**
     * Gets the uomLabels property
     */
    public String[] getUomLabels();
    
    
    /**
     * Checks if uomLabels is set
     */
    public boolean isSetUomLabels();
    
    
    /**
     * Sets the uomLabels property
     */
    public void setUomLabels(String[] uomLabels);
}