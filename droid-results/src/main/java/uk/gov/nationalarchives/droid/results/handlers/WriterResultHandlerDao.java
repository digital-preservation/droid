/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.results.handlers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNodeFilter;


/**
 * An implementation of ResultHandlerDao which writes results out to a Writer,
 * formatting them using an ItemWriter, instead of out to the profile database.
 * This can be used to output results to CSV on the console for example, or XML to a file.
 * <p>
 * We inherit from the JDBCBatchResultHandlerDao class in order to get its
 * database operations for initialising the db and loading lists of formats.
 * Although these functions probably shouldn't belong in result handlers,
 * it's where they are now, and refactoring this turns out to be fairly
 * complex.  So we re-use all the database parts from the JDBCBatchResultHandler,
 * but override the methods involved in writing out to a Writer, and those
 * which can't work if the results aren't being saved to the database
 * (e.g. loading previous results or deleting previous results).
 */
public class WriterResultHandlerDao extends JDBCBatchResultHandlerDao {

    private static final Logger LOG = LoggerFactory.getLogger(WriterResultHandlerDao.class);

    private final List<ProfileResourceNode> items = new ArrayList<>(1);

    private ItemWriter itemWriter;
    private Writer writer;
    private long nodeId = 1L;
    private ProfileResourceNodeFilter filter = new ProfileResourceNodeFilter();

    /**
     * Empty bean constructor.  You still need to set the Itemwriter and DataSource,
     * and then call init() before this class is ready to use.  If you don't also set a Writer,
     * then output will go to the standard system out.
     */
    public WriterResultHandlerDao() {
    }

    /**
     * Parameterized constructor taking the ItemWriter to use to output results to standard out.
     * @param itemWriter The ItemWriter to write out results.
     * @param datasource The database connection to the profile for initialising new databases.
     */
    public WriterResultHandlerDao(ItemWriter itemWriter, DataSource datasource) {
        this(itemWriter, null, datasource);
    }

    /**
     * Parameterized constructor taking the ItemWriter to use to output results to a Writer.
     * @param itemWriter The ItemWriter which formats results and writes them to the writer.
     * @param writer The writer to which results are written.
     * @param datasource The database connection to the profile for initialising new databases.
     */
    public WriterResultHandlerDao(ItemWriter itemWriter, Writer writer, DataSource datasource) {
        setItemWriter(itemWriter);
        setWriter(writer);
        setDatasource(datasource);
        init();
    }

    @Override
    public synchronized void init() {
        try {
            super.init();
        } finally {
            if (writer == null) { // If no writer is set, default to console output.
                writer = new PrintWriter(System.out);
            }
            itemWriter.open(writer);
        }
    }

    @Override
    public synchronized void save(ProfileResourceNode node, ResourceId parentId) {
        node.setId(nodeId++);
        if (parentId != null) {
            node.setParentId(parentId.getId());
        }
        if (filter.passesFilter(node)) { // only write the result if it passes the filter.
            items.clear(); //TODO: we just output each result in a list of one - any value in batching them for output?
            items.add(node);
            itemWriter.write(items);
        }
    }

    @Override
    public synchronized void setFilter(Filter filter) {
        this.filter = new ProfileResourceNodeFilter(filter);
    }

    @Override
    public synchronized void commit() {
        try {
            writer.flush();
        } catch (IOException e) {
            LOG.error("Error flushing writer: " + e.getMessage(), e);
        }
    }

   /**
     * <b>Note:</b> This result handler cannot load any prior results, and always returns null.
     * {@inheritDoc}
     */
    @Override
    public ProfileResourceNode loadNode(final Long nodeIdToLoad) {
        return null;
    }

    /**
     * <b>Note:</b> This result handler cannot delete previous nodes, and this method does nothing.
     * {@inheritDoc}
     */
    @Override
    public void deleteNode(final Long nodeIdToDelete) {
    }

    /**
     * Sets the ItemWriter used to format results for output to a Writer.
     * @param itemWriter The ItemWriter used to format results for output to a Writer.
     */
    public synchronized void setItemWriter(ItemWriter itemWriter) {
        this.itemWriter = itemWriter;
    }

    /**
     * Sets the Writer to which results are written.
     * @param writer The Writer to which results are written.
     */
    public synchronized void setWriter(Writer writer) {
        this.writer = writer;
    }
}
