/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.widgetwrapper;

/**
 * Interface for classes which act as option panes.
 * @author rflitcroft
 *
 */
public interface JOptionPaneProxy {

    /** The YES response. */
    int YES = 1;
    /** The NO response. */
    int NO = 2;
    /** The CANCEL response. */
    int CANCEL = 3;
    
    /**
     * 
     * @return the response value
     */
    int getResponse();
    
}
