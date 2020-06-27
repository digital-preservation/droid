package uk.gov.nationalarchives.droid.results.handlers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;

/**
 * A class which creates either a database result handler dao (using the data source),
 * or which creates as writer result handler dao (using the data source, itemwriter and writer).
 * If there is a writer set, then the writer dao is created.  If no writer is present, then
 * the database result handler dao is created.  There must always be a data source, no matter
 * which result handler dao is created.
 */
public class ResultHandlerDaoFactory implements FactoryBean<ResultHandlerDao> {

    private static final String CONSOLE = "stdout"; //TODO: what should this be?
    private static final Logger LOG = LoggerFactory.getLogger(WriterResultHandlerDao.class);

    private DataSource datasource;
    private ItemWriter itemWriter;
    private Writer writer;

    /**
     * Empty constructor.
     */
    public ResultHandlerDaoFactory() {
    }

    /**
     * Parameterised constructor taking the data source.
     * @param datasource The data source to use.
     */
    public ResultHandlerDaoFactory(DataSource datasource) {
        this(datasource, null, (Writer) null);
    }

    /**
     * Parameterized constructor taking the data source and item writer.
     * @param datasource The data source to use.
     * @param itemWriter The item writer to use that formats the data.
     */
    public ResultHandlerDaoFactory(DataSource datasource, ItemWriter itemWriter) {
        this(datasource, itemWriter, (Writer) null);
    }

    /**
     * Parameterized constructor taking the data source, item writer and writer to use.
     * @param datasource The data source to use.
     * @param itemWriter The item writer to use that formats the data.
     * @param writer The writer to use which writes out somewhere.
     */
    public ResultHandlerDaoFactory(DataSource datasource, ItemWriter itemWriter, Writer writer) {
        setDatasource(datasource);
        setItemWriter(itemWriter);
        setWriter(writer);
    }

    /**
     * Parameterized constructor taking the data source, item writer and output file path.
     * @param datasource The data source to use
     * @param itemWriter The item writer to use that formats the data.
     * @param outputFilePath The path to a file to write out to.
     */
    public ResultHandlerDaoFactory(DataSource datasource, ItemWriter itemWriter, String outputFilePath) {
        setDatasource(datasource);
        setItemWriter(itemWriter);
        setOutputFilePath(outputFilePath);
    }

    /**
     * Creates the correct object.  If there is a writer, it writes to a file, otherwise to a database.
     * @return A result handler to write the results to.
     */
    public ResultHandlerDao getObject() {
        final ResultHandlerDao result;
        if (writer == null) {
            result = new JDBCBatchResultHandlerDao(datasource);
        } else {
            result = new WriterResultHandlerDao(itemWriter, writer, datasource);
        }
        result.init();
        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return ResultHandlerDao.class;
    }

    /**
     * Sets the data source.
     * @param datasource The data source to use.
     */
    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    /**
     * Sets the item writer, which formats the data.
     * @param itemWriter The item writer to use.
     */
    public void setItemWriter(ItemWriter itemWriter) {
        this.itemWriter = itemWriter;
    }

    /**
     * Sets the writer, which is where the data is written out to.
     * @param writer The writer which is where the data is written out to.
     */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * The path to a file where the data is written out to.
     * If this is set, a Writer is constructed which writes the data out to that file.
     * @param outputFilePath The path to a file where the data will be written.
     */
    public void setOutputFilePath(String outputFilePath) {
        if (outputFilePath != null && !outputFilePath.trim().isEmpty()) {
            if (CONSOLE.equals(outputFilePath.toLowerCase())) {
                writer = new PrintWriter(System.out);
            } else {
                try {
                    writer = new PrintWriter(outputFilePath);
                } catch (FileNotFoundException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

}
