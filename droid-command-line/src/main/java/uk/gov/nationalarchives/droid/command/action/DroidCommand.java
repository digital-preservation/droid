/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

/**
 * @author rflitcroft
 *
 */
public interface DroidCommand {

    /**
     * Exceutes the command. 
     * @throws CommandExecutionException if the command failed to execution normally
     */
    void execute() throws CommandExecutionException;
}
